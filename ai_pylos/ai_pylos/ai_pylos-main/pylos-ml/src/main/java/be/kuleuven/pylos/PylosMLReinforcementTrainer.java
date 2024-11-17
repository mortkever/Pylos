package be.kuleuven.pylos;

import org.tensorflow.SavedModelBundle;

public class PylosMLReinforcementTrainer {

    //public final static String MODEL_PATH = "pylos-ml/src/main/training/resources/models/latest";
    public final static String MODEL_PATH = "resources\\models\\latest";
    
    public static void main(String[] args) throws Exception {

        try (SavedModelBundle model = SavedModelBundle.load(MODEL_PATH, "serve")) {
            System.out.println("Model loaded");

        }
    }
}