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
        Pair<Character, Rect> pred = new Pair<>(null, null);
        Pair<ArrayList<MatOfPoint>, Mat> found = findShapes();
        double prob = 0;
        for (MatOfPoint c : found.first) {
            Rect bound = Imgproc.boundingRect(c);
            INDArray arr = getInputImage(found.second, bound);
            INDArray predict = cam.model.output(arr);
            if (predict.amax().getDouble(0) > cam.precision && predict.amax().getDouble(0) > prob) {
                prob = predict.amax().getDouble(0);
                pred.first = Camera.ref[predict.argMax().getInt(0)];
                pred.second = bound;
            }
        }
        return pred.first == null ? null : pred;
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

    private Pair<ArrayList<MatOfPoint>, Mat> findShapes() {
        Mat gray = new Mat();
        Mat threshold = new Mat();
        Imgproc.cvtColor(this, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(gray, threshold, cam.black, 255, Imgproc.THRESH_BINARY);

        ArrayList<MatOfPoint> conts = new ArrayList<>(5);
        ArrayList<MatOfPoint> candidates = new ArrayList<>(3);

        Imgproc.findContours(threshold, conts, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        if (cam.min != -1 && cam.max != -1) {
            for (MatOfPoint c : conts) {
                double area = Imgproc.contourArea(c);
                Rect r = Imgproc.boundingRect(c);
                if (r.height > r.width && area >= cam.min && area <= cam.max) candidates.add(c);
            }
        } else {
            for (MatOfPoint c : conts) {
                Rect r = Imgproc.boundingRect(c);
                if (r.height > r.width) candidates.add(c);
            }
        }
        return new Pair<>(candidates, gray);
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
