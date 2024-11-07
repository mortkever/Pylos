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

public class SearchTreev2 {
    public Action action; // private
    private int score = 0;
    public ArrayList<SearchTreev2> nodes; // private

    public SearchTreev2(int layers, int currentLayer, PylosGameSimulator sim, PylosBoard board, PylosPlayer player,
            PylosGameIF game, Action a, int minOfMax, int alfa, int beta, PylosPlayerColor playerColor) {

        // initialiseren van nodes (I)
        nodes = new ArrayList<>();
        action = a; // actie om in die node te geraken

        if (layers <= currentLayer || sim.getState() == PylosGameState.COMPLETED
                || (board.getReservesSize(player.PLAYER_COLOR) == 0 && sim.getState() != PylosGameState.REMOVE_FIRST)) {
            score = EvaluatorV1.evaluate(board, playerColor);
        } else {
            // Alle acties oplijsten
            ArrayList<Action> possibleActions = getAllPossibleActions(board, sim, player);

            // for actie : possibleActie doe de actie en maak een nieuwe searchtree() aan
            // met currentlayer++ etc en stop ze in nodes
            // undo iedere keer ook de actie
            int alfa_new = Integer.MIN_VALUE;
            int beta_new = Integer.MAX_VALUE;

            if (minOfMax == 1) {
                int value = Integer.MIN_VALUE;
                for (Action action : possibleActions) {
                    // nieuwe simulator
                    PylosGameSimulator simulator = new PylosGameSimulator(sim.getState(), player.PLAYER_COLOR,
                            board);
                    action.simulate(simulator);

                    // toegevoegd dat player switcht
                    PylosPlayer p = player;

                    int minOfMax_new = minOfMax; // zodat min max niet al verandert is voor evalueren
                    // als volgende zet move is dan ben je gewisseld van speler denk ik
                    if (simulator.getState() == PylosGameState.MOVE) {
                        p = player.OTHER;
                        minOfMax_new = minOfMax * (-1);
                    }

                    int next_layer = currentLayer + 1;
                    SearchTreev2 tree = new SearchTreev2(layers, next_layer, simulator, board, p, game, action,
                            minOfMax_new, alfa, beta,
                            playerColor);
                    nodes.add(tree);
                    action.undoSimulate(simulator);

                    value = Integer.max(value, tree.score);
                    if (value > beta) {
                        break;
                    }
                    alfa = Integer.max(alfa, value);
                }
                score = value;
            } else {
                int value = Integer.MAX_VALUE;
                for (Action action : possibleActions) {
                    // nieuwe simulator
                    PylosGameSimulator simulator = new PylosGameSimulator(sim.getState(), player.PLAYER_COLOR,
                            board);
                    action.simulate(simulator);

                    // toegevoegd dat player switcht
                    PylosPlayer p = player;

                    int minOfMax_new = minOfMax; // zodat min max niet al verandert is voor evalueren
                    // als volgende zet move is dan ben je gewisseld van speler denk ik
                    if (simulator.getState() == PylosGameState.MOVE) {
                        p = player.OTHER;
                        minOfMax_new = minOfMax * (-1);
                    }

                    int next_layer = currentLayer + 1;
                    SearchTreev2 tree = new SearchTreev2(layers, next_layer, simulator, board, p, game, action,
                            minOfMax_new, alfa, beta,
                            playerColor);
                    nodes.add(tree);
                    action.undoSimulate(simulator);

                    value = Integer.min(value, tree.score);
                    if (value < alfa) {
                        break;
                    }
                    beta = Integer.max(beta, value);

                }
                score = value;
            }
        }

    }

    public ArrayList<Action> getRemoveActions(PylosBoard board, PylosPlayer player) {
        ArrayList<Action> actions = new ArrayList<Action>();
        PylosSphere[] spheres = board.getSpheres(player.PLAYER_COLOR);
        for (PylosSphere sphere : spheres) {
            if (sphere.canRemove()) {
                actions.add(new Action(sphere, null, sphere.getLocation(), ActionType.REMOVE));
            }
        }
        return actions;
    }

    // toegevoegd om actie terug te krijgen
    public int getScore() {
        return score;
    }

    public Action getBestAction(PylosPlayer player) {
        if (nodes.size() == 0) {
            System.out.println("info: " + action);
            // TreeVisualizer.showTree(this);
        }

        ArrayList<SearchTreev2> opties = new ArrayList<>();
        for (SearchTreev2 s : nodes) {
            if (s.getScore() == score) {
                opties.add(s);
            }
        }

        //randomizer voor als er meerdere acties zijn met dezelfde score.
        assert(opties.size() != 0);
        //tijdleijk in comment om beter te vergelijken, later terug eruit comment
        //Collections.shuffle(opties, player.getRandom());
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
                // possibleActions = getRemoveActions(board, player);

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
                // possibleActions = getRemoveActions(board, player);

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
