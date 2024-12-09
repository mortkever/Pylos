package be.kuleuven.pylos;

import java.util.List;
import java.util.Map;

import org.tensorflow.SavedModelBundle;

import be.kuleuven.pylos.battle.BattleMT;
import be.kuleuven.pylos.battle.BattleResult;
import be.kuleuven.pylos.battle.data.PlayedGame;
import be.kuleuven.pylos.game.PylosPlayerColor;
import be.kuleuven.pylos.player.PylosPlayer;
import be.kuleuven.pylos.player.PylosPlayerType;
import be.kuleuven.pylos.player.codes.PylosPlayerBestFit;
import be.kuleuven.pylos.player.codes.PylosPlayerMiniMax;
import be.kuleuven.pylos.player.student.StudentPlayer_VictorIndra;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

public class PylosMLReinforcementTrainer {


    public final static String MODEL_PATH = "resources\\models\\latest_min1"; 
    public final static String MODEL_PATH_2 = "resources\\models\\latest"; 

    public static final String EXPORT_PATH = "pylos-ml/src/main/training/resources/games/reinforce.json";
    public static final String IMPORT_PATH = "pylos-ml/src/main/training/resources/games/1731625595073.json";
    private static List<PlayedGame> playedGames = new ArrayList<>();
    private static List<PlayedGame> newPlayedGames = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        // SavedModelBundle newModel;
        System.out.println("Reinforcement Player loaded");
        /*
         * File file_input = new File(EXPORT_PATH);
         * FileReader reader = new FileReader(file_input);
         * Gson gson_input = new Gson();
         * Type playedGameListType = new TypeToken<List<PlayedGame>>(){}.getType();
         * playedGames = gson_input.fromJson(reader, playedGameListType);
         * //List<PlayedGame>
         * reader.close();
         * System.out.println("Read games: " + playedGames.size());
         */

        playedGames.clear();

