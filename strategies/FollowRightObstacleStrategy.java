package mycontroller.strategies;

import mycontroller.*;
import tiles.LavaTrap;
import tiles.MapTile;
import utilities.Coordinate;
import world.WorldSpatial;
import world.WorldSpatial.Direction;

import java.util.*;

/**
 * This strategy allows the car to explore the map by tagging along obstacles so
 * that they are always on the right side of the car.
 * 
 * @author Group 39
 *
 */
public class FollowRightObstacleStrategy extends PathExplorerStrategy {

	public FollowRightObstacleStrategy(int tileFollowingSensitivity, int distToSlowDown) {
		sensor = new Sensor(tileFollowingSensitivity, distToSlowDown);
		super.tilesToAvoid = new ArrayList<>();
		tilesToAvoid.add(new MapTile(MapTile.Type.WALL));
		tilesToAvoid.add(new LavaTrap());
	}

	@Override
	public void decideAction(MyAIController carController) {

		PathExplorerStrategy.CarControllerActions nextState;

		// StrategyFactory has found the turning point hence telling the car to slow down
		// Hence return from this method as we don't want to interfere with the
		// instruction given by the factory
		if (carController.getActionAtTurningPoint() == CarControllerActions.SLOWDOWN) {
			return;
		}

		// When the car just finishes turning away from its current following obstacle
		// and is searching for an obstacle ahead to switch PathExplorerStrategy
		if (carController.getActionAtTurningPoint() != null && carController.justChangedState()) {
			int distToObstacle = checkDistToObstacleAhead(carController.getOrientation(), carController.getView(),
					carController.getCurrentPosition(), tilesToAvoid);
			// Turn right when an obstacle is ahead so that the obstacle will be on the left
			// in order to use followLeftWallStrategy.
			nextState = decideTurning(distToObstacle, WorldSpatial.RelativeDirection.RIGHT,
					carController.DISTANCE_TO_TURN, carController.DISTANCE_TO_SLOW_DOWN);

			//myAIController relays the message of changingStrategyNow to the strategyFactory
			if (nextState == CarControllerActions.ISTURNINGRIGHT) {
				carController.setActionAtTurningPoint(null);
				carController.setJustChangedState(false);
				changeStrategyNow = true;
			}
		}

		// Try to determine whether or not the car is next to an obstacle.
		else if (checkFollowingObstacle(carController.getOrientation(), carController.getView(),
				carController.getCurrentPosition(), tilesToAvoid)) {

			if (carController.justChangedState()) {
				carController.setJustChangedState(false);
			}

			int distToObstacle = checkDistToObstacleAhead(carController.getOrientation(), carController.getView(),
					carController.getCurrentPosition(), tilesToAvoid);
			boolean followedTilesEndAhead = peekCorner(carController.getOrientation(), carController.getView(),
					carController.getCurrentPosition(), tilesToAvoid);
			// If there is wall ahead, turn left!
			// Or slow down the car when it's going to turn left/right soon
			nextState = decideTurning(distToObstacle, WorldSpatial.RelativeDirection.LEFT,
					carController.DISTANCE_TO_TURN, carController.DISTANCE_TO_SLOW_DOWN, followedTilesEndAhead);
		}

		// Ensure the car can find a new obstacle to follow after turning right by
		// keeping it driving along the new orientation
		else if (carController.justChangedState()
				&& carController.getLastTurnDirection() == WorldSpatial.RelativeDirection.RIGHT) {
			nextState = CarControllerActions.SLOWDOWN;
		}

		// This indicates that the car can do a right turn if it is no longer cruising along an obstacle
		else {
			// Turn right if the car is not turning into a deadend
			if (!isDeadEnd(carController.getOrientation(), carController.getView(), carController.getCurrentPosition(),
					tilesToAvoid)) {
				nextState = CarControllerActions.ISTURNINGRIGHT;
			}

			// If it's a deadend, keep driving in the current orientation until the next turn
			else {
				int distToObstacle = checkDistToObstacleAhead(carController.getOrientation(), carController.getView(),
						carController.getCurrentPosition(), tilesToAvoid);
				nextState = decideTurning(distToObstacle, WorldSpatial.RelativeDirection.LEFT,
						carController.DISTANCE_TO_TURN, carController.DISTANCE_TO_SLOW_DOWN);
			}
		}

		// New action is relayed by the StrategyControllerRelay singleton to MyAIController
		StrategyControllerRelay.getInstance().changeState(carController, nextState);
	}

	public boolean checkFollowingObstacle(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView,
			Coordinate currentPosition, ArrayList<MapTile> tilesToAvoid) {
		return sensor.checkFollowingObstacle(orientation, currentView, WorldSpatial.RelativeDirection.RIGHT,
				currentPosition, tilesToAvoid);
	}

	@Override
	public boolean peekCorner(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView,
			Coordinate currentPosition, ArrayList<MapTile> tilesToCheck) {
		return sensor.peekCorner(orientation, currentView, currentPosition, WorldSpatial.RelativeDirection.RIGHT,
				tilesToCheck);
	}

	@Override
	public boolean isDeadEnd(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView,
			Coordinate currentPosition, ArrayList<MapTile> tilesToAvoid) {
		return sensor.isDeadEnd(orientation, currentView, WorldSpatial.RelativeDirection.RIGHT, currentPosition,
				tilesToAvoid);
	}

	@Override
	public LinkedHashMap<Coordinate, MapTile> getOrientationViewInFollowingDirection(
			HashMap<Coordinate, MapTile> currentView, Direction orientation, Coordinate currentPosition) {
		return sensor.getOrientationViewInFollowingDirection(currentView, orientation,
				WorldSpatial.RelativeDirection.RIGHT, currentPosition);
	}

	@Override
	public CarControllerActions findTurningPointForNewStrategy(MyAIController carController,
			ArrayList<Coordinate> obstaclesToFollow, WorldSpatial.Direction orientation,
			HashMap<Coordinate, MapTile> currentView, Coordinate currentPosition) {
		
		//Slow down the car before making a turn at the turning point
		if (carController.getSpeed() > carController.MAX_TURNING_SPEED) {
			StrategyControllerRelay.getInstance().changeState(carController, CarControllerActions.SLOWDOWN);
			return CarControllerActions.SLOWDOWN;
		} 
		
		// Simply get back to follow left wall and continue searching for remaining
		// obstaclesToFollow
		else {
			StrategyControllerRelay.getInstance().changeState(carController, CarControllerActions.ISTURNINGLEFT);
			return CarControllerActions.ISTURNINGLEFT;
		}
	}

	@Override
	public Coordinate findTileOnOtherSide(HashMap<Coordinate, MapTile> currentView, WorldSpatial.Direction orientation,
			Coordinate currentPosition) {
		switch (orientation) {
		case NORTH:
			return sensor.findClosestObstacleInOrientation(WorldSpatial.Direction.WEST, currentView, currentPosition,
					tilesToAvoid);
		case SOUTH:
			return sensor.findClosestObstacleInOrientation(WorldSpatial.Direction.EAST, currentView, currentPosition,
					tilesToAvoid);
		case EAST:
			return sensor.findClosestObstacleInOrientation(WorldSpatial.Direction.NORTH, currentView, currentPosition,
					tilesToAvoid);
		case WEST:
			return sensor.findClosestObstacleInOrientation(WorldSpatial.Direction.SOUTH, currentView, currentPosition,
					tilesToAvoid);
		default:
			return null;
		}
	}
}