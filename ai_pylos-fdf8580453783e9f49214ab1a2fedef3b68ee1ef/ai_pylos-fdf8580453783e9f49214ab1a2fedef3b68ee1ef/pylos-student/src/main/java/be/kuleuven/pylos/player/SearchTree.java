package be.kuleuven.pylos.player;

import be.kuleuven.pylos.game.PylosLocation;
import be.kuleuven.pylos.game.PylosSphere;
import be.kuleuven.pylos.game.PylosSquare;
import be.kuleuven.pylos.game.PylosGameSimulator;
import be.kuleuven.pylos.game.PylosGameState;

import java.lang.reflect.AccessFlag.Location;
import java.util.ArrayList;

import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.player.Action.*;

public class SearchTree {
    private Action action;
    private int layer;
    private int score = 0;
    private ArrayList<SearchTree> nodes;

    public SearchTree(int layers, int currentLayer, PylosGameSimulator sim, PylosBoard board, PylosPlayer player) {
        if (layers == currentLayer || sim.getState() == PylosGameState.COMPLETED) {
            score = Evaluator.evaluate(board, sim.getColor());
        } else {
            ArrayList<Action> possibleActions = new ArrayList<Action>();
            switch (sim.getState()) {
                case MOVE:
                    // Eeventueel over alle ballen itereren ipv lokaties want bij een vierkant
                    // moeten alle ballen toch gecheckt worden.
                    PylosLocation[] locations = board.getLocations();
                    ArrayList<PylosSphere> freeSpheres = new ArrayList<PylosSphere>();
                    //alle locaties checken en er een bal naar verplaatsen als mogelijk
                    for (PylosLocation location : locations) {
                        if (location.isUsable()) {
                            PylosSphere sphere = board.getReserve(player);
                            possibleActions.add(new Action(sphere, location, null, ActionType.MOVE));
                        } else if (!location.hasAbove() && location.getSphere().PLAYER_COLOR == player.PLAYER_COLOR) {
                            freeSpheres.add(location.getSphere());
                        }
                    }
                    //alle vierkanten checken en de vrije ballen er naar verplaatsen
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
                    possibleActions = getRemoveActions(board, player);
                    break;
                case REMOVE_SECOND:
                    // analoog
                    // +pass optie
                    possibleActions = getRemoveActions(board, player);
                    possibleActions.add(new Action(null, null, null, ActionType.PASS));
                    break;
                default:
                    // ABORTED, DRAW, COMPLETED
                    sim.getWinner();
                    break;
            }
            // for actie : possibleActie doe de actie en maak een nieuwe searchtree() aan
            // met currentlayer++ etc en stop ze in nodes
            // undo iedere keer ook de actie
            for (Action action : possibleActions) {
                action.simulate(sim);
                nodes.add(new SearchTree(layers, currentLayer++, sim, board, player));
                action.undoSimulate(sim);
            }

            // Als dat alles doorlopen is.
            // Propageer de score naar boven toe.
            // laag 0: eigen zetten
            // laag 1: tegenstander zetten
            // laag 2: eigen
            // etc
            // =>even maximliseer, oneven minimaliseer.
            // if(layer%2==0){max(nodes[])}else{min(nodes[])}
            // return zelf niets. De eindscore en actie zitten in score en action.
            // Die moet er buiten dan maar uit gevoerd worden.
            int minOfMax = 0;
            if (currentLayer % 2 == 0) {
                minOfMax = 1; // max
            } else {
                minOfMax = -1; // min
            }
            for (SearchTree node : nodes) {
                if (node.score * minOfMax > score * minOfMax) {
                    score = node.score;
                }
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
}