        try (SavedModelBundle model = SavedModelBundle.load(MODEL_PATH, "serve")) {
            try (SavedModelBundle model2 = SavedModelBundle.load(MODEL_PATH_2, "serve")) {
                System.out.println("Model loaded");

                ArrayList<PylosPlayerType> players = new ArrayList<>();
                // ArrayList<PlayedGame> playedGames = new ArrayList<>();

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

                // Test opstelling: reinforcement door te spelen tegen zichzelf en anderen (ipv
                // enkel tegen zen vorige versie)
                for (int i = 0; i < players.size(); i++) {
                    for (int j = i + 1; j < players.size(); j++) {
                        BattleResult br = BattleMT.play(players.get(i), players.get(j), 1000, 4, true); // 100000
                        newPlayedGames.addAll(br.playedGames);
                    }
                }

                // BattleResult br = BattleMT.play(trainedPlayer, trainedPlayer2, 10000, 8,
                // true); // 100000
                // playedGames.addAll(br.playedGames);
                newPlayedGames.addAll(rotateBoard(newPlayedGames));
                newPlayedGames.addAll(mirrorBoard(newPlayedGames));

                newPlayedGames = addMoveColor(newPlayedGames, true);
                newPlayedGames.addAll(addMoveColor(switchPlayers(newPlayedGames), false));

                System.out.println("new played games: " + newPlayedGames.size());
                playedGames.addAll(newPlayedGames);
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

    private static List<PlayedGame> switchPlayers(List<PlayedGame> games) {
        List<PlayedGame> switchedGames = new ArrayList<>();

        for (PlayedGame game : games) {
            PylosPlayerType lightPlayer = new PylosPlayerType(game.lightPlayer) {
                public PylosPlayer create() {
                    return new PylosPlayerMiniMax(5); // houvast geen eigenlijk nut
                }
            };
            PylosPlayerType darkPlayer = new PylosPlayerType(game.darkPlayer) {
                public PylosPlayer create() {
                    return new PylosPlayerMiniMax(5); // houvast geen eigenlijk nut
                }
            };

            PylosPlayerColor winner = switch (game.winner) {
                case 1 -> PylosPlayerColor.DARK; // switched player colors winner
                case -1 -> PylosPlayerColor.LIGHT;
                case 0 -> null;
                default -> null;
            };

            List<Long> boardHistory = new ArrayList<>();
            for (Long board : game.boardHistory) {
                Long newBoard = 0L;
                for (int i = 0; i < 30; i++) {
                    Long temp = board;
                    temp = temp << 62 - 2 * i;
                    temp = temp >>> 62;
                    if (temp != 0L) {
                        temp = temp == 1 ? 2L : 1L;
                        temp = temp << 2 * i;
                        newBoard = temp | newBoard;
                    }

                }
                boardHistory.add(newBoard);
            }

            switchedGames.add(new PlayedGame(boardHistory, lightPlayer, darkPlayer, winner));

        }

        return switchedGames;
    }

    private static List<PlayedGame> rotateBoard(List<PlayedGame> games) {
        List<PlayedGame> rotatedGames = new ArrayList<>();

        // rechtsom
        Map<Integer, Integer> rotateOnce = new HashMap<>();
        rotateOnce.put(0, 12);
        rotateOnce.put(1, 8);
        rotateOnce.put(2, 4);
        rotateOnce.put(3, 0);
        rotateOnce.put(4, 13);
        rotateOnce.put(5, 9);
        rotateOnce.put(6, 5);
        rotateOnce.put(7, 1);
        rotateOnce.put(8, 14);
        rotateOnce.put(9, 10);
        rotateOnce.put(10, 6);
        rotateOnce.put(11, 2);
        rotateOnce.put(12, 15);
        rotateOnce.put(13, 11);
        rotateOnce.put(14, 7);
        rotateOnce.put(15, 3);
        rotateOnce.put(16 + 0, 16 + 6);
        rotateOnce.put(16 + 1, 16 + 3);
        rotateOnce.put(16 + 2, 16 + 0);
        rotateOnce.put(16 + 3, 16 + 7);
        rotateOnce.put(16 + 4, 16 + 4);
        rotateOnce.put(16 + 5, 16 + 1);
        rotateOnce.put(16 + 6, 16 + 8);
        rotateOnce.put(16 + 7, 16 + 5);
        rotateOnce.put(16 + 8, 16 + 2);
        rotateOnce.put(25 + 0, 25 + 2);
        rotateOnce.put(25 + 1, 25 + 0);
        rotateOnce.put(25 + 2, 25 + 3);
        rotateOnce.put(25 + 3, 25 + 1);
        rotateOnce.put(29, 29);

        Map<Integer, Integer> rotateTwice = new HashMap<>();
        rotateTwice.put(0, 15);
        rotateTwice.put(1, 14);
        rotateTwice.put(2, 13);
        rotateTwice.put(3, 12);
        rotateTwice.put(4, 11);
        rotateTwice.put(5, 10);
        rotateTwice.put(6, 9);
        rotateTwice.put(7, 8);
        rotateTwice.put(8, 7);
        rotateTwice.put(9, 6);
        rotateTwice.put(10, 5);
        rotateTwice.put(11, 4);
        rotateTwice.put(12, 3);
        rotateTwice.put(13, 2);
        rotateTwice.put(14, 1);
        rotateTwice.put(15, 0);
        rotateTwice.put(16 + 0, 16 + 8);
        rotateTwice.put(16 + 1, 16 + 7);
        rotateTwice.put(16 + 2, 16 + 6);
        rotateTwice.put(16 + 3, 16 + 5);
        rotateTwice.put(16 + 4, 16 + 4);
        rotateTwice.put(16 + 5, 16 + 3);
        rotateTwice.put(16 + 6, 16 + 2);
        rotateTwice.put(16 + 7, 16 + 1);
        rotateTwice.put(16 + 8, 16 + 0);
        rotateTwice.put(25 + 0, 25 + 3);
        rotateTwice.put(25 + 1, 25 + 2);
        rotateTwice.put(25 + 2, 25 + 1);
        rotateTwice.put(25 + 3, 25 + 0);
        rotateTwice.put(29, 29);

        Map<Integer, Integer> rotateThrice = new HashMap<>();
        rotateThrice.put(0, 3);
        rotateThrice.put(1, 7);
        rotateThrice.put(2, 11);
        rotateThrice.put(3, 15);
        rotateThrice.put(4, 2);
        rotateThrice.put(5, 6);
        rotateThrice.put(6, 10);
        rotateThrice.put(7, 14);
        rotateThrice.put(8, 1);
        rotateThrice.put(9, 5);
        rotateThrice.put(10, 9);
        rotateThrice.put(11, 13);
        rotateThrice.put(12, 0);
        rotateThrice.put(13, 4);
        rotateThrice.put(14, 8);
        rotateThrice.put(15, 12);
        rotateThrice.put(16 + 0, 16 + 2);
        rotateThrice.put(16 + 1, 16 + 5);
        rotateThrice.put(16 + 2, 16 + 8);
        rotateThrice.put(16 + 3, 16 + 1);
        rotateThrice.put(16 + 4, 16 + 4);
        rotateThrice.put(16 + 5, 16 + 7);
        rotateThrice.put(16 + 6, 16 + 0);
        rotateThrice.put(16 + 7, 16 + 3);
        rotateThrice.put(16 + 8, 16 + 6);
        rotateThrice.put(25 + 0, 25 + 1);
        rotateThrice.put(25 + 1, 25 + 3);
        rotateThrice.put(25 + 2, 25 + 0);
        rotateThrice.put(25 + 3, 25 + 2);
        rotateThrice.put(29, 29);

        ArrayList<Map<Integer, Integer>> rotationMaps = new ArrayList<>();
        rotationMaps.add(rotateOnce);
        rotationMaps.add(rotateTwice);
        rotationMaps.add(rotateThrice);

        for (Map<Integer, Integer> rotate : rotationMaps) {
            for (PlayedGame game : games) {
                PylosPlayerType lightPlayer = new PylosPlayerType(game.lightPlayer) {
                    public PylosPlayer create() {
                        return new PylosPlayerMiniMax(5); // houvast geen eigenlijk nut
                    }
                };
                PylosPlayerType darkPlayer = new PylosPlayerType(game.darkPlayer) {
                    public PylosPlayer create() {
                        return new PylosPlayerMiniMax(5); // houvast geen eigenlijk nut
                    }
                };

                PylosPlayerColor winner = switch (game.winner) {
                    case 1 -> PylosPlayerColor.LIGHT;
                    case -1 -> PylosPlayerColor.DARK;
                    case 0 -> null;
                    default -> null;
                };

                List<Long> boardHistory = new ArrayList<>();
                for (Long board : game.boardHistory) {
                    Long newBoard = 0L;
                    for (int i = 0; i < 30; i++) {
                        Long temp = board;
                        temp = temp << 62 - 2 * i;
                        temp = temp >>> 62;
                        temp = temp << 2 * rotate.get(i);
                        newBoard = temp | newBoard;

                    }
                    boardHistory.add(newBoard);
                }

                rotatedGames.add(new PlayedGame(boardHistory, lightPlayer, darkPlayer, winner));

            }
        }

        return rotatedGames;
    }

    private static List<PlayedGame> mirrorBoard(List<PlayedGame> games) {
        List<PlayedGame> mirroredGames = new ArrayList<>();

        // rechtsom
        Map<Integer, Integer> mirrorHori = new HashMap<>();
        mirrorHori.put(0, 12);
        mirrorHori.put(1, 13);
        mirrorHori.put(2, 14);
        mirrorHori.put(3, 15);
        mirrorHori.put(4, 8);
        mirrorHori.put(5, 9);
        mirrorHori.put(6, 10);
        mirrorHori.put(7, 11);
        mirrorHori.put(8, 4);
        mirrorHori.put(9, 5);
        mirrorHori.put(10, 6);
        mirrorHori.put(11, 7);
        mirrorHori.put(12, 0);
        mirrorHori.put(13, 1);
        mirrorHori.put(14, 2);
        mirrorHori.put(15, 3);
        mirrorHori.put(16 + 0, 16 + 6);
        mirrorHori.put(16 + 1, 16 + 7);
        mirrorHori.put(16 + 2, 16 + 8);
        mirrorHori.put(16 + 3, 16 + 3);
        mirrorHori.put(16 + 4, 16 + 4);
        mirrorHori.put(16 + 5, 16 + 5);
        mirrorHori.put(16 + 6, 16 + 0);
        mirrorHori.put(16 + 7, 16 + 1);
        mirrorHori.put(16 + 8, 16 + 2);
        mirrorHori.put(25 + 0, 25 + 2);
        mirrorHori.put(25 + 1, 25 + 3);
        mirrorHori.put(25 + 2, 25 + 0);
        mirrorHori.put(25 + 3, 25 + 1);
        mirrorHori.put(29, 29);

        Map<Integer, Integer> mirrorVeri = new HashMap<>();
        mirrorVeri.put(0, 3);
        mirrorVeri.put(1, 2);
        mirrorVeri.put(2, 1);
        mirrorVeri.put(3, 0);
        mirrorVeri.put(4, 7);
        mirrorVeri.put(5, 6);
        mirrorVeri.put(6, 5);
        mirrorVeri.put(7, 4);
        mirrorVeri.put(8, 11);
        mirrorVeri.put(9, 10);
        mirrorVeri.put(10, 9);
        mirrorVeri.put(11, 8);
        mirrorVeri.put(12, 15);
        mirrorVeri.put(13, 14);
        mirrorVeri.put(14, 13);
        mirrorVeri.put(15, 12);
        mirrorVeri.put(16 + 0, 16 + 2);
        mirrorVeri.put(16 + 1, 16 + 1);
        mirrorVeri.put(16 + 2, 16 + 0);
        mirrorVeri.put(16 + 3, 16 + 5);
        mirrorVeri.put(16 + 4, 16 + 4);
        mirrorVeri.put(16 + 5, 16 + 3);
        mirrorVeri.put(16 + 6, 16 + 8);
        mirrorVeri.put(16 + 7, 16 + 7);
        mirrorVeri.put(16 + 8, 16 + 6);
        mirrorVeri.put(25 + 0, 25 + 1);
        mirrorVeri.put(25 + 1, 25 + 0);
        mirrorVeri.put(25 + 2, 25 + 3);
        mirrorVeri.put(25 + 3, 25 + 2);
        mirrorVeri.put(29, 29);

        ArrayList<Map<Integer, Integer>> mirrorMaps = new ArrayList<>();
        mirrorMaps.add(mirrorHori);
        mirrorMaps.add(mirrorVeri);

        for (Map<Integer, Integer> mirror : mirrorMaps) {
            for (PlayedGame game : games) {
                PylosPlayerType lightPlayer = new PylosPlayerType(game.lightPlayer) {
                    public PylosPlayer create() {
                        return new PylosPlayerMiniMax(5); // houvast geen eigenlijk nut
                    }
                };
                PylosPlayerType darkPlayer = new PylosPlayerType(game.darkPlayer) {
                    public PylosPlayer create() {
                        return new PylosPlayerMiniMax(5); // houvast geen eigenlijk nut
                    }
                };

                PylosPlayerColor winner = switch (game.winner) {
                    case 1 -> PylosPlayerColor.LIGHT;
                    case -1 -> PylosPlayerColor.DARK;
                    case 0 -> null;
                    default -> null;
                };

                List<Long> boardHistory = new ArrayList<>();
                for (Long board : game.boardHistory) {
                    Long newBoard = 0L;
                    for (int i = 0; i < 30; i++) {
                        Long temp = board;
                        temp = temp << 62 - 2 * i;
                        temp = temp >>> 62;
                        temp = temp << 2 * mirror.get(i);
                        newBoard = temp | newBoard;

                    }
                    boardHistory.add(newBoard);
                }

                mirroredGames.add(new PlayedGame(boardHistory, lightPlayer, darkPlayer, winner));

            }
        }
        return mirroredGames;
    }

    public static List<PlayedGame> addMoveColor(List<PlayedGame> games, boolean lightFirst) {
        for (PlayedGame game : games) {
            for (int i = 0; i < game.boardHistory.size(); i++) {
                Long board = game.boardHistory.get(i);
                Boolean lightsTurn = (i % 2 == (lightFirst ? 0 : 1));
                board = board + (((lightsTurn ? 1L : 0L) << 60));

                game.boardHistory.set(i, board);
            }
        }
        return games;
    }

    public static String padleft(String input) {
        int lengte = input.length();
        for (int i = 0; i < 64 - lengte; i++)
            input = "0" + input;

        return input;
    }
}
