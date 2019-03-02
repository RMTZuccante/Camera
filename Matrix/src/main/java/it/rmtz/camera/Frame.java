package it.rmtz.camera;

import org.datavec.image.loader.NativeImageLoader;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;

public class Frame extends Mat {
    private Camera cam;

    protected Frame(Camera c) {
        cam = c;
    }

    public synchronized char predict() {
        Pair<Character, Rect> p = predictWithShape();
        return p == null ? 0 : p.first;
    }

    public synchronized Pair<Character, Rect> predictWithShape() {
        Pair<Character, Rect> pred = null;
        Mat threshold = new Mat();
        Imgproc.cvtColor(this, threshold, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(threshold, threshold, cam.black, 255, Imgproc.THRESH_BINARY);
        ArrayList<MatOfPoint> conts = findShapes(threshold);
        double prob = 0;
        for (int i = 0; i < conts.size(); i++) {
            MatOfPoint c = conts.get(i);
            Rect bound = Imgproc.boundingRect(c);
            INDArray arr = getInputImage(threshold, bound);
            INDArray predict = cam.model.output(arr);
            if (predict.amax().getDouble(0) > cam.precision && predict.amax().getDouble(0) > prob) {
                prob = predict.amax().getDouble(0);
                pred = new Pair<>(Camera.ref[predict.argMax().getInt(0)], bound);
            }
        }
        return pred;
    }

    private INDArray getInputImage(Mat img, Rect rect) {
        try {
            img = new Mat(img, new Rect(rect.x - cam.offset, rect.y - cam.offset, rect.height + cam.offset * 2, rect.width + cam.offset * 2));
        } catch (CvException e) {
            img = new Mat(img, rect);
        }
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

    private ArrayList<MatOfPoint> findShapes(Mat threshold) {
        ArrayList<MatOfPoint> conts = new ArrayList<>();
        Imgproc.findContours(threshold, conts, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        if (cam.min == -1 || cam.max == -1) return conts;
        else {
            ArrayList<MatOfPoint> inRange = new ArrayList<>();
            for (MatOfPoint c : conts) {
                double area = Imgproc.contourArea(c);
                if (area >= cam.min && area <= cam.max) inRange.add(c);
            }
            return inRange;
        }
    }

    public class Pair<A, B> {
        public A first;
        public B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }
    }
}
