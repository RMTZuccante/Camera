package it.rtmz.dl;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class DeepLearning {
    private MultiLayerNetwork model = null;
    private char[] ref = null;

    DeepLearning(String modelPath, char[] ref) throws IOException {
        File f = new File("model.dl4j");
        if (!f.exists() || !f.canRead()) {
            throw new FileNotFoundException("Model file not found");
        }
        model = ModelSerializer.restoreMultiLayerNetwork(f.getAbsolutePath());
        model.init();
        this.ref = ref;
    }

    private INDArray imageFromBytes() {
        /*NativeImageLoader loader = new NativeImageLoader(80, 80, 1);
        ImagePreProcessingScaler scaler = new ImagePreProcessingScaler();
        try {
            INDArray mat = loader.asMatrix(img);
            scaler.transform(mat);
            return mat;
        } catch (IOException e) {
            return null;
        }*/return null;
    }
}
