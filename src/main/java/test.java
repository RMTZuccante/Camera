import it.rtmz.Imshow;
import org.apache.commons.lang3.SystemUtils;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class test {
    public static boolean loadLib(String path) {
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
        return true;
    }

    public static void main(String[] args) throws IOException {
        if (!loadLib("C:\\opencv\\build\\java\\x64")) System.exit(-1);
        VideoCapture cam = new VideoCapture();
        cam.open(0);
        Scanner tas = new Scanner(System.in);
        File f = new File("model.dl4j");
        MultiLayerNetwork model = null;
        if (f.exists() && f.canRead()) model = ModelSerializer.restoreMultiLayerNetwork(f.getAbsolutePath());
        else {
            System.err.println("No model file");
            System.exit(-1);
        }
        model.init();
        //MultiLayerNetwork model = KerasModelImport.importKerasSequentialModelAndWeights(ClassLoader.getSystemClassLoader().getResource("model.h5").getPath().substring(1));

        char ref[] = new char[]{'S', 'H', 'U'};
        Mat img = new Mat();
        Imshow imshow = new Imshow("Camera");
        while (cam.isOpened() && cam.read(img)) {
            Mat copy = img.clone();
            ArrayList<MatOfPoint> conts = findShapes(img, 500, 6000);
            for (int i = 0; i < conts.size(); i++) {
                MatOfPoint c = conts.get(i);
                Rect bound = Imgproc.boundingRect(c);
                INDArray arr = getInputImage(copy, bound);
                INDArray predict = model.output(arr);
                if (predict.amax().getDouble(0) > 0.9999) {
                    Imgproc.rectangle(img, new Point(bound.x, bound.y), new Point(bound.x + bound.width, bound.y + bound.height), new Scalar(0, 255, 0), 2);
                    Imgproc.putText(img, ref[predict.argMax().getInt(0)] + " " + predict.amax().getDouble(0) * 100 + "%", new Point(bound.x + bound.width + 10, bound.y + bound.height), 0, 1, new Scalar(0, 0, 255));
                }
            }
            imshow.showImage(img);
        }
        System.out.println("Exit");
    }

    static INDArray getInputImage(Mat img, Rect rect) {
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


    static ArrayList<MatOfPoint> findShapes(Mat img, int min, int max) {
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