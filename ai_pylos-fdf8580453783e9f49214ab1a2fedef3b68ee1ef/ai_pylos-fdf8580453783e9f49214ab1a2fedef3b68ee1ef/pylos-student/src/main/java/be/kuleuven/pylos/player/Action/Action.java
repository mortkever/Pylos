package be.kuleuven.pylos.player.Action;

import be.kuleuven.pylos.game.PylosGameIF;
import be.kuleuven.pylos.game.PylosLocation;
import be.kuleuven.pylos.game.PylosSphere;
import be.kuleuven.pylos.game.PylosGameSimulator;
import be.kuleuven.pylos.game.PylosGameState;

public class Action {
  // later: lijst van alle mogelijke toekomstige moves (eigen moves en moves
  // tegenstander, dus ook kleur meegeven)
  // en dan daaruit later beste kan kiezen
  // of state (er was nog 1)

  ActionType TYPE;
  PylosSphere SPHERE;
  PylosLocation TO;
  PylosLocation FROM;
  PylosGameState prevState;

  // ...

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

  public void simulate(PylosGameSimulator sim) {
    if (TYPE == ActionType.MOVE) {
      sim.moveSphere(SPHERE, TO);
    } else if (TYPE == ActionType.REMOVE) {
      sim.removeSphere(SPHERE);
    } else if (TYPE == ActionType.PASS) {
      sim.pass();
    }
  }

  public void undoSimulate(PylosGameSimulator sim) {
    if (TYPE == ActionType.MOVE) {
      if (prevState == PylosGameState.MOVE) {
        sim.undoMoveSphere(this.SPHERE, this.FROM, this.prevState, this.SPHERE.PLAYER_COLOR.other());
      } else if (this.FROM == null) {
        sim.undoAddSphere(SPHERE, this.prevState, this.SPHERE.PLAYER_COLOR.other());
      } else {
        assert false;
      }
    } else if (TYPE == ActionType.REMOVE) {
      if (prevState == PylosGameState.REMOVE_FIRST) {
        sim.undoRemoveFirstSphere(SPHERE, FROM, prevState, this.SPHERE.PLAYER_COLOR.other());
      } else if (prevState == PylosGameState.REMOVE_SECOND) {
        sim.undoRemoveSecondSphere(SPHERE, FROM, prevState, this.SPHERE.PLAYER_COLOR.other());
      } else {
        assert false;
      }
    } else if (TYPE == ActionType.PASS) {
      sim.undoPass(prevState, this.SPHERE.PLAYER_COLOR.other());
    } else {
      assert false;
    }
  }

}
