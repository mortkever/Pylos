package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.game.PylosGameIF;
import be.kuleuven.pylos.game.PylosGameSimulator;
import be.kuleuven.pylos.player.PylosPlayer;

import be.kuleuven.pylos.player.Action.Action;
import be.kuleuven.pylos.player.SearchTreev2;

public class StudentPlayerV2 extends PylosPlayer {

	int depth = 6;

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

		SearchTreev2 tree = new SearchTreev2(depth, 1, simulator, board, this, game, null, 1, Integer.MIN_VALUE,
				Integer.MAX_VALUE, this.PLAYER_COLOR); // 6
		Action action = tree.getBestAction(this);
		return action;

	}
}
