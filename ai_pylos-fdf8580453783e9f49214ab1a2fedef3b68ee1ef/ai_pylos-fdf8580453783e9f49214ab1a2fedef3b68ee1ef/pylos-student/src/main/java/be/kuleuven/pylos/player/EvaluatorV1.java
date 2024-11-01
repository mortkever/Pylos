package be.kuleuven.pylos.player;

import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.game.PylosPlayerColor;

public class EvaluatorV1 {
	private static int EXCESS_RESERVE_SCORE = 1; // multiplier voor het aantal ballen meer dan de tegenstander

	public static int evaluate(PylosBoard board, PylosPlayerColor playerColor) {
		int score = 0;

		// telt het aantal bollen in reserve meer dan de tegenstand
		int difference = (board.getReservesSize(playerColor)
				- board.getReservesSize(playerColor.other()));
		score = difference * EXCESS_RESERVE_SCORE;

		// als al winnaar bepaald?

		return score;
	}
}
