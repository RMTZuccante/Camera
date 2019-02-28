package it.rmtz;

import com.fazecast.jSerialComm.SerialPort;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import it.rmtz.camera.Camera;
import it.rmtz.matrix.Matrix;
import it.rmtz.matrix.SerialConnector;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Brain {
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

        SerialPort stm = null;
        for (SerialPort p : SerialPort.getCommPorts()) {
            if (p.getDescriptivePortName().equals("Maple")) {
                stm = p;
                break;
            }
        }

        if (stm == null) {
            System.err.println("Cannot find serial port connected to Maple");
        }

        System.out.println("Ready to start");
        SerialConnector c = new SerialConnector(stm, 115200);
        Matrix m = new Matrix(c, left, right);
        m.start();
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
