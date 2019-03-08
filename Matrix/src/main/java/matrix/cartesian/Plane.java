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
                break;
            case Matrix.SOUTH:
                p.y--;
            case Matrix.EAST:
                p.x++;
            case Matrix.WEST:
                p.x--;
        }
    }

    public Point getPoint() {
        return p.clone();
    }

    public void insert(Cell c) {
        for (LinkedList<Cell> ics : matrixList) {

        }
//matrixList.get(x).set()
    }

    public Cell get() {
        return null;
    }

    public Cell getNear(int dir) {
        return null;
    }
}
