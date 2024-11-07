package be.kuleuven.pylos.player;

import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.game.PylosPlayerColor;

public class EvaluatorV5 {
	private static int EXCESS_RESERVE_SCORE = 1; // multiplier voor het aantal ballen meer dan de tegenstander
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
