package be.kuleuven.pylos;

import be.kuleuven.pylos.battle.BattleMT;
import be.kuleuven.pylos.battle.BattleResult;
import be.kuleuven.pylos.battle.data.PlayedGame;
import be.kuleuven.pylos.player.PylosPlayer;
import be.kuleuven.pylos.player.PylosPlayerType;
import be.kuleuven.pylos.player.codes.PylosPlayerBestFit;
import be.kuleuven.pylos.player.codes.PylosPlayerMiniMax;
import be.kuleuven.pylos.player.student.StudentPlayer_VictorIndra;

import com.google.gson.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PylosMLCollect {
    public static String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    public static final String EXPORT_PATH = "pylos-ml/src/main/training/resources/games/"+timestamp+".json"; //0
    //public static final String EXPORT_PATH = "pylos-ml/src/main/training/resources/games/test_set.json";

    public static void main(String[] args) throws IOException {
        // Collect games
        List<PlayedGame> playedGames = PylosMLCollect.collectGames();

        System.out.println("Played games: " + playedGames.size());

        // Export to json file
        File file = new File(EXPORT_PATH);
        Files.createDirectories(file.getParentFile().toPath());
        FileWriter writer = new FileWriter(file);
        Gson gson = new GsonBuilder().create();

        gson.toJson(playedGames, writer);

        writer.flush();
        writer.close();

        System.out.println("Exported to: " + EXPORT_PATH);
    }

    public static List<PlayedGame> collectGames() {
        ArrayList<PylosPlayerType> players =new ArrayList<>();
        ArrayList<PlayedGame> playedGames = new ArrayList<>();

        PylosPlayerType p1 = new PylosPlayerType("BF") {
            @Override
            public PylosPlayer create() {
                return new PylosPlayerBestFit();
            }
        };
        players.add(0,p1);

        PylosPlayerType p2 = new PylosPlayerType("MM2") {
            @Override
            public PylosPlayer create() {
                return new PylosPlayerMiniMax(2);
            }
        };
        players.add(1,p2);

        PylosPlayerType p3 = new PylosPlayerType("StudentPlayer_VictorIndra") {
            @Override
            public PylosPlayer create() {
                return new StudentPlayer_VictorIndra();
            }
        };
        players.add(2,p3);

        for(int i = 0; i<players.size();i++){
            for(int j = 0; j<players.size();j++){
                BattleResult br = BattleMT.play(players.get(i), players.get(j), 100000, 8, true); //100000
                playedGames.addAll(br.playedGames);
            }
           
        }

        

        
        
        return playedGames;
    }
}


