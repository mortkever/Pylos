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

/**
 * Created by Jan on 20/02/2015.
 */
public class StudentPlayerIndra extends PylosPlayer {

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

		// get all spheres: MOET NOG AANGEPAST WORDEN
		// ArrayList<PylosSphere> possibleSpheres = new ArrayList<PylosSphere>();
		ArrayList<Action> possibleActions = new ArrayList<Action>();
		PylosSphere[] allSpheres = board.getSpheres(this.PLAYER_COLOR);
		// select used spheres

		// check if free to take => no spheres above
		int size = allSpheres.length;
		for (int i = 0; i < size; i++) {
			if (allSpheres[i].canRemove()) {
				// possibleSpheres.add(allSpheres[i]);
				possibleActions.add(new Action(allSpheres[i], null, null, ActionType.REMOVE));

			}
		}
		// get random action
		Random rand = new Random();
		;
		int randomNum = rand.nextInt(((possibleActions.size() - 1) - 0) + 1) + 0; // random integer between 0 and size

		// move sphere from location to reserve
		possibleActions.get(randomNum).execute(game);
	}

	@Override
	public void doRemoveOrPass(PylosGameIF game, PylosBoard board) {
		/*
		 * game methods
		 * game.removeSphere(mySphere);
		 * game.pass()
		 */

		/* always pass : moet nog aangepast worden */
		Action passAction = new Action(null, null, null, ActionType.PASS);
		passAction.execute(game);
	}

	private Action getBestMove(PylosGameIF game, PylosBoard board) {

		PylosGameSimulator simulator = new PylosGameSimulator(game.getState(), this.PLAYER_COLOR, board);

		ArrayList<Action> possibleActions = new ArrayList<Action>();
		// possible place actions
		PylosSphere sphere = board.getReserve(this);
		PylosLocation[] locations = board.getLocations();
		int size = locations.length;
		
		for (int i = 0; i < size; i++) {
			assert(locations[i] != null);
			if (locations[i].isUsable()) {
				possibleActions.add(new Action(sphere, locations[i], null, ActionType.MOVE));
			}
		}

		Action bestAction = null;
		int bestScore = -999999; // kan ook gwn score van eerste actie aan toekennen
		for (Action a : possibleActions) {
			a.simulate(simulator);
			int currentScore = evaluateBoard(game, board);
			if (currentScore > bestScore) {
				bestScore = currentScore;
				bestAction = a;
			}

			a.undoSimulate(simulator);
		}

		return bestAction;

	}

	private int evaluateBoard(PylosGameIF game, PylosBoard board) { // hoe hoger score hoe beter
		int currentPlayerReserves = board.getReservesSize(this.PLAYER_COLOR);
		int opponentPlayerReserves = board.getReservesSize(this.OTHER.PLAYER_COLOR);
		int score = currentPlayerReserves - opponentPlayerReserves;
		return score;
	}

	public static class Action {
		// later: lijst van alle mogelijke toekomstige moves (eigen moves en moves
		// tegenstander, dus ook kleur meegeven)
		// en dan daaruit later beste kan kiezen
		// of state (er was nog 1)

		ActionType TYPE;
		PylosSphere SPHERE;
		PylosLocation TO;
		PylosLocation FROM;

		PylosGameState prevState;
		PylosPlayerColor prevColor;

		// ...
		// reversesimulate
		// simulate
		// execute

		public Action(PylosSphere sphere, PylosLocation to, PylosLocation from, ActionType type) {
			this.SPHERE = sphere;
			this.TO = to;
			this.FROM = from;
			this.TYPE = type;
		}

		public void execute(PylosGameIF game) {
			if (TYPE == ActionType.MOVE) {
				game.moveSphere(SPHERE, TO);
			} else if (TYPE == ActionType.REMOVE) {
				game.removeSphere(SPHERE);
			} else if (TYPE == ActionType.PASS) {
				game.pass();
			}
		}

		// new
		public void simulate(PylosGameSimulator sim) {
			prevState = sim.getState(); // niet zeker of die twee juist
			prevColor = sim.getColor();
			if (TYPE == ActionType.MOVE) {
				sim.moveSphere(SPHERE, TO);
			} else if (TYPE == ActionType.REMOVE) {
				sim.removeSphere(SPHERE);
			} else if (TYPE == ActionType.PASS) {
				sim.pass();
			}
		}

		// nog undo
		public void undoSimulate(PylosGameSimulator sim) {
			if (TYPE == ActionType.MOVE) {
				sim.undoMoveSphere(SPHERE, FROM, prevState, prevColor);
			} else if (TYPE == ActionType.REMOVE) {
				sim.undoRemoveFirstSphere(SPHERE, FROM, prevState, prevColor);
			} else if (TYPE == ActionType.PASS) {
				sim.undoPass(prevState, prevColor);
			}
		}

	}

	public enum ActionType {
		MOVE,
		REMOVE,
		PASS;
	}
}
