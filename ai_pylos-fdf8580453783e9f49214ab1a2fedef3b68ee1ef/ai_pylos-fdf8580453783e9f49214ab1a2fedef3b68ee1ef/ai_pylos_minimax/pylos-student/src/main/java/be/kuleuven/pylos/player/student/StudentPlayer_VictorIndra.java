package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.game.PylosGameIF;
import be.kuleuven.pylos.game.PylosGameSimulator;
import be.kuleuven.pylos.player.PylosPlayer;
import be.kuleuven.pylos.player.Action.Action;
import be.kuleuven.pylos.game.PylosPlayerColor;
import be.kuleuven.pylos.game.PylosLocation;
import be.kuleuven.pylos.game.PylosSphere;
import be.kuleuven.pylos.game.PylosSquare;
import be.kuleuven.pylos.game.PylosGameState;
import java.util.ArrayList;
import java.util.Collections;
import be.kuleuven.pylos.player.Action.*;
import java.util.Random;

public class StudentPlayer_VictorIndra extends PylosPlayer {
    // ---------------------------------------------------------------------------------------
    private int diepte = 9; //aantal lagen dat we diep kijken | 9: +-1.3s/game, 8: +-0.3s/game
    // ---------------------------------------------------------------------------------------

	@Override
	public void doMove(PylosGameIF game, PylosBoard board) {
		Action bestAction = getBestMove(game, board);
		bestAction.execute(game);
	}

	@Override
	public void doRemove(PylosGameIF game, PylosBoard board) {
		Action bestAction = getBestMove(game, board);
		bestAction.execute(game);
	}

	@Override
	public void doRemoveOrPass(PylosGameIF game, PylosBoard board) {
		Action bestAction = getBestMove(game, board);
		bestAction.execute(game);
	}

	private Action getBestMove(PylosGameIF game, PylosBoard board) {
		PylosGameSimulator simulator = new PylosGameSimulator(game.getState(), this.PLAYER_COLOR, board);
        SearchTree.MAX_DEPTH = diepte;
		SearchTree tree = new SearchTree(1, simulator, board, this, game, null,Integer.MIN_VALUE, Integer.MAX_VALUE, true,  this.PLAYER_COLOR,this.getRandom()); // 6
		Action action = tree.getBestAction(this.getRandom());


		return action;

	}
}





class Evaluator {
	private static int EXCESS_RESERVE_SCORE = 10; // multiplier voor het aantal ballen meer dan de tegenstander
	private static int WIN_BONUS = 5;
	private static int LOSE_PENALTY = -5;

	public static int evaluate(PylosBoard board, PylosPlayerColor playerColor) {
		int score = 0;
		int reserveOwn = board.getReservesSize(playerColor);
		int reserveOther = board.getReservesSize(playerColor.other());
		// telt het aantal bollen in reserve meer dan de tegenstand
		int difference = reserveOwn - reserveOther;
		score = difference * EXCESS_RESERVE_SCORE;

		// als al winnaar bepaald?
		//maakt weinig verschil als daar bonus voor geeft of penalty
		// if(reserveOwn == 0){
		// 	score += WIN_BONUS;
		// }
		// else if(reserveOther == 0){
		// 	score -= LOSE_PENALTY;
		// }
		return score;
	}


}




class SearchTree {
    public Action action; // private
    private int score = 0;
    public ArrayList<SearchTree> nodes; // private

    Action bestAction = null;

    public static int MAX_DEPTH = 9; // diepte kan hier aanpassen: 8

    public SearchTree(int currentLayer, PylosGameSimulator sim, PylosBoard board, PylosPlayer player,
            PylosGameIF game, Action a, int alpha, int beta, boolean isMaximizingPlayer, PylosPlayerColor playerColor, Random playerRandom) {
        nodes = new ArrayList<>();
        action = a; // actie om in die node te geraken

        if (MAX_DEPTH <= currentLayer || sim.getState() == PylosGameState.COMPLETED
                || (board.getReservesSize(player.PLAYER_COLOR) == 0 && sim.getState() != PylosGameState.REMOVE_FIRST)) {
            score = Evaluator.evaluate(board, playerColor);
        } else {

            ArrayList<Action> possibleActions = getAllPossibleActions(board, sim, player);
            Collections.shuffle(possibleActions, playerRandom);
            
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
                        SearchTree tree = new SearchTree(next_layer, sim, board, p, game, action, alpha, beta,
                                isMaximizingPlayer_new, playerColor, playerRandom);
                        int eval = tree.getScore();
                        nodes.add(tree);

                        if (eval > maxEval) {
                            maxEval = eval;
                            bestAction = action;
                            score = eval;
                        }

                        alpha = Math.max(alpha, eval);
                        action.undoSimulate(sim);

                        if (beta <= eval) {
                            
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
                        SearchTree tree = new SearchTree(next_layer, sim, board, p, game, action, alpha, beta,
                                isMaximizingPlayer_new, playerColor, playerRandom);
                        int eval = tree.getScore();
                        nodes.add(tree);

                        if (eval < minEval) {
                            minEval = eval;
                            bestAction = action;
                            score = eval;
                        }

                        beta = Math.min(beta, eval);
                        action.undoSimulate(sim);

                        if (eval < alpha) {
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

    public Action getBestAction(Random playerRandom) {
        //kan wrs ook gebruik maken van bestAction rechtstreeks => nog testen

        // TreeVisualizer.showTree(this);
        if (nodes.size() == 0) {
            System.out.println("info: " + action);
            // TreeVisualizer.showTree(this);
        }

        ArrayList<SearchTree> opties = new ArrayList<>();
        for (SearchTree s : nodes) {
            if (s.getScore() == score) {
                opties.add(s);
            }
        }

        // randomizer voor als er meerdere acties zijn met dezelfde score.
        assert (opties.size() != 0);
        // tijdelijk comment om beter te kunnen vergelijken, later terug eruit
        Collections.shuffle(opties, playerRandom);
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
