import camera.Camera;
import camera.ModelLoader;
import com.fazecast.jSerialComm.SerialPort;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import json.Values;
import matrix.Matrix;
import matrix.SerialConnector;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class Brain {
    private static SerialPort stm;

    private static Thread shutdown = new Thread(() -> {
        System.out.println("Closing serial port: " + (stm.closePort() ? "TRUE" : "FALSE"));
    });

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

        Values v = new Values(config);
        if (v.load()) {
            if (Camera.loadLib(v.libpath)) {
                MultiLayerNetwork model = null;
                try {
                    model = ModelLoader.loadModel(modelpath);
                    System.out.println("Model imported");
                } catch (ModelLoader.ModelLoaderException e) {
                    System.out.println("Error loading model " + e.getMessage());
                    if (e.isCritic()) System.exit(-1);
                }

                if (model != null) {
                    left = new Camera(model, v.ref, v.minArea, v.maxAra, v.thresh, v.offset, v.precision, v.paddings);
                    right = new Camera(model, v.ref, v.minArea, v.maxAra, v.thresh, v.offset, v.precision, v.paddings);
                    if (!left.open(v.leftCameaId)) {
                        System.err.println("Error opening left camera. index: " + v.leftCameaId);
                    } else System.out.println("Left camera opened");

                    if (!right.open(v.rightCameraId)) {
                        System.err.println("Error opening right camera. index: " + v.rightCameraId);
                    } else System.out.println("Right camera opened");

                    System.out.println("Cameras loaded");
                }
            } else {
                System.err.println("Error loading lib, provided path may be wrong");
            }
        } else {
            System.err.println("Error loading config.json");
            System.exit(-1);
        }

        stm = SerialPort.getCommPort("ttyAMA0");
        if (args.length > 0) {
            stm = SerialPort.getCommPort(args[0]);
        }

        System.out.println("Using " + stm.getSystemPortName());
        SerialConnector c = new SerialConnector(stm, 115200);
        Matrix m = new Matrix(c, left, right, v.distwall, v.bodytemp);
        Runtime.getRuntime().addShutdownHook(shutdown);
        m.start();
    }
}
