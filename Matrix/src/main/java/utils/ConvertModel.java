package utils;

import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static utils.Utils.RMTZ_LOGGER;
import static utils.Utils.setupLogger;

public class ConvertModel {
    private final static Logger logger = Logger.getLogger(RMTZ_LOGGER);

    public static void main(String[] args) {
        setupLogger(true);
        try {
            logger.info("Looking for model.h5 in ./model");
            File f = new File("./model/model.h5");
            MultiLayerNetwork model = KerasModelImport.importKerasSequentialModelAndWeights(f.getAbsolutePath(), false);
            ModelSerializer.writeModel(model, new File("./model/model.dl4j"), true);
            logger.info("Model converted");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "File not found");
        } catch (UnsupportedKerasConfigurationException e) {
            logger.log(Level.SEVERE, "Unsupported Keras configuration");
        } catch (InvalidKerasConfigurationException e) {
            logger.log(Level.SEVERE, "Invalid Keras configuration");
        }
    }
}
