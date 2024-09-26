package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.game.PylosGame;
import be.kuleuven.pylos.game.PylosGameIF;
import be.kuleuven.pylos.game.PylosLocation;
import be.kuleuven.pylos.game.PylosSphere;
import be.kuleuven.pylos.player.PylosPlayer;

/**
 * Created by Jan on 20/02/2015.
 */
public class StudentPlayer extends PylosPlayer {

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
	}

	@Override
	public void doRemove(PylosGameIF game, PylosBoard board) {
		/*
		 * game methods
		 * game.removeSphere(mySphere);
		 */
	}

	@Override
	public void doRemoveOrPass(PylosGameIF game, PylosBoard board) {
		/*
		 * game methods
		 * game.removeSphere(mySphere);
		 * game.pass()
		 */
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

		// ...

		public Action(PylosSphere sphere, PylosLocation to, PylosLocation from, ActionType type) {
			this.SPHERE = sphere;
			this.TO = to;
			this.FROM = from;
			this.TYPE = type;
		}

		public void execute(PylosGameIF game) {
			if (TYPE == ActionType.MOVE) {
				moveTo(game);
			} else if (TYPE == ActionType.REMOVE) {
				removeSphere(game);
			} else if (TYPE == ActionType.PASS) {
				pass(game);
			}
		}

		private void moveTo(PylosGameIF game) {
			game.moveSphere(SPHERE, TO);
		}

		private void removeSphere(PylosGameIF game) {
			game.removeSphere(SPHERE);
		}

		private void pass(PylosGameIF game) {
			game.pass();
		}
	}

	public enum ActionType {
		MOVE,
		REMOVE,
		PASS;
	}
}
