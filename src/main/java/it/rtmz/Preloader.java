package it.rtmz;

import it.rtmz.camera.Camera;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class Preloader {
    public static void main(String[] args) {
        ServerSocket prserver = null;
        try {
            prserver = new ServerSocket(1026);
        } catch (IOException e) {
            System.err.println("Another preloader is already running, passing arguments to it");
            try {
                Socket client = new Socket("localhost", 1026);
                OutputStreamWriter w = new OutputStreamWriter(client.getOutputStream());
                w.write(Arrays.toString(args));
                w.flush();
                w.close();
                client.close();
            } catch (IOException e1) {
                System.err.println("Cannot connect to preloader");
                System.exit(-1);
            }
            System.exit(0);
        }


        String lp = null;
        String modelpath = "model.dl4j";
        if (args != null && args.length > 0) {
            int i = 0;
            try {
                for (i = 0; i < args.length; i++) {
                    if (args[i].equals("-lp")) {
                        lp = args[++i];
                    } else if (args[i].equals("-m")) {
                        modelpath = args[++i];
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                System.err.println("Missing argument after [" + args[i - 1] + "]l");
                System.exit(-1);
            }
        }

        if (!Camera.loadLib(lp)) System.exit(-1);
        File f = new File(modelpath);
        MultiLayerNetwork model = null;
        if (f.exists() && f.canRead()) {
            try {
                model = ModelSerializer.restoreMultiLayerNetwork(f.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        exec(args);
    }

    public static void exec(String[] args) {
        Imshow sh = new Imshow("ASd");
        VideoCapture c = new VideoCapture(0);
        if (!c.open(0)) System.out.println("niente");
        Mat m = new Mat();
        while (c.isOpened() && c.read(m)) sh.showImage(m);
    }
}
