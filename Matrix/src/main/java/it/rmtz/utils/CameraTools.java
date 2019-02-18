package it.rmtz.utils;

import it.rmtz.camera.Camera;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class CameraTools {

    public synchronized static void main(String[] args) {
        Camera.loadLib("C:\\opencv\\build\\java\\x64");
        VideoCapture cap = new VideoCapture();
        cap.open(0);
        JFrame controls = new JFrame("Controlli");
        controls.setSize(500, 200);
        controls.setLayout(new FlowLayout());
        JSlider black = new JSlider(0, 255, 80);
        JLabel blackValue = new JLabel(Integer.toString(black.getValue()));
        controls.add(black);
        controls.add(blackValue);
        JSpinner maxArea = new JSpinner(new SpinnerNumberModel(6000, 1, 921600, 10));
        JSpinner minArea = new JSpinner(new SpinnerNumberModel(500, 1, 921600, 10));
        controls.add(minArea);
        controls.add(maxArea);

        controls.setVisible(true);
        black.addChangeListener(e -> {
            blackValue.setText(Integer.toString(black.getValue()));
        });
        Mat frame = new Mat();
        while (cap.isOpened() && cap.read(frame)) {
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGB2GRAY);
            Imgproc.threshold(frame, frame, black.getValue(), 255, Imgproc.THRESH_BINARY);
            ArrayList<MatOfPoint> shapes = findShapes(frame, (Integer) minArea.getValue(), (Integer) maxArea.getValue());
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_GRAY2BGR);
            Imgproc.drawContours(frame, shapes, -1, new Scalar(0, 0, 255), 3);
            HighGui.imshow("BRUCIA!", frame);
            HighGui.waitKey(1);
        }
    }

    private static ArrayList<MatOfPoint> findShapes(Mat img, int min, int max) {
        ArrayList<MatOfPoint> conts = new ArrayList<>();
        Imgproc.findContours(img, conts, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        if (min == -1 || max == -1) return conts;
        else {
            ArrayList<MatOfPoint> inRange = new ArrayList<>();
            for (MatOfPoint c : conts) {
                double area = Imgproc.contourArea(c);
                if (area >= min && area <= max) inRange.add(c);
            }
            return inRange;
        }
    }
}
