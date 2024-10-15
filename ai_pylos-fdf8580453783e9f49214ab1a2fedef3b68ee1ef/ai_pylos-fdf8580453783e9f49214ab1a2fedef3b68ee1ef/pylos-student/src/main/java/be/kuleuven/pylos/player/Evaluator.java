package be.kuleuven.pylos.player;

import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.game.PylosLocation;
import be.kuleuven.pylos.game.PylosPlayerColor;
import be.kuleuven.pylos.game.PylosSquare;

public class Evaluator {

	//beetje gespeeld met getallen
    private static int OWN_SQUARE_SCORE = 200; // eigen vierkant gecreerd
	private static int POSSIBLE_STRANGE_SQUARE = -100; // potentiele vierkanten (3/4) met een mix aan kralen.
	private static int EXCESS_RESERVE_SCORE = 50; // multiplier voor het aantal ballen meer dan de tegenstander
	private static int TWO_REMOVE_BONUS_SCORE = 120; // bonus voor situatie met twee wegneembare kralen bij eigen vierkant

	//Slechte staat van het bord, wordt de score voor verlaagd.
	private static int INCOMPLETE_OPPONENT_SQUARE_SCORE = -200; // vierkanten 3/4 kralen van de tegenstander, wordt
																// afgetrokken
	private static int STRANGE_SQUARE_SCORE = -50; // gemengde vierkanten, wordt afgetrokken

    public static int evaluate(PylosBoard board, PylosPlayerColor currentColor) {
		int score = 0;
		// telt het aantal bollen in reserve meer dan de tegenstand indien minder, is
		// nul.
		if (board.getReservesSize(currentColor) > board.getReservesSize(currentColor.other())) {
			score += (board.getReservesSize(currentColor) - board.getReservesSize(currentColor.other()))
					* EXCESS_RESERVE_SCORE;
		}

		// score op basis van de aanwezige vierkanten.
		PylosSquare[] squares;
		squares = board.getAllSquares();
		PylosLocation[] locations = board.getLocations();
		for (PylosSquare square : squares) {
			if (square.isSquare(currentColor) && !square.getTopLocation().isUsed()) {
				score+= OWN_SQUARE_SCORE;
				int aantal = 0;
				for (PylosLocation loc : locations) {
					if (!loc.hasAbove())
						aantal++;
				}
				if(aantal>1){
					score +=  TWO_REMOVE_BONUS_SCORE;
				}
			} else if (square.isSquare() && !square.getTopLocation().isUsed()) {
				score += STRANGE_SQUARE_SCORE;
			} else if (square.getInSquare() == 3 && square.getInSquare(currentColor.other()) != 3) {
				score += POSSIBLE_STRANGE_SQUARE;
			} else if (square.getInSquare() == 3 && square.getInSquare(currentColor.other()) == 3) {
				score += INCOMPLETE_OPPONENT_SQUARE_SCORE;
				
			}
		}

        //als al winnaar bepaald?

		return score;
	}
}
