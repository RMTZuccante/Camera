package matrix;

import camera.Camera;
import camera.Frame;
import matrix.SerialConnector.*;
import matrix.cartesian.Plane;
import matrix.cell.Cell;
import matrix.cell.Cell.Victim;
import matrix.cell.RisingCell;
import org.opencv.core.Rect;

import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static matrix.SerialConnector.*;
import static utils.Utils.RMTZ_LOGGER;

public class Matrix {
    private final static Logger logger = Logger.getLogger(RMTZ_LOGGER);
    public final static byte NORTH = 0, EAST = 1, SOUTH = 2, WEST = 3;
    private static final int[] weights = new int[]{0, 1, 1, 2}; //Constant weights for pathfinding
    private Camera left, right;
    private byte direction;
    private SerialConnector connector;
    private int maxWallDist;
    private Cell start, actual;
    private float bodyTemp;
    private Plane plane;
    private Cell lastMirror;
    private Frame.Pair<Victim, Camera> foundVictim = null;
    private Random random = new Random();
    private boolean wasclimbing = false;

    private Step firstStep = new Step();

    private Runnable camera = new Runnable() {
        @Override
        public void run() {
            int ril = 2;
            foundVictim = null;
            boolean r = false, l = false;
            try {
                Thread.sleep(750);
            } catch (InterruptedException e) {
            }
            char v = 0;
            double area = 0;
            int count = 0;
            Camera cam = null;
            Frame.Pair<Character, Rect> pred;
            while (!Thread.interrupted() && (!l || !r)) {
                if (left != null && left.isOpened()) {
                    try {
                        pred = left.capture().predictWithShape();
                        char c = pred != null ? pred.first : 0;
                        if (c == v) {
                            if (pred.second.area() > area) {
                                count++;
                                area = pred.second.area();
                                if (count > 3) {
                                    break;
                                }
                            }
                        } else if (c != 0) {
                            count = 0;
                            cam = left;
                            v = c;
                            area = pred.second.area();
                        }
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Error getting frame from left camera");
                        left.close();
                        left = null;
                    } catch (IllegalArgumentException e) {
                        v = 0;
                    }
                } else l = false;
                if (right != null && right.isOpened()) {
                    try {
                        pred = right.capture().predictWithShape();
                        char c = pred != null ? pred.first : 0;
                        if (c == v) {
                            if (pred.second.area() > area) {
                                count++;
                                area = pred.second.area();
                                if (count > 3) {
                                    break;
                                }
                            }
                        } else if (c != 0) {
                            count = 0;
                            cam = left;
                            v = c;
                            area = pred.second.area();
                        }
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Error getting frame from right camera");
                        right.close();
                        right = null;
                    } catch (IllegalArgumentException e) {
                        v = 0;
                    }
                } else r = false;
            }
            if (v != 0) foundVictim = new Frame.Pair<>(Victim.valueOf("" + v), cam);
        }
    };

    public Matrix(SerialConnector connector, Camera left, Camera right, int maxWallDist, float bodyTemp) {
        this.left = left;
        this.right = right;
        this.connector = connector;
        this.maxWallDist = maxWallDist;
        this.bodyTemp = bodyTemp;
        direction = NORTH;
    }

