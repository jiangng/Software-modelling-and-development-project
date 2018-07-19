package mycontroller.strategies;

import mycontroller.MyAIController;
import tiles.MapTile;

import java.util.ArrayList;

/**
 * Group 39
 * Interface for basic strategies the CarController could be using.
 */
public interface CarControllerStrategy {

    enum CarControllerActions { ACCELERATE, SLOWDOWN, ISTURNINGLEFT, ISTURNINGRIGHT, REVERSE }
    
    void decideAction(MyAIController carController);
}
