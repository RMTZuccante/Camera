package it.rmtz.utils;

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
            System.out.println("Looking for model.h5 in this directory");
            File f = new File("model.h5");
            MultiLayerNetwork model = KerasModelImport.importKerasSequentialModelAndWeights(f.getAbsolutePath());
            ModelSerializer.writeModel(model, new File("model.dl4j"), true);
            System.out.println("Model converted");
        } catch (IOException e) {
            System.err.println("File not found");
        } catch (UnsupportedKerasConfigurationException e) {
            System.err.println("Unsupported Keras configuration");
        } catch (InvalidKerasConfigurationException e) {
            System.err.println("Invalid Keras configuration");
        }
    }
}
