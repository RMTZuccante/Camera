package utils;

import camera.Camera;
import camera.Frame.Pair;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import json.Values;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static utils.Utils.RMTZ_LOGGER;
import static utils.Utils.setupLogger;

/**
 * Created by NicoTF
 */

public class CameraTest {
    private final static Logger logger = Logger.getLogger(RMTZ_LOGGER);

    public static void main(String[] args) {
        setupLogger(true);
        JsonObject config = null;
        Camera left = null, right = null;

        try {
            config = new JsonParser().parse(new JsonReader(new FileReader("config.json"))).getAsJsonObject();
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Cannot find config.json");
            System.exit(-1);
        }

        Values v = new Values(config);

        if (v.load()) {
            if (Camera.loadLib(v.libpath)) {
                left = new Camera(v.leftCameaId, v.minArea, v.maxAra, v.thresh, v.paddings, false);
                if (!left.open()) {
                    logger.log(Level.SEVERE, "Error opening left camera. index: " + v.leftCameaId);
                } else logger.info("Left camera opened");

                right = new Camera(v.rightCameraId, v.minArea, v.maxAra, v.thresh, v.paddings, true);
                if (!right.open()) {
                    logger.log(Level.SEVERE, "Error opening right camera. index: " + v.rightCameraId);
                } else logger.info("Right camera opened");
            } else {
                logger.log(Level.SEVERE, "Error loading lib, provided path may be wrong");
            }
        } else {
            logger.log(Level.SEVERE, "Error loading config.json");
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
}
