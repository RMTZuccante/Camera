package matrix;

import camera.Camera;
import matrix.Cell.Victim;
import matrix.SerialConnector.*;
import matrix.cartesian.Plane;

import static matrix.SerialConnector.*;

public class Matrix {
    public final static byte NORTH = 0, EAST = 1, SOUTH = 2, WEST = 3;
    private static final int[] weights = new int[]{0, 1, 1, 2}; //Constant weights for pathfinding
    private final Camera left, right;
    private byte direction;
    private SerialConnector connector;
    private int maxWallDist;
    private Cell start, actual;
    private float bodyTemp;
    private Plane plane;

    private Step firstStep = new Step();

    public Matrix(SerialConnector connector, Camera left, Camera right, int maxWallDist, float bodyTemp) {
        this.left = left;
        this.right = right;
        this.connector = connector;
        this.maxWallDist = maxWallDist;
        this.bodyTemp = bodyTemp;
        direction = NORTH;
    }

    public void start() {
        while (!connector.handShake()) {
            System.err.println("Handshake failed!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        connector.setDebug((byte) 0);

        start = actual = new Cell();
        plane = new Plane(start);

        while (true) {
            boolean victimfound = actual.victim != Victim.NONE;
            inspectCell();
            Direction dir = nextDirection();
            if (dir != null) {
                switch (dir) {
                    case BACK:
                        System.out.println("Go back");
                        connector.rotate(90);
                        direction = getNewCardinalDirection(direction, Direction.RIGHT);
                        inspectCell();
                        connector.rotate(90);
                        direction = getNewCardinalDirection(direction, Direction.RIGHT);
                        break;
                    case LEFT:
                        System.out.println("Go left");
                        connector.rotate(-90);
                        direction = getNewCardinalDirection(direction, Direction.LEFT);
                        inspectCell();
                        break;
                    case RIGHT:
                        System.out.println("Go right");
                        connector.rotate(90);
                        direction = getNewCardinalDirection(direction, Direction.RIGHT);
                        inspectCell();
                        break;
                    case FRONT:
                        System.out.println("Go straight");
                        break;

                }
                if (actual.victim != Victim.NONE && !victimfound) {
                    int packages = 0;

                    if (actual.victim == Victim.HEAT || actual.victim == Victim.S) {
                        packages = 1;
                    } else if (actual.victim == Victim.H) {
                        packages = 2;
                    }
                    connector.victim(packages);
                }


                System.out.println("\tGo!");
                go(true);
                int goret = connector.go();
                if (goret == GOBLACK) {
                    actual.black = true;
                    firstStep.next = null;
                    go(false);
                } else {
                    if (goret == GOOBSTACLE) actual.weight = 10;
                    if (goret == GORISE) actual.weight = 20;
                }
            } else {
                System.out.println("Finished! MISSION COMPLETED!");
                break;
            }
        }
    }

    private void inspectCell() {
        Distances distances = connector.getDistances();

        /*If there are no walls on some directions add a cell beside the actual one*/
        if (distances.getFrontL() > maxWallDist) { //TODO bottle
            addFrontCell();
            System.out.println("front cell: " + distances.getFrontL());
        }
        if (distances.getLeft() > maxWallDist) {
            addLeftCell();
            System.out.println("left cell: " + distances.getLeft());
        }
        if (distances.getRight() > maxWallDist) {
            addRightCell();
            System.out.println("right cell: " + distances.getRight());
        }
        if (distances.getBack() > maxWallDist) {
            addBackCell();
            System.out.println("back cell: " + distances.getBack());
        }

        actual.mirror = isMirror();
        if (isVictim()) actual.victim = Victim.HEAT;
        actual.visited = true; //Mark the cell as visited
    }

    private void go(boolean forward) {
        if (forward) {
            actual = getCellByCardinalDirection(actual, direction);
        } else {
            actual = getCellByCardinalDirection(actual, getNewCardinalDirection(direction, Direction.BACK));
        }
        byte dir = forward ? direction : getNewCardinalDirection(direction, Direction.BACK);
        plane.move(dir);
    }

    private boolean isMirror() {
        Color color = connector.getColor();
        // TODO improve detections rules
        return (color.getBlue() < color.getGreen()) && color.getRed() < color.getGreen();
    }


    private boolean isVictim() {
        Temps temps = connector.getTemps();
        // TODO improve detections rules
        return temps.getLeft() > bodyTemp || temps.getRight() > bodyTemp;
    }

    /**
     * Returns the next direction to take, calls pathfinding if needed.
     * It also checks if all the maze is visited
     *
     * @return Direction: the next direction to take
     */
    private Direction nextDirection() {
        Direction dir = null;
        boolean gotNewDir = false;
        if (firstStep.next == null) {
            if (pathFinding(actual, firstStep, direction) == -1) {
                if (actual != start) {
                    System.err.println("No more cells, ALL VISITED!, going back to home");
                    start.visited = false;
                    if (pathFinding(actual, firstStep, direction) == -1) {
                        System.err.println("Failed pathfinding to home! ERROR!");
                    } else gotNewDir = true;
                }
            } else gotNewDir = true;
        } else gotNewDir = true;

        if (gotNewDir) {
            dir = firstStep.next.direction;
            firstStep.next = firstStep.next.next;
        }
        return dir;
    }

    /**
     * @param cell      the actual cell
     * @param prev      the previews step (direction to assume)
     * @param direction the cardinal direction
     * @return int: the weight of the found path, -1 means that there are no path to new cells
     */
    private int pathFinding(Cell cell, Step prev, byte direction) {
        int weight = -1;
        if (cell != null && !cell.considered && !cell.black) { //If the cell doesn't exists (wall), it has already been considered or is black return the weight (-1), else calculate a path
            if (cell.visited) { //If the cell is already visited keep looking
                cell.considered = true; //Mark the cell as considered
                Step[] steps = new Step[]{new Step(Direction.FRONT), new Step(Direction.RIGHT), new Step(Direction.LEFT), new Step(Direction.BACK)}; //Directions ordered to check the path
                byte[] cardinals = new byte[]{direction, getNewCardinalDirection(direction, Direction.RIGHT), getNewCardinalDirection(direction, Direction.LEFT), getNewCardinalDirection(direction, Direction.BACK)}; //Set cardinal directions
                int pos = -1; //Index of found shortest path to new cell

                for (int i = 0; i < 4; i++) {
                    int tempw = pathFinding(getCellByCardinalDirection(cell, cardinals[i]), steps[i], cardinals[i]); //Temporary weight of calculated path
                    if (tempw != -1) { //If the calculated path found a new cell
                        tempw += weights[i]; //Add the rotation weight to tempw
                        if (weight == -1 || tempw < weight) { //If the new path is worth update values
                            weight = tempw;
                            pos = i;
                            if ((tempw - weights[i]) == 0)
                                break; //If new path's weight (without the rotation weight) is 0 that means that a new cell is near the actual so this path can be considered as the best
                        }
                    }
                }

                if (weight != -1) { //If a new path has been found add the actual cell's weight and add the direction
                    weight += cell.weight;
                    prev.next = steps[pos];
                }
                cell.considered = false; //Set considered to false so new paths can be calculated through this cell
            } else { //If the cell isn't visited yet set the weight to 0
                weight = 0;
                prev.next = null;
            }
        }
        return weight;
    }

    private Cell getCellByCardinalDirection(Cell cell, byte dir) {
        Cell c;
        if (dir == NORTH) c = cell.north;
        else if (dir == SOUTH) c = cell.south;
        else if (dir == EAST) c = cell.east;
        else c = cell.west;
        return c;
    }

    private byte getNewCardinalDirection(byte start, Direction rot) {
        if (rot == Direction.LEFT) {
            start--;
        } else if (rot == Direction.RIGHT) {
            start++;
        } else if (rot == Direction.BACK) {
            start += 2;
        }
        return (byte) Math.floorMod(start, 4);
    }

    private void addFrontCell() {
        addCell(NORTH, SOUTH, EAST, WEST);
    }

    private void addRightCell() {
        addCell(WEST, EAST, NORTH, SOUTH);
    }

    private void addLeftCell() {
        addCell(EAST, WEST, SOUTH, NORTH);
    }

    private void addBackCell() {
        addCell(SOUTH, NORTH, WEST, EAST);
    }

    private void addCell(byte south, byte north, byte west, byte east) {
        if (direction == south && actual.north == null) {
            actual.north = plane.getNear(NORTH);
            if (actual.north == null) {
                System.out.print("new ");
                actual.north = new Cell();
                plane.setNear(NORTH, actual.north);
            }
            actual.north.south = actual;
        } else if (direction == north && actual.south == null) {
            actual.south = plane.getNear(SOUTH);
            if (actual.south == null) {
                System.out.print("new ");
                actual.south = new Cell();
                plane.setNear(SOUTH, actual.south);
            }
            actual.south.north = actual;
        } else if (direction == west && actual.east == null) {
            actual.east = plane.getNear(EAST);
            if (actual.east == null) {
                System.out.print("new ");
                actual.east = new Cell();
                plane.setNear(EAST, actual.east);
            }
            actual.east.west = actual;
        } else if (direction == east && actual.west == null) {
            actual.west = plane.getNear(WEST);
            if (actual.west == null) {
                System.out.print("new ");
                actual.west = new Cell();
                plane.setNear(WEST, actual.west);
            }
            actual.west.east = actual;
        }
    }

    enum Direction {
        LEFT,
        RIGHT,
        FRONT,
        BACK
    }
}
