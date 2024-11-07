package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.game.PylosGameIF;
import be.kuleuven.pylos.game.PylosGameType;
import be.kuleuven.pylos.game.PylosLocation;
import be.kuleuven.pylos.game.PylosSphere;
import be.kuleuven.pylos.player.PylosPlayer;

import be.kuleuven.pylos.player.Action.Action;
import be.kuleuven.pylos.player.Action.ActionType;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Ine on 5/05/2015.
 */
public class StudentPlayerRandomFit extends PylosPlayer {

  @Override
  public void doMove(PylosGameIF game, PylosBoard board) {
    /* add a reserve sphere to a feasible random location */
    // try {
    // assert false;
    // System.out.println("assertions are disabled");
    // } catch (AssertionError e) {
    // System.out.println("assertions are enabled");
    // }

    PylosSphere sphere = board.getReserve(this);
    PylosLocation[] locations = board.getLocations();
    int size = locations.length;
    ArrayList<Action> possibleActions = new ArrayList<Action>();
    for (int i = 0; i < size; i++) {
      if (locations[i].isUsable()) {
        possibleActions.add(new Action(sphere, locations[i], null, ActionType.MOVE));
      }
    }
    // https://stackoverflow.com/questions/363681/how-do-i-generate-random-integers-within-a-specific-range-in-java
    Random rand = new Random();
    ;
    int randomNum = rand.nextInt(((possibleActions.size() - 1) - 0) + 1) + 0; // random integer between 0 and size

    possibleActions.get(randomNum).execute(game);

  }

  @Override
  public void doRemove(PylosGameIF game, PylosBoard board) {
    /* removeSphere a random sphere */

    // get all spheres
    // ArrayList<PylosSphere> possibleSpheres = new ArrayList<PylosSphere>();
    ArrayList<Action> possibleActions = new ArrayList<Action>();
    PylosSphere[] allSpheres = board.getSpheres(this.PLAYER_COLOR);
    // select used spheres

    // check if free to take => no spheres above
    int size = allSpheres.length;
    for (int i = 0; i < size; i++) {
      if (allSpheres[i].canRemove()) {
        // possibleSpheres.add(allSpheres[i]);
        possibleActions.add(new Action(allSpheres[i], null, null, ActionType.REMOVE));

      }
    }
    // get random action
    Random rand = new Random();
    ;
    int randomNum = rand.nextInt(((possibleActions.size() - 1) - 0) + 1) + 0; // random integer between 0 and size

    // move sphere from location to reserve
    possibleActions.get(randomNum).execute(game);

  }

  @Override
  public void doRemoveOrPass(PylosGameIF game, PylosBoard board) {
    /* always pass */
    Action passAction = new Action(null, null, null, ActionType.PASS);
    passAction.execute(game);

  }
}