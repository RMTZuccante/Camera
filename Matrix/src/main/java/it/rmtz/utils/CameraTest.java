package it.rmtz.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import it.rmtz.camera.Camera;
import javafx.util.Pair;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Nicol� Tagliaferro
 */

public class CameraTest {
    private static int cl, cr, thresh, minArea, maxAra, offset;
    private static char[] ref;
    private static double precision;

    public static void main(String[] args) {
        JsonObject config = null;
        Camera left = null, right = null;

        String lp = null;
        String modelpath = "./model/model.dl4j";
        if (args != null && args.length > 0) {
            int i = 0;
            try {
                for (i = 0; i < args.length; i++) {
                    if (args[i].equals("-lp")) {
                        lp = args[++i];
                    } else if (args[i].equals("-mp")) {
                        modelpath = args[++i];
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                System.err.println("Missing argument after [" + args[i - 1] + "]l");
                System.exit(-1);
            }
        }

        try {
            config = new JsonParser().parse(new JsonReader(new FileReader("config.json"))).getAsJsonObject();
        } catch (FileNotFoundException e) {
            System.err.println("Cannot find config.json");
            System.exit(-1);
        }

        if (getValuesFromJson(config)) {
            if (Camera.loadLib(lp)) {
                File f = new File(modelpath);
                MultiLayerNetwork model = null;
                if (f.exists() && f.canRead()) {
                    try {
                        model = ModelSerializer.restoreMultiLayerNetwork(f.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.err.println("\n\nError loading model:");
                        model = null;
                    }
                } else {
                    System.err.println("Missing model file");
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

        while (left.isOpened() || right.isOpened()) {
            if (left.isOpened()) {
                try {
                    left.capture();
                } catch (IOException e) {
                    left.close();
                    continue;
                }
                Pair<Character, Rect> a = left.getFrame().predictWithShape();
                if (a != null) {
                    Imgproc.rectangle(left.getFrame(), new Point(a.getValue().x, a.getValue().y), new Point(a.getValue().x + a.getValue().width, a.getValue().y + a.getValue().height), new Scalar(0, 255, 0));
                    Imgproc.putText(left.getFrame(), a.getKey() + "", new Point(a.getValue().x + a.getValue().width + 10, a.getValue().y + a.getValue().height + 10), 0, 0.8, new Scalar(0, 255, 0));
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
                    Imgproc.rectangle(right.getFrame(), new Point(a.getValue().x, a.getValue().y), new Point(a.getValue().x + a.getValue().width, a.getValue().y + a.getValue().height), new Scalar(0, 255, 0));
                    Imgproc.putText(right.getFrame(), a.getKey() + "", new Point(a.getValue().x + a.getValue().width + 10, a.getValue().y + a.getValue().height + 10), 0, 0.8, new Scalar(0, 255, 0));
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
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }
}
