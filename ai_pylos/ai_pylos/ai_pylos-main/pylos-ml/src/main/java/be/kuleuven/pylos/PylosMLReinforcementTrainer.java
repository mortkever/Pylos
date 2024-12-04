package be.kuleuven.pylos;

import java.util.List;

import org.tensorflow.SavedModelBundle;

import be.kuleuven.pylos.battle.BattleMT;
import be.kuleuven.pylos.battle.BattleResult;
import be.kuleuven.pylos.battle.data.PlayedGame;
import be.kuleuven.pylos.player.PylosPlayer;
import be.kuleuven.pylos.player.PylosPlayerType;
import be.kuleuven.pylos.player.codes.PylosPlayerBestFit;
import be.kuleuven.pylos.player.codes.PylosPlayerMiniMax;
import be.kuleuven.pylos.player.student.StudentPlayer_VictorIndra;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;

public class PylosMLReinforcementTrainer {

    // public final static String MODEL_PATH =
    // "pylos-ml/src/main/training/resources/models/latest";
    // public final static String MODEL_PATH = "resources\\models\\reinforce_old";
    public final static String MODEL_PATH = "resources\\models\\latest_min1"; // 20241107-1015

    // public final static String MODEL_PATH_2 = "resources\\models\\20241120-1844";
    public final static String MODEL_PATH_2 = "resources\\models\\latest"; // latest
    // public final static String MODEL_PATH_2 = "resources\\models\\reinforce"; //
    // latest

    public static final String EXPORT_PATH = "pylos-ml/src/main/training/resources/games/reinforce.json";
    private static List<PlayedGame> playedGames = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        // SavedModelBundle newModel;
        System.out.println("test 1");
        try (SavedModelBundle model = SavedModelBundle.load(MODEL_PATH, "serve")) {
            try (SavedModelBundle model2 = SavedModelBundle.load(MODEL_PATH_2, "serve")) {
                System.out.println("Model loaded");

                ArrayList<PylosPlayerType> players = new ArrayList<>();
                ArrayList<PlayedGame> playedGames = new ArrayList<>();

                PylosPlayerType trainedPlayer = new PylosPlayerType("ML") {
                    @Override
                    public PylosPlayer create() {
                        return new PylosPlayerML(model);
                    }
                };
                players.add(trainedPlayer);

                // tegen een oudere versie van ml spelen
                PylosPlayerType trainedPlayer2 = new PylosPlayerType("ML2") {
                    @Override
                    public PylosPlayer create() {
                        return new PylosPlayerML(model2);
                    }
                };
                players.add(trainedPlayer2);

                PylosPlayerType p1 = new PylosPlayerType("MM6") {
                    @Override
                    public PylosPlayer create() {
                        return new PylosPlayerMiniMax(6);
                    }
                };
                players.add(0, p1);

                PylosPlayerType p2 = new PylosPlayerType("MM2") {
                    @Override
                    public PylosPlayer create() {
                        return new PylosPlayerMiniMax(2);
                    }
                };
                players.add(1, p2);

                PylosPlayerType p3 = new PylosPlayerType("StudentPlayer_VictorIndra") {
                    @Override
                    public PylosPlayer create() {
                        return new StudentPlayer_VictorIndra();
                    }
                };
                players.add(2, p3);

                PylosPlayerType p4 = new PylosPlayerType("MM4") {
                    @Override
                    public PylosPlayer create() {
                        return new PylosPlayerMiniMax(5);
                    }
                };
                players.add(2, p4);

                //Test opstelling: reinforcement door te spelen tegen zichzelf en anderen (ipv enkel tegen zen vorige versie)
                for(int i = 0; i<players.size();i++){
                    for(int j = 0; j<players.size();j++){
                        BattleResult br = BattleMT.play(players.get(i), players.get(j), 1000, 4,true); //100000
                        playedGames.addAll(br.playedGames);
                    }

                }

                //BattleResult br = BattleMT.play(trainedPlayer, trainedPlayer2, 10000, 8, true); // 100000
                //playedGames.addAll(br.playedGames);

                System.out.println("Played games: " + playedGames.size());

                // Export to json file
                File file = new File(EXPORT_PATH);
                Files.createDirectories(file.getParentFile().toPath());
                FileWriter writer = new FileWriter(file);
                Gson gson = new GsonBuilder().create();

                gson.toJson(playedGames, writer);

                writer.flush();
                writer.close();

                System.out.println("Exported to: reinforce.json");

                // https://scikit-learn.org/0.15/modules/scaling_strategies.html
                // improve model with new data
                // newModel = model.

                // model.fit(boards, scores, epochs=EPOCHS, batch_size=BATCH_SIZE);

                // model.save(MODEL_EXPORT_PATH + "reinforcement_test");
                // model.export(MODEL_EXPORT_PATH + "latest");

            }

        }
    }

}
