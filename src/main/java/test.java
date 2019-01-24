import it.rtmz.camera.Camera;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.Scanner;

public class test {
    public static void main(String[] args) {
        Camera cam = new Camera();
        cam.loadLib();

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
}
