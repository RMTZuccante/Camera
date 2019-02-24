package it.rmtz.matrix;

import it.rmtz.camera.Camera;

public class Matrix {
    final byte NORTH = 0, EAST = 1, SOUTH = 2, WEST = 3;
    Camera left, right;
    byte direction = NORTH;
    private MatrixConnector connector;
    private int maxWallDist;
    private Cell start, actual;
    private float bodyTemp;

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
            int[] distances = connector.getDistances();

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

    private void nextCell() {

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
}
