package camera;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

import java.io.File;
import java.io.IOException;

public class ModelLoader {
    public static MultiLayerNetwork loadModel(String path) throws ModelLoaderException {
        File f = new File(path);
        MultiLayerNetwork model = null;
        if (f.exists() && f.canRead()) {
            try {
                model = ModelSerializer.restoreMultiLayerNetwork(f.getAbsolutePath());
            } catch (IOException e) {
                throw new ModelLoaderException("Error loading model: " + e.getMessage());
            }
        } else {
            throw new ModelLoaderException("Model file does not exists or cannot be read", false);
        }
        return model;
    }

    public static class ModelLoaderException extends Exception {
        private boolean critic = true;

        ModelLoaderException(String msg) {
            super(msg);
        }

        ModelLoaderException(String msg, boolean critic) {
            super(msg);
            this.critic = critic;
        }

        public boolean isCritic() {
            return critic;
        }
    }
}
