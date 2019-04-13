package camera;

import org.apache.commons.lang3.SystemUtils;
import org.opencv.core.Rect;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Camera {

    private static boolean libLoaded = false;
    private static Logger logger = Logger.getLogger(Camera.class.getName());
    int min, max, black;
    int[] paddings;
    private Frame frame = new Frame(this);
    private VideoCapture cap;
    private int camId;
    private Rect portion;
    private boolean invertPadding;

    public Camera(int camId, int min, int max, int black, int[] paddings, boolean invertPadding) {
        this.max = max;
        this.min = min;
        this.black = black;
        this.paddings = paddings;
        this.camId = camId;
        this.invertPadding = invertPadding;
        cap = new VideoCapture(camId);
    }

    public static boolean isLibLoaded() {
        return libLoaded;
    }

    public static boolean loadLib(String path) {
        if (libLoaded) return true;
        if (path == null || path.length() == 0) {
            logger.info("[INFO]: No lib path specified, searching in this directory");
            path = ".";
        }
        File dir = new File(path);
        if (!dir.isDirectory() || !dir.exists()) {
            logger.log(Level.SEVERE, "The provided lib path is invalid");
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
            logger.log(Level.SEVERE, "OS not compatible yet");
            return false;
        }
        if (path == null) {
            logger.log(Level.SEVERE, "Cannot find native library in specified path");
            return false;
        }
        System.load(path);
        libLoaded = true;
        return true;
    }

    public Frame getFrame() {
        return frame;
    }

    public boolean open() {
        if (!libLoaded) {
            logger.log(Level.SEVERE, "[ERR]: Trying opening camera without loading library");
            return false;
        }
        return cap.open(camId);
    }

    public boolean isOpened() {
        return cap.isOpened();
    }

    public void close() {
        cap.release();
    }

    public Frame capture() throws IOException {
        if (!libLoaded) {
            logger.log(Level.SEVERE, "[ERR]: Trying reading camera input without loading library");
            return null;
        } else if (!cap.isOpened()) {
            logger.log(Level.SEVERE, "[ERR]: Trying reading camera without opening it");
            return null;
        }
        if (!cap.read(frame)) {
            throw new IOException("Camera may be disconnected");
        }
        /*Core.rotate(frame, frame, Core.ROTATE_180);
        if (portion == null) {
            if (!invertPadding)
                portion = new Rect(paddings[3], paddings[0], frame.width() - paddings[1] - paddings[3], frame.height() - paddings[0] - paddings[2]);
            else
                portion = new Rect(paddings[1], paddings[0], frame.width() + paddings[1] - paddings[3], frame.height() - paddings[0] - paddings[2]);
        }
        frame = new Frame(frame, portion, this);*/
        return frame;
    }
}