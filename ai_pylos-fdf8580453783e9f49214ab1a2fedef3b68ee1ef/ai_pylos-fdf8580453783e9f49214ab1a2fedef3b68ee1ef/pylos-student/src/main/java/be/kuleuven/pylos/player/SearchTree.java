package be.kuleuven.pylos.player;

import be.kuleuven.pylos.game.PylosLocation;
import be.kuleuven.pylos.game.PylosSphere;
import be.kuleuven.pylos.game.PylosSquare;
import be.kuleuven.pylos.game.PylosGameSimulator;
import be.kuleuven.pylos.game.PylosGameState;

import java.util.ArrayList;

import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.player.Action.*;

public class SearchTree {
    private Action action;
    private int layer;
    private int score;
    private SearchTree[] nodes;

    public SearchTree(int layers, int currentLayer, PylosGameSimulator sim, PylosBoard board, PylosPlayer player) {
        if (layers == currentLayer) {
            score = Evaluator.evaluate(board, sim.getColor());
        } else {
            ArrayList<Action> possibleActions = new ArrayList<Action>();
            switch (sim.getState()) {
                case MOVE:
                //Eeventueel over alle ballen itereren ipv lokaties want bij een vierkant moeten alle ballen toch gecheckt worden.
                    PylosLocation[] locations = board.getLocations();
                    ArrayList<PylosSphere> freeSpheres = new ArrayList<PylosSphere>();
                    for (PylosLocation location : locations) {
                        if (location.isUsable()) {
                            PylosSphere sphere = board.getReserve(player);
                            possibleActions.add(new Action(sphere, location, null, ActionType.MOVE));
                        } else if (!location.hasAbove() && location.getSphere().PLAYER_COLOR == player.PLAYER_COLOR) {
                            freeSpheres.add(location.getSphere());
                        }
                    }
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
                    //analoog
                    break;
                case REMOVE_SECOND:
                    //analoog
                    //+pass optie
                    break;

                default:
                    // ABORTED, DRAW, COMPLETED
                    break;
            }
            //for actie : possibleActie doe de actie en maak een nieuwe searchtree() aan met currentlayer++ etc en stop ze in nodes
            // undo iedere keer ook de actie

            //Als dat alles doorlopen is.
            //Propageer de score naar boven toe.
            //laag 0: eigen zetten
            //laag 1: tegenstander zetten
            //laag 2: eigen
            //etc
            //=>even maximliseer, oneven minimaliseer.
            //if(layer%2==0){max(nodes[])}else{min(nodes[])}
            //return zelf niets. De eindscore en actie zitten in score en action.
            //Die moet er buiten dan maar uit gevoerd worden.
        }

    }
}
