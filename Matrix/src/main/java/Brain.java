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
import java.util.logging.Level;
import java.util.logging.Logger;

import static utils.Utils.RMTZ_LOGGER;
import static utils.Utils.setupLogger;

public class Brain {
    private final static Logger logger = Logger.getLogger(RMTZ_LOGGER);
    private static SerialPort stm;

    private static Thread shutdown = new Thread(() -> {
        logger.info("Closing serial port: " + (stm.closePort() ? "TRUE" : "FALSE"));
    });

    public static void main(String[] args) {
        setupLogger(true);
        JsonObject config = null;
        Camera left = null, right = null;

        String modelpath = "./model/model.dl4j";

        try {
            config = new JsonParser().parse(new JsonReader(new FileReader("config.json"))).getAsJsonObject();
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE,"Cannot find config.json");
            System.exit(-1);
        }

        Values v = new Values(config);
        if (v.load()) {
            if (v.enbaleCameras && Camera.loadLib(v.libpath)) {
                MultiLayerNetwork model = null;
                try {
                    model = ModelLoader.loadModel(modelpath);
                    logger.info("Model imported");
                } catch (ModelLoader.ModelLoaderException e) {
                    logger.info("Error loading model " + e.getMessage());
                    if (e.isCritic()) System.exit(-1);
                }

                if (model != null) {
                    left = new Camera(v.leftCameaId, model, v.ref, v.minArea, v.maxAra, v.thresh, v.offset, v.precision, v.paddings, false);
                    if (!left.open(v.leftCameaId)) {
                        logger.log(Level.SEVERE,"Error opening left camera. index: " + v.leftCameaId);
                    } else logger.info("Left camera opened");

                    right = new Camera(v.rightCameraId, model, v.ref, v.minArea, v.maxAra, v.thresh, v.offset, v.precision, v.paddings, true);
                    if (!right.open(v.rightCameraId)) {
                        logger.log(Level.SEVERE,"Error opening right camera. index: " + v.rightCameraId);
                    } else logger.info("Right camera opened");

                    logger.info("Cameras loaded");
                }
            } else {
                logger.log(Level.SEVERE,"Error loading lib, provided path may be wrong");
            }
        } else {
            logger.log(Level.SEVERE,"Error loading config.json");
            System.exit(-1);
        }

        stm = SerialPort.getCommPort("ttyAMA0");
        if (args.length > 0) {
            stm = SerialPort.getCommPort(args[0]);
        }

        logger.info("Using " + stm.getSystemPortName());
        SerialConnector c = new SerialConnector(stm, 115200);
        Matrix m = new Matrix(c, left, right, v.distwall, v.bodytemp);
        Runtime.getRuntime().addShutdownHook(shutdown);
        m.start(v.debugLevel, v.black);
    }
}
