package be.kuleuven.pylos.player;

import be.kuleuven.pylos.game.PylosLocation;
import be.kuleuven.pylos.game.PylosPlayerColor;
import be.kuleuven.pylos.game.PylosSphere;
import be.kuleuven.pylos.game.PylosSquare;
import be.kuleuven.pylos.game.PylosGameSimulator;
import be.kuleuven.pylos.game.PylosGameState;
import be.kuleuven.pylos.game.PylosGameIF;

import java.util.ArrayList;
import java.util.Collections;

import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.player.Action.*;

public class SearchTreev5 {
    public Action action; // private
    private int score = 0;
    public ArrayList<SearchTreev5> nodes; // private
    // alfa: beste keuze tot nu toe voor max player
    public int alfa = Integer.MIN_VALUE;
    // beta: beste keuze tot nu to voor MIN player
    public int beta = Integer.MAX_VALUE;

    Action bestAction = null;

    private static final int MAX_DEPTH = 8; // diepte kan hier aanpassen

    public SearchTreev5(int currentLayer, PylosGameSimulator sim, PylosBoard board, PylosPlayer player,
            PylosGameIF game, Action a, int alpha, int beta, boolean isMaximizingPlayer, PylosPlayerColor playerColor) {
        nodes = new ArrayList<>();
        action = a; // actie om in die node te geraken

        if (MAX_DEPTH <= currentLayer || sim.getState() == PylosGameState.COMPLETED
                || (board.getReservesSize(player.PLAYER_COLOR) == 0 && sim.getState() != PylosGameState.REMOVE_FIRST)) {
            score = EvaluatorV5.evaluate(board, playerColor);
        } else {

            ArrayList<Action> possibleActions = getAllPossibleActions(board, sim, player);
            Collections.shuffle(possibleActions, player.getRandom());
            
                if (isMaximizingPlayer) {
                    int maxEval = Integer.MIN_VALUE;

                    for (Action action : possibleActions) {
                        action.simulate(sim);

                        PylosPlayer p = player;

                        boolean isMaximizingPlayer_new = isMaximizingPlayer; // zodat min max niet al verandert is voor
                                                                             // evalueren
                        // als volgende zet move is dan ben je gewisseld van speler denk ik
                        if (sim.getState() == PylosGameState.MOVE) {
                            p = player.OTHER;
                            isMaximizingPlayer_new = !isMaximizingPlayer;
                        }

                        int next_layer = currentLayer + 1;

                        // Recursie
                        SearchTreev5 tree = new SearchTreev5(next_layer, sim, board, p, game, action, alfa, beta,
                                isMaximizingPlayer_new, playerColor);
                        int eval = tree.getScore();
                        nodes.add(tree);

                        if (eval > maxEval) {
                            maxEval = eval;
                            bestAction = action;
                            score = eval;
                        }

                        alpha = Math.max(alpha, eval);
                        action.undoSimulate(sim);

                        if (beta <= alpha) {
                            
                            nodes.clear();
                            break; // Beta cut-off
                        }

                    }
                } else {
                    int minEval = Integer.MAX_VALUE;

                    for (Action action : possibleActions) {
                        action.simulate(sim);

                        PylosPlayer p = player;

                        boolean isMaximizingPlayer_new = isMaximizingPlayer; // zodat min max niet al verandert is voor
                                                                             // evalueren
                        // als volgende zet move is dan ben je gewisseld van speler denk ik
                        if (sim.getState() == PylosGameState.MOVE) {
                            p = player.OTHER;
                            isMaximizingPlayer_new = !isMaximizingPlayer;
                        }

                        int next_layer = currentLayer + 1;

                        // Recursive call
                        SearchTreev5 tree = new SearchTreev5(next_layer, sim, board, p, game, action, alfa, beta,
                                isMaximizingPlayer_new, playerColor);
                        int eval = tree.getScore();
                        nodes.add(tree);

                        if (eval < minEval) {
                            minEval = eval;
                            bestAction = action;
                            score = eval;
                        }

                        beta = Math.min(beta, eval);
                        action.undoSimulate(sim);
                        if (beta <= alpha) {
                            
                            nodes.clear();
                            break; // Alpha cut-off
                        }

                    }

                }
            
        }
    }

    // toegevoegd om actie terug te krijgen
    public int getScore() {
        return score;
    }

    public Action getBestAction(PylosPlayer player) {
        //kan wrs ook gebruik maken van bestAction rechtstreeks => nog testen

        // TreeVisualizer.showTree(this);
        if (nodes.size() == 0) {
            System.out.println("info: " + action);
            // TreeVisualizer.showTree(this);
        }

        ArrayList<SearchTreev5> opties = new ArrayList<>();
        for (SearchTreev5 s : nodes) {
            if (s.getScore() == score) {
                opties.add(s);
            }
        }

        // randomizer voor als er meerdere acties zijn met dezelfde score.
        assert (opties.size() != 0);
        // tijdelijk comment om beter te kunnen vergelijken, later terug eruit
        Collections.shuffle(opties, player.getRandom());
        return opties.get(0).action;
    }

    public ArrayList<Action> getAllPossibleActions(PylosBoard board, PylosGameSimulator sim, PylosPlayer player) {
        ArrayList<Action> possibleActions = new ArrayList<Action>();
        switch (sim.getState()) {
            case MOVE:
                // Eventueel over alle ballen itereren ipv lokaties want bij een vierkant
                // moeten alle ballen toch gecheckt worden.
                PylosLocation[] locations = board.getLocations();

                ArrayList<PylosSphere> freeSpheres = new ArrayList<PylosSphere>();
                // alle locaties checken en er een bal naar verplaatsen als mogelijk
                for (PylosLocation location : locations) {
                    if (location.isUsable()) {
                        PylosSphere sphere = board.getReserve(player);
                        possibleActions.add(new Action(sphere, location, null, ActionType.MOVE));
                    } else if (location.getSphere() != null) {
                        if (!location.hasAbove() && location.getSphere().PLAYER_COLOR == player.PLAYER_COLOR) {
                            freeSpheres.add(location.getSphere());
                        }
                    }
                }
                // alle vierkanten checken en de vrije ballen er naar verplaatsen
                for (PylosSquare square : board.getAllSquares()) {
                    if (square.isSquare() && !square.getTopLocation().isUsed()) {
                        for (PylosSphere sphere : freeSpheres) {
                            if (sphere.canMoveTo(square.getTopLocation())) {
                                possibleActions.add(new Action(sphere, square.getTopLocation(),
                                        sphere.getLocation(), ActionType.MOVE));
                            }
                        }
                    }
                }
                break;
            case REMOVE_FIRST:

                PylosSphere[] spheres = board.getSpheres(player.PLAYER_COLOR);
                for (PylosSphere sphere : spheres) {
                    if (sphere.canRemove()) {
                        possibleActions.add(new Action(sphere, null, sphere.getLocation(), ActionType.REMOVE));
                    }
                }

                break;
            case REMOVE_SECOND:
                // analoog
                // +pass optie

                PylosSphere[] spheres_2 = board.getSpheres(player.PLAYER_COLOR);
                for (PylosSphere sphere : spheres_2) {
                    if (sphere.canRemove()) {
                        possibleActions.add(new Action(sphere, null, sphere.getLocation(), ActionType.REMOVE));
                    }
                }
                possibleActions.add(new Action(null, null, null, ActionType.PASS));
                break;
            default:
                // ABORTED, DRAW, COMPLETED
                // sim.getWinner();
                System.out.println("Passed default: something went wrong");
                break;
        }

        return possibleActions;
    }

}
