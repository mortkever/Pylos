package be.kuleuven.pylos.player.student;

import java.util.ArrayList;
import java.util.Random;

import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.game.PylosGame;
import be.kuleuven.pylos.game.PylosGameIF;
import be.kuleuven.pylos.game.PylosGameSimulator;
import be.kuleuven.pylos.game.PylosGameState;
import be.kuleuven.pylos.game.PylosLocation;
import be.kuleuven.pylos.game.PylosPlayerColor;
import be.kuleuven.pylos.game.PylosSphere;
import be.kuleuven.pylos.player.PylosPlayer;

import be.kuleuven.pylos.player.Action.Action;
import be.kuleuven.pylos.player.Action.ActionType;
import be.kuleuven.pylos.player.SearchTreeNew;
import be.kuleuven.pylos.player.SearchTreePrevious;


public class StudentPlayerPrevious extends PylosPlayer {

	int depth = 3;
	@Override
	public void doMove(PylosGameIF game, PylosBoard board) {
		/*
		 * board methods
		 * PylosLocation[] allLocations = board.getLocations();
		 * PylosSphere[] allSpheres = board.getSpheres();
		 * PylosSphere[] mySpheres = board.getSpheres(this);
		 * PylosSphere myReserveSphere = board.getReserve(this);
		 */

		/*
		 * game methods
		 * game.moveSphere(myReserveSphere, allLocations[0]);
		 */

		Action bestAction = getBestMove(game, board);
		bestAction.execute(game);

	}

	@Override
	public void doRemove(PylosGameIF game, PylosBoard board) {
		/*
		 * game methods
		 * game.removeSphere(mySphere);
		 */

		Action bestAction = getBestMove(game, board);
		bestAction.execute(game);

		// // get all spheres: MOET NOG AANGEPAST WORDEN
		// // ArrayList<PylosSphere> possibleSpheres = new ArrayList<PylosSphere>();
		// ArrayList<Action> possibleActions = new ArrayList<Action>();
		// PylosSphere[] allSpheres = board.getSpheres(this.PLAYER_COLOR);
		// // select used spheres

		// // check if free to take => no spheres above
		// int size = allSpheres.length;
		// for (int i = 0; i < size; i++) {
		// 	if (allSpheres[i].canRemove()) {
		// 		// possibleSpheres.add(allSpheres[i]);
		// 		possibleActions.add(new Action(allSpheres[i], null, null, ActionType.REMOVE));

		// 	}
		// }
		// // get random action
		// Random rand = new Random();
		// ;
		// int randomNum = rand.nextInt(((possibleActions.size() - 1) - 0) + 1) + 0; // random integer between 0 and size

		// // move sphere from location to reserve
		// possibleActions.get(randomNum).execute(game);
	}

	@Override
	public void doRemoveOrPass(PylosGameIF game, PylosBoard board) {
		/*
		 * game methods
		 * game.removeSphere(mySphere);
		 * game.pass()
		 */
		Action bestAction = getBestMove(game, board);
		bestAction.execute(game);
		// /* always pass : moet nog aangepast worden */
		// Action passAction = new Action(null, null, null, ActionType.PASS);
		// passAction.execute(game);
	}

	private Action getBestMove(PylosGameIF game, PylosBoard board) {

		PylosGameSimulator simulator = new PylosGameSimulator(game.getState(), this.PLAYER_COLOR, board);

		//game toegevoegd
		//public SearchTree(int layers, int currentLayer, PylosGameSimulator sim, PylosBoard board, PylosPlayer player, PylosGame game)
		SearchTreePrevious tree = new SearchTreePrevious(depth, 1, simulator, board, this, game, null, 1, Integer.MIN_VALUE, Integer.MAX_VALUE); //6
		Action action = tree.getBestAction();
		return action;


	}



}
