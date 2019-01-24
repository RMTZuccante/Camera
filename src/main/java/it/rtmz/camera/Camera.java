package it.rtmz.camera;

import org.apache.commons.lang3.SystemUtils;
import org.datavec.image.loader.NativeImageLoader;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Camera {

    private static boolean libLoaded = false;
    private VideoCapture cap = new VideoCapture();

    public Camera() {
    }

    public boolean loadLib(String path) {
        if (libLoaded) return true;
        if (path == null || path.length() == 0) {
            System.out.println("[INFO]: No lib path specified, searching in this directory");
            path = ".";
        }
        File dir = new File(path);
        if (!dir.isDirectory() || !dir.exists()) {
            System.err.println("The provided lib path is invalid");
            return false;
        }
        path = null;
        File[] files = dir.listFiles();
        if (SystemUtils.IS_OS_WINDOWS) {
            for (File f : files) {
                if (f.getName().matches("\\S*(opencv_java)\\d*(.dll)")) {
                    path = f.getAbsolutePath();
                    break;
                }
            }
        } else if (SystemUtils.IS_OS_LINUX) {
            for (File f : files) {
                if (f.getName().matches("\\S*(opencv_java)\\d*(.so)")) {
                    path = f.getAbsolutePath();
                    break;
                }
            }
        } else {
            System.err.println("OS not compatible yet");
            return false;
        }
        if (path == null) {
            System.err.println("Cannot find native library in specified path");
            return false;
        }
        System.load(path);
        libLoaded = true;
        return true;
    }

    public boolean open(int i) {
        if (!libLoaded) {
            System.err.println("[ERR]: Trying opening camera without loading library");
            return false;
        }
        return cap.open(i);
    }

    public boolean open(String fn) {
        if (!libLoaded) {
            System.err.println("[ERR]: Trying opening camera without loading library");
            return false;
        }
        return cap.open(fn);
    }

    public Frame capture() {
        if (!libLoaded) {
            System.err.println("[ERR]: Trying reading camera input without loading library");
            return null;
        }
        Frame f = new Frame();
        cap.read(f);
        return f;
    }

    private INDArray getInputImage(Mat img, Rect rect) {
        img = new Mat(img, rect);
        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
        Imgproc.resize(img, img, new Size(80, 80));
        NativeImageLoader loader = new NativeImageLoader(80, 80, 1);
        ImagePreProcessingScaler scaler = new ImagePreProcessingScaler();
        try {
            INDArray mat = loader.asMatrix(img);
            scaler.transform(mat);
            return mat;
        } catch (IOException e) {
            return null;
        }
    }

    private ArrayList<MatOfPoint> findShapes(Mat img, int min, int max) {
        Mat gray = new Mat();
        Mat thresh = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(gray, thresh, 80, 255, Imgproc.THRESH_BINARY);
        ArrayList<MatOfPoint> conts = new ArrayList<>();
        Imgproc.findContours(thresh, conts, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
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