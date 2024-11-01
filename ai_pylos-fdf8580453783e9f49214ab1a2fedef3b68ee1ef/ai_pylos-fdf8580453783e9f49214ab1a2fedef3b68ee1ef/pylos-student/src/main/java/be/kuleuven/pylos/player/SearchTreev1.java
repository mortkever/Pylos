package be.kuleuven.pylos.player;

import be.kuleuven.pylos.game.PylosLocation;
import be.kuleuven.pylos.game.PylosPlayerColor;
import be.kuleuven.pylos.game.PylosSphere;
import be.kuleuven.pylos.game.PylosSquare;
import be.kuleuven.pylos.game.PylosGameSimulator;
import be.kuleuven.pylos.game.PylosGameState;
import be.kuleuven.pylos.game.PylosGameIF;


import java.util.ArrayList;

import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.player.Action.*;

public class SearchTreev1 {
    public Action action; // private
    private int score = 0;
    public ArrayList<SearchTreev1> nodes; // private

    public SearchTreev1(int layers, int currentLayer, PylosGameSimulator sim, PylosBoard board, PylosPlayer player,
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

            boolean prune = false;
            for (Action action : possibleActions) {
                if (!prune) {
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
                    SearchTreev1 tree = new SearchTreev1(layers, next_layer, simulator, board, p, game, action,
                            minOfMax_new,
                            alfa_new,
                            beta_new, playerColor);
                    nodes.add(tree);

                    /*if (minOfMax == -1) {
                        if (tree.score >= beta_new)
                            alfa_new = tree.score;
                        if (tree.score < alfa) {
                            prune = true;
                        }
                    } else {
                        if (tree.score <= alfa_new)
                            alfa_new = tree.score;
                        if (tree.score > beta) {
                            prune = true;
                        }
                    }*/

                    action.undoSimulate(simulator);
                }
            }

            if (nodes.size() > 0) {
                    score = nodes.get(0).score;
            }

            // Score naar boven propageren
            for (int i = 0; i < nodes.size(); i++) {
                // System.out.println(node.score + ", layer: "+currentLayer);
                if (minOfMax == 1) { // neem het maximum
                    // if (nodes.get(i).score > score) {
                    // score = nodes.get(i).score;
                    // }
                    score = Math.max(score, nodes.get(i).score);
                } else {
                    // if (nodes.get(i).score > (score * (-1))) { // neem het minimum -> terug
                    // maximum? want dan wordt
                    // gekeken tov min speler, dus die wil zijn score ook
                    // maximaliseren, gwn *-1 dan om uiteindelijke
                    // score = (nodes.get(i).score * (-1)); // * (-1)
                    // }
                    score = Math.min(score, nodes.get(i).score);
                }
                // System.out.println("node score: " + node.score);
                // if (node.score* minOfMax > score * minOfMax ) { //tweede efkes weg * minOfMax
                // score = node.score *minOfMax ; //minOfMax is test

                // }
                nodes.get(i).nodes = null;

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

    public Action getBestAction() {
        if (nodes.size() == 0) {
            System.out.println("info: " + action);
            // TreeVisualizer.showTree(this);
        }
        Action bestAction = nodes.get(0).action;
        for (SearchTreev1 s : nodes) {
            if (s.getScore() == score) {
                // System.out.println("score: " + score);
                bestAction = s.action;
            }
        }
        // TreeVisualizer.showTree(this);
        return bestAction;
    }

    public ArrayList<Action> getAllPossibleActions(PylosBoard board, PylosGameSimulator sim, PylosPlayer player) {
        ArrayList<Action> possibleActions = new ArrayList<Action>();
        switch (sim.getState()) {
            case MOVE:
                // Eeventueel over alle ballen itereren ipv lokaties want bij een vierkant
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
