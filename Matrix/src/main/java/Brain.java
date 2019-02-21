import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import it.rmtz.camera.Camera;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.opencv.highgui.HighGui;

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
        boolean cameraloaded = false;

        try {
            config = new JsonParser().parse(new JsonReader(new FileReader("config.json"))).getAsJsonObject();
        } catch (FileNotFoundException e) {
            System.err.println("Cannot find config.json");
            System.exit(-1);
        }

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

        if (!getValuesFromJson(config) || !Camera.loadLib(lp)) {
            System.err.println("Can't use camera, continuing anyway");
        } else {
            File f = new File(modelpath);
            MultiLayerNetwork model = null;
            if (f.exists() && f.canRead()) {
                try {
                    model = ModelSerializer.restoreMultiLayerNetwork(f.getAbsolutePath());
                    left = new Camera(model, ref, minArea, maxAra, thresh, offset, precision);
                    right = new Camera(model, ref, minArea, maxAra, thresh, offset, precision);
                    cameraloaded = left.open(cl) && right.open(cr);
                } catch (IOException e) {
                    e.printStackTrace();
                    model = null;
                }
            }
        }

        if (!cameraloaded) {
            System.err.println("Error loading cameras, continuing anyway");
            left.close();
            right.close();
            left = right = null;
        }

        while (cameraloaded) {
            try {
                HighGui.imshow("left", left.capture());
                HighGui.imshow("right", right.capture());
                HighGui.waitKey(1);
                System.out.println("Left: " + left.getFrame().predict());
                System.out.println("Right: " + right.getFrame().predict());
            } catch (IOException e) {
                left.close();
                right.close();
                cameraloaded = false;
            }
        }
        HighGui.destroyWindow("left");
        HighGui.destroyWindow("right");
        System.out.println("Starting");
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
