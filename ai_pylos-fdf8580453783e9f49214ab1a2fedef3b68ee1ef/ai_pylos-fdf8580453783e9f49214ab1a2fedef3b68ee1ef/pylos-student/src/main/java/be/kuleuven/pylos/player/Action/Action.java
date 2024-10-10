package be.kuleuven.pylos.player.Action;
import be.kuleuven.pylos.game.PylosGameIF;
import be.kuleuven.pylos.game.PylosLocation;
import be.kuleuven.pylos.game.PylosSphere;
import be.kuleuven.pylos.game.PylosGameSimulator;

public class Action {
    // later: lijst van alle mogelijke toekomstige moves (eigen moves en moves tegenstander, dus ook kleur meegeven)
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

    public void execute(PylosGameIF game){
      if(TYPE == ActionType.MOVE){
        moveTo(game);
      }
      else if(TYPE == ActionType.REMOVE){
        removeSphere(game);
      }
      else if(TYPE == ActionType.PASS){
        pass(game);
      }
    }

    private void moveTo(PylosGameIF game) {
      game.moveSphere(SPHERE, TO);
    }

    private void removeSphere(PylosGameIF game) {
      game.removeSphere(SPHERE);
    }

    private void pass(PylosGameIF game){
      game.pass();
    }

    public void simulate(PylosGameSimulator sim){
      if(TYPE == ActionType.MOVE){
        sim.moveSphere(SPHERE, TO);
      }
      else if(TYPE == ActionType.REMOVE){
        sim.removeSphere(SPHERE);
      }
      else if(TYPE == ActionType.PASS){
        sim.pass();
      }
    }

  }




