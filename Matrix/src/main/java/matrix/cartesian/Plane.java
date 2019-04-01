package matrix.cartesian;

import matrix.Cell;
import matrix.Matrix;

public class Plane {

    private Point p = new Point(0, 0); // p.x = matrixList.pos, p.y = matrixList.get().pos
    private LinkedList<LinkedList<Cell>> matrixList = new LinkedList<>(0, 0, 0);

    public Plane(Cell cell) {
        matrixList.set(new LinkedList<>(0, 0, 0));
        matrixList.get().set(cell);
    }

    public void move(byte direction) {
        switch (direction) {
            case Matrix.NORTH:
                p.y++;
                for (LinkedList<Cell> y: matrixList) {
                    y.moveTo(p.y);
                }
                break;
            case Matrix.SOUTH:
                p.y--;
                for (LinkedList<Cell> y : matrixList) {
                    y.moveTo(p.y);
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
                if (now.getPositiveBound() < p.y + 1) {
                    for (LinkedList<Cell> y : matrixList) {
                        y.addTop();
                    }
                }
                c = now.getAfter();
                break;
            case Matrix.SOUTH:
                if (now.getNegativeBound() > p.y - 1) {
                    for (LinkedList<Cell> y : matrixList) {
                        y.addBottom();
                    }
                }
                c = now.getBefore();
                break;
            case Matrix.EAST:
                if (matrixList.getPositiveBound() < p.x + 1) {
                    matrixList.addTop();
                    matrixList.setAfter(new LinkedList<>(now.getNegativeBound(), now.getPositiveBound(), now.getPos()));
                }
                c = matrixList.getAfter().get();
                break;
            case Matrix.WEST:
                if (matrixList.getNegativeBound() > p.x - 1) {
                    matrixList.addBottom();
                    matrixList.setBefore(new LinkedList<>(now.getNegativeBound(), now.getPositiveBound(), now.getPos()));
                }
                c = matrixList.getBefore().get();
                break;
        }
        return c;
    }
}
