package matrix.cartesian;

public class Point implements Cloneable {
    public int x, y;


    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point clone() {
        return new Point(x, y);
    }
}
