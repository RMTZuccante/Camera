package camera;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class Frame extends Mat {
    private Camera cam;

    protected Frame(Camera c) {
        cam = c;
    }

    protected Frame(Mat m, Rect dim, Camera c) {
        super(m, dim);
        cam = c;
    }

    public synchronized char predict() {
        Pair<Character, Rect> p = predictWithShape();
        return p == null ? 0 : p.first;
    }

    public synchronized Pair<Character, Rect> predictWithShape() {
        char character = 0;
        Pair<ArrayList<MatOfPoint>, Mat> shapes = findShapes();
        for (MatOfPoint cont : shapes.first) {
            Rect bounding = Imgproc.boundingRect(cont);
            double area = Imgproc.contourArea(cont);
            if (bounding.height > bounding.width && area >= cam.min && area <= cam.max) {
                bounding.height *= 0.6;
                Mat cropped = new Mat(shapes.second, bounding);
                Mat edges = new Mat();
                Imgproc.Canny(cropped, edges, 75, 150);
                Mat lines = new Mat();
                Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180, 30, 0, 250);
                Mat corners = new Mat();
                Imgproc.cornerHarris(edges, corners, 2, 3, 0.04);
                if (corners.rows() > 70) {
                    // S 2 U 4 H 6
                    int r = lines.rows();
                    if (r > 5 && r < 10) character = 'H';
                    else if (r > 3 && r < 6) character = 'U';
                    else if (r < 3 && r > 0) character = 'S';
                }
                if (character != 0) {
                    return new Pair<>(character, bounding);
                }
            }
        }
        return null;
    }

    private Pair<ArrayList<MatOfPoint>, Mat> findShapes() {
        Mat gray = new Mat();
        Imgproc.cvtColor(this, gray, Imgproc.COLOR_BGR2GRAY);
        Mat threshold = new Mat();
        Imgproc.threshold(gray, threshold, cam.black, 225, Imgproc.THRESH_BINARY);
        ArrayList<MatOfPoint> contours = new ArrayList<>(2);
        Imgproc.findContours(threshold, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        return new Pair<>(contours, gray);

    }

    public static class Pair<A, B> {
        public A first;
        public B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }
    }
}
