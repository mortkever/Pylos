package be.kuleuven.pylos;

import be.kuleuven.pylos.battle.BattleMT;
import be.kuleuven.pylos.player.PylosPlayer;
import be.kuleuven.pylos.player.PylosPlayerType;
import be.kuleuven.pylos.player.codes.PylosPlayerBestFit;
import be.kuleuven.pylos.player.codes.PylosPlayerMiniMax;
import be.kuleuven.pylos.player.codes.PylosPlayerRandomFit;
import be.kuleuven.pylos.player.student.StudentPlayer_VictorIndra;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.proto.MetaGraphDef;
import org.tensorflow.proto.SignatureDef;
import org.tensorflow.proto.TensorInfo;

import java.util.Map;

public class PylosMLTest {

    // public final static String MODEL_PATH =
    // "pylos-ml/src/main/training/resources/models/latest";

    //MODEL AANPASSEN NAAR DEZE DAT JE WIL GEBRUIKEN!!!!!
    
    public final static String MODEL_PATH = "resources\\models\\latest";
    //public final static String MODEL_PATH_2 = "resources\\models\\20241107-1057"; // latest, reinforce

    public final static String MODEL_PATH_2 = "resources\\models\\latest_min1";

    public static void main(String[] args) throws Exception {

        try (SavedModelBundle model = SavedModelBundle.load(MODEL_PATH, "serve")) {
            try (SavedModelBundle model2 = SavedModelBundle.load(MODEL_PATH_2, "serve")) { //_2
                System.out.println("Model loaded");
                printModelSignature(model2);

                PylosPlayerType trainedPlayer = new PylosPlayerType("ML") {
                    @Override
                    public PylosPlayer create() {
                        return new PylosPlayerML(model2);
                    }
                };

                //tegen een oudere versie van ml spelen
                PylosPlayerType trainedPlayer2 = new PylosPlayerType("ML2") {
                    @Override
                    public PylosPlayer create() {
                        return new PylosPlayerML(model2);
                    }
                };

                PylosPlayerType mm2 = new PylosPlayerType("MM2") {
                    @Override
                    public PylosPlayer create() {
                        return new PylosPlayerMiniMax(2);
                    }
                };

                PylosPlayerType mm4 = new PylosPlayerType("MM4") {
                    @Override
                    public PylosPlayer create() {
                        return new PylosPlayerMiniMax(4);
                    }
                };
                PylosPlayerType bestfit = new PylosPlayerType("bestfit") {
                    @Override
                    public PylosPlayer create() {
                        return new PylosPlayerBestFit();
                    }
                };

                PylosPlayerType studentPlayer = new PylosPlayerType("StudentPlayer_VictorIndra") {
                    @Override
                    public PylosPlayer create() {
                        return new StudentPlayer_VictorIndra();
                    }
                };
                PylosPlayerType mm6 = new PylosPlayerType("MM6") {
                    @Override
                    public PylosPlayer create() {
                        return new PylosPlayerMiniMax(6);
                    }
                };
                PylosPlayerType random = new PylosPlayerType("random") {
                    @Override
                    public PylosPlayer create() {
                        return new PylosPlayerRandomFit();
                    }
                };
                PylosPlayerType mm3 = new PylosPlayerType("MM3") {
                    @Override
                    public PylosPlayer create() {
                        return new PylosPlayerMiniMax(3);
                    }
                };
                //BattleMT.play(trainedPlayer, trainedPlayer2, 1000, 8);
                BattleMT.play(trainedPlayer, studentPlayer, 1000, 8);
            }

        }
    }

    private static void printModelSignature(SavedModelBundle model) {
        MetaGraphDef m = model.metaGraphDef();
        SignatureDef sig = m.getSignatureDefOrThrow("serving_default");
        int numInputs = sig.getInputsCount();
        int i = 1;
        System.out.println("MODEL SIGNATURE");
        System.out.println("Inputs:");
        for (Map.Entry<String, TensorInfo> entry : sig.getInputsMap().entrySet()) {
            TensorInfo t = entry.getValue();
            System.out.printf(
                    "%d of %d: %-20s (Node name in graph: %-20s, type: %s)\n",
                    i++, numInputs, entry.getKey(), t.getName(), t.getDtype());
        }
        int numOutputs = sig.getOutputsCount();
        i = 1;
        System.out.println("Outputs:");
        for (Map.Entry<String, TensorInfo> entry : sig.getOutputsMap().entrySet()) {
            TensorInfo t = entry.getValue();
            System.out.printf(
                    "%d of %d: %-20s (Node name in graph: %-20s, type: %s)\n",
                    i++, numOutputs, entry.getKey(), t.getName(), t.getDtype());
        }
        System.out.println("-----------------------------------------------");
    }
}
