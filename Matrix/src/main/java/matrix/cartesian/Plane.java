package matrix.cartesian;

import matrix.Cell;
import matrix.Matrix;

public class Plane {

    private Point p = new Point(0, 0);
    private LinkedList<LinkedList<Cell>> matrixList = new LinkedList<>();

    public void move(int direction) {
        switch (direction) {
            case Matrix.NORTH:
                p.y++;
                for (LinkedList<Cell> x : matrixList) {
                    x.moveTo(p.y);
                }
                break;
            case Matrix.SOUTH:
                p.y--;
                for (LinkedList<Cell> x : matrixList) {
                    x.moveTo(p.y);
                }
            case Matrix.EAST:
                p.x++;
                matrixList.moveTo(p.x);
            case Matrix.WEST:
                p.x--;
                matrixList.moveTo(p.x);
        }
    }

    public Point getPoint() {
        return p.clone();
    }

    public void insert(Cell c) {
        matrixList.get().set(c);
    }

    public Cell get() {
        return matrixList.get().get();
    }

    public Cell getNear(int dir) {
        switch (dir) {
            case Matrix.NORTH:
                p.y++;
                if (now.getAfter() == null) {
                    for (LinkedList<Cell> x : matrixList) {
                        x.setAfter(null);
                    }
                }
                for (LinkedList<Cell> x : matrixList) {
                    x.moveTo(p.y);
                }
                break;
            case Matrix.SOUTH:
                p.y--;
                if (now.getBefore() == null) {
                    for (LinkedList<Cell> x : matrixList) {
                        x.setBefore(null);
                    }
                }
                for (LinkedList<Cell> x : matrixList) {
                    x.moveTo(p.y);
                }
            case Matrix.EAST:
                p.x++;
                if (matrixList.getAfter() == null) {
                    matrixList.setAfter(new LinkedList<>(now.getFrom(), now.getTo(), now.getPos()));
                }
                matrixList.moveTo(p.x);
            case Matrix.WEST:
                p.x--;
                if (matrixList.getBefore() == null) {
                    matrixList.setBefore(new LinkedList<>(now.getFrom(), now.getTo(), now.getPos()));
                }
                matrixList.moveTo(p.x);
        }
    }
}
