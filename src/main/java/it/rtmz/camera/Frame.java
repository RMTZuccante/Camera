package it.rtmz.camera;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;

public class Frame extends Mat {
    private Camera cam;

    protected Frame(Camera c) {
        cam = c;
    }

    public char predict() {
        Mat copy = this.clone();
        ArrayList<MatOfPoint> conts = findShapes(500, 6000);
        char pred = 0;
        double prob = 0;
        for (int i = 0; i < conts.size(); i++) {
            MatOfPoint c = conts.get(i);
            Rect bound = Imgproc.boundingRect(c);
            INDArray arr = getInputImage(copy, bound);
            INDArray predict = cam.model.output(arr);
            if (predict.amax().getDouble(0) > 0.9999 && predict.amax().getDouble(0) > prob) {
                prob = predict.amax().getDouble(0);
                pred = Camera.ref[predict.argMax().getInt(0)];
            }
        }
        return pred;
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

    private ArrayList<MatOfPoint> findShapes(int min, int max) {
        Mat gray = new Mat();
        Mat thresh = new Mat();
        Imgproc.cvtColor(this, gray, Imgproc.COLOR_BGR2GRAY);
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
