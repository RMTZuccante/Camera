import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

import java.io.File;
import java.io.IOException;

public class ConvertModel {
    public static void main(String[] args) {
        try {
            MultiLayerNetwork model = KerasModelImport.importKerasSequentialModelAndWeights(ClassLoader.getSystemClassLoader().getResource("model.h5").getPath().substring(1));
            ModelSerializer.writeModel(model, new File("model.dl4j"), true);
        } catch (IOException e) {
            System.err.println("File not found");
        } catch (UnsupportedKerasConfigurationException e) {
            e.printStackTrace();
        } catch (InvalidKerasConfigurationException e) {
            e.printStackTrace();
        }
    }
}
