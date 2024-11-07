package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.game.PylosGameIF;
import be.kuleuven.pylos.game.PylosGameSimulator;
import be.kuleuven.pylos.player.PylosPlayer;

import be.kuleuven.pylos.player.Action.Action;
import be.kuleuven.pylos.player.SearchTreev2;
import be.kuleuven.pylos.player.SearchTreev5;

public class StudentPlayerV5 extends PylosPlayer {


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
		SearchTreev5 tree = new SearchTreev5(1, simulator, board, this, game, null, Integer.MIN_VALUE, Integer.MAX_VALUE, true,  this.PLAYER_COLOR); // 6
		Action action = tree.getBestAction(this);
		return action;

	}
}