    public void start(byte debugLevel, byte black) {
        while (!connector.handShake()) {
            System.out.println("hand");
            /*if (++i > 10) {
                i = 0;
                logger.info("10 handshake failed attemps, resetting STM");
                connector.reset();
            }*/
            logger.log(Level.SEVERE, "Handshake failed!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
        connector.setDebug(debugLevel);
        connector.setBlackThreshold(black);

        start = actual = lastMirror = new Cell();
        plane = new Plane(start);

        for (int i = 0; i < random.nextInt(10); i++) random.nextBoolean();

        Thread t = null;

        while (true) {
            t = new Thread(camera);
            boolean victimfound = actual.getVictim() != Victim.NONE;
            inspectCell();
            Direction dir = nextDirection();
            if (dir != null) {
                t.start();
                switch (dir) {
                    case BACK:
                        logger.info("Go back");
                        connector.rotate(90);
                        direction = getNewCardinalDirection(direction, Direction.RIGHT);
                        inspectCell();
                        connector.rotate(90);
                        direction = getNewCardinalDirection(direction, Direction.RIGHT);
                        break;
                    case LEFT:
                        logger.info("Go left");
                        connector.rotate(-90);
                        direction = getNewCardinalDirection(direction, Direction.LEFT);
                        inspectCell();
                        break;
                    case RIGHT:
                        logger.info("Go right");
                        connector.rotate(90);
                        direction = getNewCardinalDirection(direction, Direction.RIGHT);
                        inspectCell();
                        break;
                    case FRONT:
                        logger.info("Go straight");
                        break;

                }
                t.interrupt();
                if (actual.getVictim() != Victim.NONE && !victimfound) {
                    int packages = 0;

                    if (actual.getVictim() == Victim.HEAT || actual.getVictim() == Victim.S) {
                        packages = 1;
                    } else if (actual.getVictim() == Victim.H) {
                        packages = 2;
                    }
                    connector.victim(packages);
                }


                logger.info("\tGo!");
                go(true);
                t = new Thread(camera);
                t.start();
                int goret = connector.go();
                t.interrupt();
                if (goret == GOBLACK) {
                    wasclimbing = false;
                    actual.setBlack(true);
                    firstStep.next = null;
                    go(false);
                } else if (goret == GOOBSTACLE) {
                    wasclimbing = false;
                    actual.weight = 10;
                } else if (goret == GORISE) {
                    if (wasclimbing) {
                        logger.log(Level.SEVERE, "Fake end of rise detected");
                        actual.unset();
                        plane.remove();
                        go(false);
                        if (actual instanceof RisingCell) logger.log(Level.SEVERE, "\tSolved");
                        else logger.log(Level.SEVERE, "\tNot solved, Nico fix it!");
                    }

                    if (actual instanceof RisingCell) {
                        logger.info("Changed floor");
                    } else {
                        actual = new RisingCell(actual, plane);
                        logger.info("Changed to NEW floor");
                    }
                    wasclimbing = true;
                    firstStep.next = null;
                    addFrontCell();
                    go(true);
                } else wasclimbing = false;
            } else {
                logger.info("Finished! MISSION COMPLETED!");
                break;
            }
        }
    }

    private void inspectCell() {
        Distances distances = connector.getDistances();
        if (foundVictim != null) logger.info("Found Victim! " + foundVictim);


        Temps temps = connector.getTemps();

        /*If there are no walls on some directions add a cell beside the actual one*/
        if (distances.getFrontL() > maxWallDist) { //TODO bottle
            addFrontCell();
            logger.info("front cell: " + distances.getFrontL());
        }
        if (distances.getLeft() > maxWallDist) {
            addLeftCell();
            if (foundVictim != null && foundVictim.second == left) {
                actual.setVictim(foundVictim.first);
                logger.info("Found victim " + foundVictim.second);
            } else if (isVictim(temps.getLeft(), temps.getAmbient())) actual.setVictim(Victim.HEAT);
            logger.info("left cell: " + distances.getLeft());
        }
        if (distances.getRight() > maxWallDist) {
            addRightCell();
            if (foundVictim != null && foundVictim.second == right) {
                actual.setVictim(foundVictim.first);
                logger.info("Found victim " + foundVictim.second);
            } else if (isVictim(temps.getRight(), temps.getAmbient())) actual.setVictim(Victim.HEAT);
            logger.info("right cell: " + distances.getRight());
        }
        if (distances.getBack() > maxWallDist) {
            addBackCell();
            logger.info("back cell: " + distances.getBack());
        }

        actual.setMirror(isMirror());
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


    private boolean isVictim(float temp, float ambient) {
        // TODO improve detections rules
        return (temp) > bodyTemp;
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
                    logger.log(Level.SEVERE, "No more cells, ALL VISITED!, going back to home");
                    start.visited = false;
                    if (pathFinding(actual, firstStep, direction) == -1) {
                        logger.log(Level.SEVERE, "Failed pathfinding to home! ERROR!");
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
        if (cell != null && !cell.considered && !cell.isBlack()) { //If the cell doesn't exists (wall), it has already been considered or is black return the weight (-1), else calculate a path
            if (cell.visited) { //If the cell is already visited keep looking
                cell.considered = true; //Mark the cell as considered
                Step[] steps;

                if (random.nextBoolean())
                    steps = new Step[]{new Step(Direction.FRONT), new Step(Direction.RIGHT), new Step(Direction.LEFT), new Step(Direction.BACK)}; //Directions ordered to check the path
                else
                    steps = new Step[]{new Step(Direction.FRONT), new Step(Direction.LEFT), new Step(Direction.RIGHT), new Step(Direction.BACK)}; //Directions ordered to check the path

                byte[] cardinals = new byte[]{direction, getNewCardinalDirection(direction, steps[1].direction), getNewCardinalDirection(direction, steps[2].direction), getNewCardinalDirection(direction, Direction.BACK)}; //Set cardinal directions
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
        if (dir == NORTH) c = cell.getNorth();
        else if (dir == SOUTH) c = cell.getSouth();
        else if (dir == EAST) c = cell.getEast();
        else c = cell.getWest();
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
        if (direction == south && actual.getNorth() == null) {
            actual.setNorth(plane.getNear(NORTH));
            if (actual.getNorth() == null) {
                System.out.print("new ");
                actual.setNorth(new Cell());
                plane.setNear(NORTH, actual.getNorth());
            }
            actual.getNorth().setSouth(actual);
        } else if (direction == north && actual.getSouth() == null) {
            actual.setSouth(plane.getNear(SOUTH));
            if (actual.getSouth() == null) {
                System.out.print("new ");
                actual.setSouth(new Cell());
                plane.setNear(SOUTH, actual.getSouth());
            }
            actual.getSouth().setNorth(actual);
        } else if (direction == west && actual.getEast() == null) {
            actual.setEast(plane.getNear(EAST));
            if (actual.getEast() == null) {
                System.out.print("new ");
                actual.setEast(new Cell());
                plane.setNear(EAST, actual.getEast());
            }
            actual.getEast().setWest(actual);
        } else if (direction == east && actual.getWest() == null) {
            actual.setWest(plane.getNear(WEST));
            if (actual.getWest() == null) {
                System.out.print("new ");
                actual.setWest(new Cell());
                plane.setNear(WEST, actual.getWest());
            }
            actual.getWest().setEast(actual);
        }
    }

    private Direction not(Direction d) {
        Direction dir = null;
        switch (d) {
            case BACK:
                dir = Direction.FRONT;
                break;
            case FRONT:
                dir = Direction.BACK;
                break;
            case LEFT:
                dir = Direction.RIGHT;
                break;
            case RIGHT:
                dir = Direction.LEFT;
                break;
        }
        return dir;
    }

    enum Direction {
        LEFT,
        RIGHT,
        FRONT,
        BACK
    }
}
