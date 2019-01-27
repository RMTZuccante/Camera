package it.rtmz.camera;

import org.apache.commons.lang3.SystemUtils;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.io.IOException;

public class Camera {

    static char[] ref = new char[]{'H', 'S', 'U'};
    private static boolean libLoaded = false;
    MultiLayerNetwork model;
    private VideoCapture cap = new VideoCapture();

    public Camera(MultiLayerNetwork model) {
        this.model = model;
    }

    static public boolean loadLib(String path) {
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

    public boolean open(int i) {
        if (!libLoaded) {
            System.err.println("[ERR]: Trying opening camera without loading library");
            return false;
        }
        return cap.open(i);
    }

    public boolean open(String fn) {
        if (!libLoaded) {
            System.err.println("[ERR]: Trying opening camera without loading library");
            return false;
        }
        return cap.open(fn);
    }

    public Frame capture() throws IOException {
        if (!libLoaded) {
            System.err.println("[ERR]: Trying reading camera input without loading library");
            return null;
        } else if (!cap.isOpened()) {
            System.err.println("[ERR]: Trying reading camera without opening it");
            return null;
        }
        Frame f = new Frame(this);
        if (!cap.read(f)) throw new IOException("Camera may be disconnected");
        return f;
    }
}