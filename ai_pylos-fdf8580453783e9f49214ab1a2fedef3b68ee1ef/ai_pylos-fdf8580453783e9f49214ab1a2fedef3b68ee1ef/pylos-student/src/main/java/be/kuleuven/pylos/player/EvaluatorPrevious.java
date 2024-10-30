package be.kuleuven.pylos.player;

import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.game.PylosLocation;
import be.kuleuven.pylos.game.PylosPlayerColor;
import be.kuleuven.pylos.game.PylosSphere;
import be.kuleuven.pylos.game.PylosSquare;

public class EvaluatorPrevious {

	// beetje gespeeld met getallen
	private static int OWN_SQUARE_SCORE = 200; // eigen vierkant gecreerd
	private static int POSSIBLE_STRANGE_SQUARE = 100; // potentiele vierkanten (3/4) met een mix aan kralen.
	private static int EXCESS_RESERVE_SCORE = 50; // multiplier voor het aantal ballen meer dan de tegenstander
	private static int TWO_REMOVE_BONUS_SCORE = 120; // bonus voor situatie met twee wegneembare kralen bij eigen
														// vierkant

	// Slechte staat van het bord, wordt de score voor verlaagd.
	private static int INCOMPLETE_OPPONENT_SQUARE_SCORE = -200; // vierkanten 3/4 kralen van de tegenstander, wordt
																// afgetrokken
	private static int STRANGE_SQUARE_SCORE = -50; // gemengde vierkanten, wordt afgetrokken

	// je bent hoger dan opponent
	private static int HIGHER_THAN_OPPONENT_SCORE = 200;

	// je bent lager dan opponent
	private static int LOWER_THAN_OPPONENT_SCORE = -200;

	// als op zelfde niveau da, zoveel mogelijk bollen op dat niveau
	// wordt vermenigvuldigt met aantal bollen op dat niveau
	// + als meer bollen dan opponent, - als minder bollen dan opponent
	private static int SAME_HEIGHT_DIFFERENT_AMOUNT = 20;

	// vanaf eigen kleur reservebollen kleiner dan START_ENDGAME_AMOUNT zijn
	// reservebollen meer waard
	private static int START_ENDGAME_AMOUNT = 6;
	// aantal keer reservebollen meer waard
	private static int WEIGHT_ENDGAME = 2;

	private static int CENTER_BONUS = 15;

	public static int evaluate(PylosBoard board, PylosPlayerColor currentColor) {
		int score = 0;
		PylosSquare[] squares = board.getAllSquares();
		PylosLocation[] locations = board.getLocations();

		for (PylosSquare square : squares) {
			// Altijd eerst andere vierkanten blokkeren
			if (square.getInSquare() == 3 && square.getInSquare(currentColor.other()) == 3) {
				//score = -20000;
				score += INCOMPLETE_OPPONENT_SQUARE_SCORE;
				// eventueel nog kijken of het een vierkant is waarbij hij twee bollen kan
				// wegnemen of 1 bol
				return score;
			}
			// Eigen vierkanten creëeren
			if (square.isSquare(currentColor) && !square.getTopLocation().isUsed()) {
				// score+= OWN_SQUARE_SCORE;
				int aantal = 0;
				for (PylosLocation loc : locations) {
					if (!loc.hasAbove() && loc.getSphere() != null && loc.getSphere().PLAYER_COLOR == currentColor)
						aantal++;
				}
				if (aantal > 1) {
					// score += TWO_REMOVE_BONUS_SCORE;
					// eigen vierkant gecreëerd en je kunt 2 bollen wegnemen
					score = 15000;
				} else {
					score = 10000;
				}
				return score;

			}

			// score maken
			// nog toevoegen: hoe hoger hoe beter, hoe centraler hoe beter
			// hoger dan tegenstander ook beter?
			// zorgen dat beste score bereikt hiermee, lager dan score die je krijgt om
			// andere te blokkeren
			int hoogte = 0;
			int aantal_hoogte = 0;
			int hoogte_opponent = 0;
			int aantal_hoogte_opponent = 0;
			// aantal in center is beter
			int center_amount = 0;
			for (PylosLocation loc : locations) {
				if (loc.getSphere() != null && loc.getSphere().PLAYER_COLOR == currentColor) {
					if (hoogte < loc.Z) { // zo krijg je max hoogte van eigen kleur
						hoogte = loc.Z;
						aantal_hoogte = 0;
					}
					if (hoogte == loc.Z) { // aantal van eigen kleur op die hoogte
						aantal_hoogte++;
					}
				} else if (loc.getSphere() != null) {
					if (hoogte_opponent < loc.Z) { // zo krijg je max hoogte van ander kleur
						hoogte_opponent = loc.Z;
						aantal_hoogte_opponent = 0;
					}
					if (hoogte_opponent == loc.Z) { // aantal van ander kleur op die hoogte
						aantal_hoogte_opponent++;
					}
				}
				if (loc.isUsed() && loc.getSphere().PLAYER_COLOR == currentColor) {
					PylosSphere sphere = loc.getSphere();
					PylosLocation location_sphere = sphere.getLocation();
					// Hardcoded voorlopig voor onderste laag
					// die 3 zou nog kunnen kijken naar welke hoogte en dan verste locatie
					if (location_sphere.X > 0 && location_sphere.Y > 0
							&& location_sphere.X < 3 && location_sphere.Y < 3) {
						center_amount++;
					}
				}
				if (hoogte > hoogte_opponent) {
					score += HIGHER_THAN_OPPONENT_SCORE;
				} else if (hoogte < hoogte_opponent) {
					score += LOWER_THAN_OPPONENT_SCORE;
				} else { // hoogtes gelijk
							// neg verschil als andere speler meer bollen op die hoogte, anders pos
					int verschil = aantal_hoogte - aantal_hoogte_opponent;
					score += (verschil * SAME_HEIGHT_DIFFERENT_AMOUNT);
				}
				score += center_amount * CENTER_BONUS;

				// telt het aantal bollen in reserve meer dan de tegenstand indien minder, is
				// nul.
				// in endgame is 1 extra reservebal meer waard dan in begin
				if (board.getReservesSize(currentColor) > board.getReservesSize(currentColor.other())) {
					int difference = (board.getReservesSize(currentColor)
							- board.getReservesSize(currentColor.other()));
					if (board.getReservesSize(currentColor) < START_ENDGAME_AMOUNT) {
						// als speler in endgame
						score += (difference * EXCESS_RESERVE_SCORE * WEIGHT_ENDGAME);
					} else {
						score += difference * EXCESS_RESERVE_SCORE;
					}
				}

				// score op basis van de aanwezige vierkanten.
				if (square.isSquare() && !square.getTopLocation().isUsed()
						&& square.getInSquare(currentColor.other()) != 4) {
					score += STRANGE_SQUARE_SCORE;
				} else if (square.getInSquare() == 3 && square.getInSquare(currentColor.other()) != 3) {
					score += POSSIBLE_STRANGE_SQUARE;
					// } else if (square.getInSquare() == 3 &&
					// square.getInSquare(currentColor.other()) == 3) {
					// score += INCOMPLETE_OPPONENT_SQUARE_SCORE;

				}

			}
		}
		// als al winnaar bepaald?

		return score;
	}
}
