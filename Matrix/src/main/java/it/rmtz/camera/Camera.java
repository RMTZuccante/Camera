package it.rmtz.camera;

import org.apache.commons.lang3.SystemUtils;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.io.IOException;

public class Camera {

    static char[] ref = null;
    private static boolean libLoaded = false;
    int min, max, black, offset;
    double precision;
    MultiLayerNetwork model;
    private Frame frame = new Frame(this);
    private VideoCapture cap = new VideoCapture();

    public Camera(MultiLayerNetwork model, char[] ref, int min, int max, int black, int offset, double precision) {
        Camera.ref = ref;
        this.max = max;
        this.min = min;
        this.black = black;
        this.model = model;
        this.offset = offset;
        this.precision = precision;
    }

    public static boolean isLibLoaded() {
        return libLoaded;
    }

    public static boolean loadLib(String path) {
        if (libLoaded) return true;
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
        libLoaded = true;
        return true;
    }

    public Frame getFrame() {
        return frame;
    }

    public boolean open(int i) {
        if (!libLoaded) {
            System.err.println("[ERR]: Trying opening camera without loading library");
            return false;
        }
        return cap.open(i);
    }

    public boolean isOpened() {
        return cap.isOpened();
    }

    public void close() {
        cap.release();
    }

    public Frame capture() throws IOException {
        if (!libLoaded) {
            System.err.println("[ERR]: Trying reading camera input without loading library");
            return null;
        } else if (!cap.isOpened()) {
            System.err.println("[ERR]: Trying reading camera without opening it");
            return null;
        }
        if (!cap.read(frame)) throw new IOException("Camera may be disconnected");
        Core.rotate(frame, frame, Core.ROTATE_180);
        return frame;
    }
}