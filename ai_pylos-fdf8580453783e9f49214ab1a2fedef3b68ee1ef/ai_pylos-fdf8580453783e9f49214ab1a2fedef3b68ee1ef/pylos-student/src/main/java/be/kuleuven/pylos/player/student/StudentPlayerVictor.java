package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.game.PylosGame;
import be.kuleuven.pylos.game.PylosGameIF;
import be.kuleuven.pylos.game.PylosLocation;
import be.kuleuven.pylos.game.PylosSphere;
import be.kuleuven.pylos.game.PylosSquare;
import be.kuleuven.pylos.player.PylosPlayer;
import be.kuleuven.pylos.player.Action.Action;

/**
 * Created by Jan on 20/02/2015.
 */
public class StudentPlayerVictor extends PylosPlayer {

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

	public int evaluate(PylosBoard board){
		int score = 0;
		//telt het aantal bollen in reserve meer dan de tegenstand indien minder, is nul.
		if(board.getReservesSize(PLAYER_COLOR) > board.getReservesSize(OTHER)){
			score += board.getReservesSize(PLAYER_COLOR) - board.getReservesSize(OTHER);
		}
		
		//score op basis van de aanwezige vierkanten.
		PylosSquare[] squares = new PylosSquare[14];
		
		return score;
	}

}
