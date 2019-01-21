import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Camera {
    public static void main(String[] args) throws IOException {
        System.load(ClassLoader.getSystemClassLoader().getResource("opencv_java401.dll").getFile());

        VideoCapture cam = new VideoCapture();
        cam.open(0);
        Scanner tas = new Scanner(System.in);

        MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(ClassLoader.getSystemClassLoader().getResource("model.dl4j").getPath().substring(1));
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
                if (predict.amax().getDouble(0) > 0.999) {
                    Imgproc.rectangle(img, new Point(bound.x, bound.y), new Point(bound.x + bound.width, bound.y + bound.height), new Scalar(0, 255, 0), 2);
                    Imgproc.putText(img, ref[predict.argMax().getInt(0)] + "", new Point(bound.x + bound.width + 10, bound.y + bound.height), 0, 1, new Scalar(0, 0, 255));
                }
                imshow.showImage(img);
            }
        }
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
