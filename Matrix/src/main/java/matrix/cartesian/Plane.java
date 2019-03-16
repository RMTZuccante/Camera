package matrix.cartesian;

import matrix.Cell;
import matrix.Matrix;

public class Plane {

    private Point p = new Point(0, 0);
    private LinkedList<LinkedList<Cell>> matrixList = new LinkedList<>(0, 0, 0);

    public Plane(Cell cell) {
        matrixList.set(new LinkedList<>(0, 0, 0));
        matrixList.get().set(cell);
    }

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
                break;
            case Matrix.EAST:
                p.x++;
                matrixList.moveTo(p.x);
                break;
            case Matrix.WEST:
                p.x--;
                matrixList.moveTo(p.x);
                break;
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


    public void setNear(int dir, Cell c) {
        switch (dir) {
            case Matrix.NORTH:
                matrixList.get().setAfter(c);
                break;
            case Matrix.SOUTH:
                matrixList.get().setBefore(c);
                break;
            case Matrix.EAST:
                matrixList.getAfter().set(c);
                break;
            case Matrix.WEST:
                matrixList.getBefore().set(c);
                break;
        }
    }

    public Cell getNear(int dir) {
        LinkedList<Cell> now = matrixList.get();
        Cell c = null;
        switch (dir) {
            case Matrix.NORTH:
                if (now.getTo() < p.y + 1) {
                    for (LinkedList<Cell> y : matrixList) {
                        y.addAfter();
                    }
                }
                c = now.getAfter();
                break;
            case Matrix.SOUTH:
                if (now.getFrom() > p.y - 1) {
                    for (LinkedList<Cell> x : matrixList) {
                        x.addBefore();
                    }
                }
                c = now.getBefore();
                break;
            case Matrix.EAST:
                if (matrixList.getTo() < p.x + 1) {
                    matrixList.addAfter();
                    matrixList.setAfter(new LinkedList<>(now.getFrom(), now.getTo(), now.getPos()));
                }
                c = matrixList.getAfter().get();
                break;
            case Matrix.WEST:
                if (matrixList.getFrom() > p.x - 1) {
                    matrixList.addBefore();
                    matrixList.setBefore(new LinkedList<>(now.getFrom(), now.getTo(), now.getPos()));
                }
                c = matrixList.getBefore().get();
                break;
        }
        return c;
    }
}
