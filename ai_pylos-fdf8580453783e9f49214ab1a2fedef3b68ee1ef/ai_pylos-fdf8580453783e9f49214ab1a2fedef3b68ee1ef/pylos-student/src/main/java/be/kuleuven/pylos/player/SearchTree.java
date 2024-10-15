package be.kuleuven.pylos.player;

import be.kuleuven.pylos.game.PylosLocation;
import be.kuleuven.pylos.game.PylosSphere;
import be.kuleuven.pylos.game.PylosSquare;
import be.kuleuven.pylos.game.PylosGameSimulator;
import be.kuleuven.pylos.game.PylosGameState;
import be.kuleuven.pylos.game.PylosGameIF;

import be.kuleuven.pylos.player.TreeVisualizer;

//import java.lang.reflect.AccessFlag.Location;
import java.util.ArrayList;

import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.game.PylosGame;
import be.kuleuven.pylos.player.Action.*;

public class SearchTree {
    public Action action; // private
    private int layer;
    private int score = 0;
    public ArrayList<SearchTree> nodes; // private
    private int minOfMax;

    public SearchTree(int layers, int currentLayer, PylosGameSimulator sim, PylosBoard board, PylosPlayer player,
            PylosGameIF game, Action a, int minOfMax) {

        // initialiseren van nodes (I)
        nodes = new ArrayList<>();
        action = a; // actie om in die node te geraken

        if (layers <= currentLayer || sim.getState() == PylosGameState.COMPLETED || (board.getReservesSize(player.PLAYER_COLOR) == 0 && sim.getState() != PylosGameState.REMOVE_FIRST)) {
            score = Evaluator.evaluate(board, sim.getColor());
        } else {

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
                                    possibleActions.add(new Action(sphere, square.getTopLocation(), sphere.getLocation(), ActionType.MOVE));
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
                    //possibleActions = getRemoveActions(board, player);

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
                    sim.getWinner();
                    break;
            }
            // for actie : possibleActie doe de actie en maak een nieuwe searchtree() aan
            // met currentlayer++ etc en stop ze in nodes
            // undo iedere keer ook de actie
            
            for (Action action : possibleActions) {
                // nieuwe simulator

                PylosGameSimulator simulator = new PylosGameSimulator(sim.getState(), player.PLAYER_COLOR, board);
                
                action.simulate(simulator);
                // toegevoegd dat player switcht
                PylosPlayer p = player;

                // DIT WERKT NIET, STEL DAT JE REMOVE ACTIE HEBT OFZO DAN BLIJF JE IN VOLGENDE
                // LAYER BIJ ZELFDE SPELER
                // if (currentLayer % 2 != 0){
                // //dan andere speler
                // p = player.OTHER;
                // }
                int minOfMax_new = minOfMax; //zodat min max niet al verandert is voor evalueren
                if (simulator.getState() == PylosGameState.MOVE) { // als volgende zet move is dan ben je gewisseld van
                                                                   // speler denk ik
                    // dan andere speler
                    p = player.OTHER;
                    minOfMax_new = minOfMax * (-1);
                }
                int next_layer = currentLayer + 1;
                
                nodes.add(new SearchTree(layers, next_layer, simulator, board, p, game, action, minOfMax_new)); // currentLayer++
                                                                                                            // -> anders
                                                                                                            // ga je
                                                                                                            // huidige
                                                                                                            // variabele
                                                                                                            // ook
                                                                                                            // telkens
                                                                                                            // verhogen
                // test om actie toe te voegen
                // actie om in die node te geraken
                // for (SearchTree n : nodes) {
                // n.action = action;
                // }

                action.undoSimulate(simulator);
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

            // werkte ook niet doordat speler meerdere layers aan zet kan zijn
            // int minOfMax = 0;
            // if (currentLayer % 2 == 0) {
            // minOfMax = -1; // min -> aangepast want starten in laag 1
            // } else {
            // minOfMax = 1; // max
            // }
            // if(nodes.size()>0){
            // score = nodes.get(0).score; //initialiseren op score van eerste kind
            // }

            if (nodes.size() > 0) {
                //score = nodes.get(0).score; // initialiseren op score van eerste kind
                if (minOfMax == 1) { // neem het maximum
                    score = nodes.get(0).score;
                } else {
                    score = nodes.get(0).score *-1;
                }
            }

            for (SearchTree node : nodes) {
                // System.out.println(node.score + ", layer: "+currentLayer);
                if (minOfMax == 1) { // neem het maximum
                    if (node.score > score) {
                        score = node.score;
                    }
                } else {
                    if (node.score > (score * (-1))) { // neem het minimum -> terug maximum? want dan wordt gekeken tov min speler, dus die wil zijn score ook maximaliseren, gwn *-1 dan om uiteindelijke
                        score = (node.score * (-1)); //* (-1)
                    }
                }
                // System.out.println("node score: " + node.score);
                // if (node.score* minOfMax > score * minOfMax ) { //tweede efkes weg * minOfMax
                // score = node.score *minOfMax ; //minOfMax is test

                // }
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
            TreeVisualizer.showTree(this);
        }
        Action bestAction = nodes.get(0).action;
        for (SearchTree s : nodes) {
            if (s.getScore() == score) {
                // System.out.println("score: " + score);
                bestAction = s.action;
            }
        }
        //TreeVisualizer.showTree(this);
        return bestAction;
    }

}
