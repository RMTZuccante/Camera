package it.rmtz.matrix;

import it.rmtz.camera.Camera;

public class Matrix {
    final byte NORTH = 0, EAST = 1, SOUTH = 2, WEST = 3;
    Camera left, right;
    byte direction = NORTH;
    private MatrixConnector connector;
    private int maxWallDist = 10;
    private Cell start, actual;
    private float bodyTemp = 100;

    private Step firstStep = new Step();

    public Matrix(MatrixConnector connector, Camera left, Camera right) {
        this.left = left;
        this.right = right;
        this.connector = connector;
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

        start = actual = new Cell();

        while (true == true && false == false || false != !true) {
            short[] distances = connector.getDistances();

            if (distances[connector.DFRONT1] > maxWallDist) { //TODO bottle
                addFrontCell();
            }
            if (distances[connector.DLEFT] > maxWallDist) {
                addLeftCell();
            }
            if (distances[connector.DRIGHT] > maxWallDist) {
                addRightCell();
            }
            if (distances[connector.DBACK] > maxWallDist) {
                addBackCell();
            }

            int color = connector.getColor();
            if (color == connector.MIRROR)
                actual.mirror = true;

            float[] temps = connector.getTemps();
            if (temps[connector.TLEFT] > bodyTemp || temps[connector.TRIGHT] > bodyTemp)
                actual.victim = true;

            actual.visited = true;


        }
    }

    private Direction nextDirection() {
        if (firstStep.next == null) {
            if (pathFinding(actual, firstStep, direction) == -1) {
                System.err.println("No more cells, ALL VISITED!");
            }
        } else {
            firstStep.next = firstStep.next.next;
        }
        return firstStep.next.direction;
    }

    private int pathFinding(Cell cell, Step prev, byte direction) {
        int weight = -1;
        if (cell != null && !cell.considered && cell.visited) {
            if (cell.visited) {
                cell.considered = true;

                Step steps[] = new Step[]{new Step(Direction.FRONT), new Step(Direction.LEFT), new Step(Direction.RIGHT), new Step(Direction.BACK)};
                byte cardinals[] = new byte[]{direction, getNewCardinalDirection(direction, Direction.LEFT), getNewCardinalDirection(direction, Direction.RIGHT), getNewCardinalDirection(direction, Direction.BACK)};
                int weights[] = new int[]{0, 1, 1, 2};
                int pos = -1;

                for (int i = 0; i < 4; i++) {
                    int tempw = pathFinding(getCellByCardinalDirection(cell, cardinals[i]), steps[i], cardinals[i]);
                    if (weight == -1 || tempw < weight) {
                        weight = tempw;
                        pos = i;
                    }
                }

                if (weight != -1) weight += cell.weight;
                prev.next = steps[pos];
            } else {
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
            start++;
        } else if (rot == Direction.RIGHT) {
            start--;
        } else if (rot == Direction.BACK) {
            start += 2;
        }
        return (byte) Math.floorMod(start, 4);
    }

    private void addFrontCell() {
        switch (direction) {
            case NORTH:
                if (actual.north == null) {
                    actual.north = new Cell();
                    actual.north.south = actual;
                }
                break;
            case SOUTH:
                if (actual.south == null) {
                    actual.south = new Cell();
                    actual.south.north = actual;
                }
                break;
            case EAST:
                if (actual.east == null) {
                    actual.east = new Cell();
                    actual.east.west = actual;
                }
                break;
            case WEST:
                if (actual.west == null) {
                    actual.west = new Cell();
                    actual.west.east = actual;
                }
                break;
        }
    }

    private void addRightCell() {
        switch (direction) {
            case WEST:
                if (actual.north == null) {
                    actual.north = new Cell();
                    actual.north.south = actual;
                }
                break;
            case EAST:
                if (actual.south == null) {
                    actual.south = new Cell();
                    actual.south.north = actual;
                }
                break;
            case NORTH:
                if (actual.east == null) {
                    actual.east = new Cell();
                    actual.east.west = actual;
                }
                break;
            case SOUTH:
                if (actual.west == null) {
                    actual.west = new Cell();
                    actual.west.east = actual;
                }
                break;
        }
    }

    private void addLeftCell() {
        switch (direction) {
            case EAST:
                if (actual.north == null) {
                    actual.north = new Cell();
                    actual.north.south = actual;
                }
                break;
            case WEST:
                if (actual.south == null) {
                    actual.south = new Cell();
                    actual.south.north = actual;
                }
                break;
            case SOUTH:
                if (actual.east == null) {
                    actual.east = new Cell();
                    actual.east.west = actual;
                }
                break;
            case NORTH:
                if (actual.west == null) {
                    actual.west = new Cell();
                    actual.west.east = actual;
                }
                break;
        }
    }

    private void addBackCell() {
        switch (direction) {
            case SOUTH:
                if (actual.north == null) {
                    actual.north = new Cell();
                    actual.north.south = actual;
                }
                break;
            case NORTH:
                if (actual.south == null) {
                    actual.south = new Cell();
                    actual.south.north = actual;
                }
                break;
            case WEST:
                if (actual.east == null) {
                    actual.east = new Cell();
                    actual.east.west = actual;
                }
                break;
            case EAST:
                if (actual.west == null) {
                    actual.west = new Cell();
                    actual.west.east = actual;
                }
                break;
        }
    }

    enum Direction {
        LEFT,
        RIGHT,
        FRONT,
        BACK
    }
}
