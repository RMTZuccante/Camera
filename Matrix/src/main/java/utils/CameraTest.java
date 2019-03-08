package utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import camera.Camera;
import camera.Frame.Pair;
import camera.ModelLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by NicoTF
 */

public class CameraTest {
    private static int cl, cr, thresh, minArea, maxAra, offset;
    private static String libpath;
    private static char[] ref;
    private static double precision;

    public static void main(String[] args) {
        JsonObject config = null;
        Camera left = null, right = null;

        String modelpath = "./model/model.dl4j";

        try {
            config = new JsonParser().parse(new JsonReader(new FileReader("config.json"))).getAsJsonObject();
        } catch (FileNotFoundException e) {
            System.err.println("Cannot find config.json");
            System.exit(-1);
        }

        if (getValuesFromJson(config)) {
            if (Camera.loadLib(libpath)) {
                MultiLayerNetwork model = null;
                try {
                    model = ModelLoader.loadModel(modelpath);
                } catch (ModelLoader.ModelLoaderException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }

                if (model != null) {
                    left = new Camera(model, ref, minArea, maxAra, thresh, offset, precision);
                    right = new Camera(model, ref, minArea, maxAra, thresh, offset, precision);

                    if (!left.open(cl)) {
                        System.err.println("Error opening left camera. index: " + cl);
                    }

                    if (!right.open(cr)) {
                        System.err.println("Error opening right camera. index: " + cr);
                    }
                }
            } else {
                System.err.println("Error loading lib, provided path may be wrong");
            }
        } else {
            System.err.println("Error loading config.json");
        }

        if (left != null) while (left.isOpened() || right.isOpened()) {
            if (left.isOpened()) {
                try {
                    left.capture();
                } catch (IOException e) {
                    left.close();
                    continue;
                }
                Pair<Character, Rect> a = left.getFrame().predictWithShape();
                if (a != null) {
                    Imgproc.rectangle(left.getFrame(), new Point(a.second.x, a.second.y), new Point(a.second.x + a.second.width, a.second.y + a.second.height), new Scalar(0, 255, 0));
                    Imgproc.putText(left.getFrame(), a.first + "", new Point(a.second.x + a.second.width + 10, a.second.y + a.second.height + 10), 0, 0.8, new Scalar(0, 255, 0));
                }
                HighGui.imshow("Left", left.getFrame());
            }
            if (right.isOpened()) {
                try {
                    right.capture();
                } catch (IOException e) {
                    right.close();
                    continue;
                }
                Pair<Character, Rect> a = right.getFrame().predictWithShape();
                if (a != null) {
                    Imgproc.rectangle(right.getFrame(), new Point(a.second.x, a.second.y), new Point(a.second.x + a.second.width, a.second.y + a.second.height), new Scalar(0, 255, 0));
                    Imgproc.putText(right.getFrame(), a.first + "", new Point(a.second.x + a.second.width + 10, a.second.y + a.second.height + 10), 0, 0.8, new Scalar(0, 255, 0));
                }
                HighGui.imshow("Right", right.getFrame());
            }
            if (HighGui.waitKey(1) == 'Q') break;
        }
        left.close();
        right.close();
        System.exit(0);
    }

    private static boolean getValuesFromJson(JsonObject obj) {
        try {
            cl = obj.get("CAMERA_LEFT").getAsInt();
            cr = obj.get("CAMERA_RIGHT").getAsInt();
            JsonArray jsonRef = obj.get("ref").getAsJsonArray();
            ref = new char[jsonRef.size()];
            for (int i = 0; i < ref.length; i++) {
                ref[i] = jsonRef.get(i).getAsCharacter();
            }
            thresh = obj.get("THRESH").getAsInt();
            minArea = obj.get("MIN_AREA").getAsInt();
            maxAra = obj.get("MAX_AREA").getAsInt();
            offset = obj.get("OFFSET").getAsInt();
            precision = obj.get("PRECISION").getAsDouble();
            libpath = obj.get("LIBPATH").getAsString();
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }
}
