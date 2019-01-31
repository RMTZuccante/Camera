package it.rtmz.train;

import it.rtmz.camera.Camera;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Train {
    private static VideoCapture cap;
    //https://netty.io/wiki/user-guide-for-4.x.html
    public static void main(String[] args) {
        String lp = null;
        if (args != null && args.length > 0) {
            int i = 0;
            try {
                for (i = 0; i < args.length; i++) {
                    if (args[i].equals("-lp")) {
                        lp = args[++i];
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                System.err.println("Missing argument after [" + args[i - 1] + "]l");
                System.exit(-1);
            }
        }

        if (!Camera.loadLib(lp)) System.exit(-1);
        cap = new VideoCapture();
        if (!cap.open(0)) {
            System.err.println("Cannot open camera");
            System.exit(-1);
        }
        ServerSocket server = null;
        try {
            server = new ServerSocket(1026);
        } catch (IOException e) {
            System.err.println("Cannot access port 1026");
            System.exit(-1);
        }

        /*Mat im = new Mat();
        MatOfByte jpg = new MatOfByte();
        while (cap.isOpened() && cap.read(im)) {
            Imgcodecs.imencode(".jpg", im, jpg);
            HighGui.imshow("cos?", jpg);
            HighGui.waitKey(1);
        }*/


        try {
            while (true) {
                Socket client = server.accept();
                stream(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void stream(Socket client) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
        Mat im = new Mat();
        MatOfByte jpg = new MatOfByte();
        while (client.isConnected() && cap.isOpened() && cap.read(im)) {
            Imgcodecs.imencode(".jpg", im, jpg);
            oos.writeObject(jpg.toArray());
        }
    }
}
