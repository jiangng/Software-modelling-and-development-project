
package mycontroller.strategies;
import mycontroller.MyAIController;
import mycontroller.Sensor;
import mycontroller.TilesChecker;
import tiles.MapTile;
import utilities.Coordinate;
import world.WorldSpatial;
import world.WorldSpatial.Direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PathExplorerStrategy is one of the 2 superclasses implementing CarControllerStrategy.
 * PathExplorerStrategy consists of 2 subclasses: FollowLeftObstacle and FollowRightObstacle.
 * More information in the subclasses files
 * @author Group 39
 *
 */
public abstract class PathExplorerStrategy implements CarControllerStrategy {

	// Different strategies manipulate the behaviour of the sensor differently, so we need a
	// reference to it
	protected Sensor sensor;
	protected ArrayList<MapTile> tilesToAvoid;
	//A flag to notify the StrategyFactory to use the other PathExlorerStrategy
	protected boolean changeStrategyNow = false;
	//A threshold to slow down the car before reaching the turning point
	public final int DISTANCE_TO_CHECK_FOR_TURNING_POINT = 1;

	
	public abstract void decideAction(MyAIController carController);

	/**
	 * Adjusts the speed of the car before a turning event occurs and decides
	 * when the car should turn to avoid obstacle
	 * @param distToObstacle
	 * @param turningDirection: left or right
	 * @param maxDistToTurn (threshold)
	 * @param maxDistToSlowDown (threshold)
	 * @param followedTilesEndAhead 
	 * @return the action taken 
	 */
	public static CarControllerActions decideTurning(int distToObstacle,
			WorldSpatial.RelativeDirection turningDirection, int maxDistToTurn, int maxDistToSlowDown,
			boolean followedTilesEndAhead) {
		// If there is wall ahead, turn right!
		if (distToObstacle <= maxDistToTurn) {
			if (turningDirection == WorldSpatial.RelativeDirection.LEFT) {
				return CarControllerActions.ISTURNINGLEFT;
			} else {
				return CarControllerActions.ISTURNINGRIGHT;
			}
		}

		// Slow down the car when it's going to turn soon
		else if (distToObstacle <= maxDistToSlowDown || followedTilesEndAhead) {
			return CarControllerActions.SLOWDOWN;
		}

		else {
			return CarControllerActions.ACCELERATE;
		}
	}

	/**
	 * Adjusts the speed of the car before a turning event occurs and decides
	 * when the car should turn to avoid obstacle
	 * @param distToObstacle
	 * @param The relativeDirection the car takes when there is an obstacle ahead
	 * @param maxDistToTurn
	 * @param maxDistToSlowDown
	 * @return the action taken 
	 */
	public static CarControllerActions decideTurning(int distToObstacle,
			WorldSpatial.RelativeDirection turningDirection, int maxDistToTurn, int maxDistToSlowDown) {
		// If there is wall ahead, turn right!
		if (distToObstacle <= maxDistToTurn) {
			if (turningDirection == WorldSpatial.RelativeDirection.LEFT) {
				return CarControllerActions.ISTURNINGLEFT;
			} else {
				return CarControllerActions.ISTURNINGRIGHT;
			}
		}

		// Slow down the car when it's going to turn soon
		else if (distToObstacle <= maxDistToSlowDown) {
			return CarControllerActions.SLOWDOWN;
		}

		else {
			return CarControllerActions.ACCELERATE;
		}
	}

	/**
	 * Checks if the car is cruising along obstacles on the left/right (depends on which strategy)
	 * @param orientation
	 * @param currentView
	 * @param currentPosition
	 * @param tilesToAvoid
	 * @return is the car cruising along obstacles? 
	 */
	public abstract boolean checkFollowingObstacle(WorldSpatial.Direction orientation,
			HashMap<Coordinate, MapTile> currentView, Coordinate currentPosition, ArrayList<MapTile> tilesToAvoid);

	/**
	 * Checks how far the obstacle ahead is from the car
	 * @param orientation
	 * @param currentView
	 * @param currentPosition
	 * @param tilesToCheck
	 * @return how many road tiles between the car and the obstacle ahead?
	 */
	public int checkDistToObstacleAhead(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView,
			Coordinate currentPosition, ArrayList<MapTile> tilesToCheck) {
		return sensor.checkDistToObstacleAhead(orientation, currentView, currentPosition, tilesToCheck);
	}

	/**
	 * Check if the obstacles on the following side are going to end ahead
	 * If yes, the car will slow down before turning
	 * @param orientation
	 * @param currentView
	 * @param currentPosition
	 * @param tilesToCheck
	 * @return yes or no
	 */
	public abstract boolean peekCorner(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView,
			Coordinate currentPosition, ArrayList<MapTile> tilesToCheck);

	
	/**
	 * Checks if taking a turn when the obstacles on the following side end will end up before a deadend
	 * @param orientation
	 * @param currentView
	 * @param currentPosition
	 * @param tilesToAvoid
	 * @return yes or no
	 */
	public abstract boolean isDeadEnd(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView,
			Coordinate currentPosition, ArrayList<MapTile> tilesToAvoid);

	/**
	 * Get the view the car is able to see in the orientation of the following side of the car.
	 * @param currentView
	 * @param orientation
	 * @param currentPosition
	 * @return a linkedHashMap with 4 MapTile
	 */
	public abstract LinkedHashMap<Coordinate, MapTile> getOrientationViewInFollowingDirection(
			HashMap<Coordinate, MapTile> currentView, WorldSpatial.Direction orientation, Coordinate currentPosition);

	/**
	 * Get the coordinate of the obstacle the car is tagging along. 
	 * @param currentView
	 * @param orientation
	 * @param currentPosition
	 * @return Coordinate
	 */
	public Coordinate getFollowingObstacle(HashMap<Coordinate, MapTile> currentView, Direction orientation,
			Coordinate currentPosition) {

		LinkedHashMap<Coordinate, MapTile> viewInFollowingDirection = getOrientationViewInFollowingDirection(
				currentView, orientation, currentPosition);

		int i = 1;
		for (Map.Entry<Coordinate, MapTile> tileInView : viewInFollowingDirection.entrySet()) {
			for (MapTile tile : tilesToAvoid) {
				if (TilesChecker.checkTileSameType(tile, tileInView.getValue())
						&& i <= sensor.getTileFollowingSensitivity())
					return tileInView.getKey();
			}
			i++;
			
			//Car can't possibly tag along an obstacle farther than the tileFollowingSensitivity, hence stop checking
			if (i > sensor.getTileFollowingSensitivity()) {
				break;
			}
		}

		return null;
	}

	/**
	 * Returns the action taken by the car when it approaches/reaches the turningPoint to switch PathExplorerStrategy
	 * @param carController
	 * @param obstaclesToFollow
	 * @param orientation
	 * @param currentView
	 * @param currentPosition
	 * @return an action
	 */
	public abstract CarControllerActions findTurningPointForNewStrategy(MyAIController carController,
			ArrayList<Coordinate> obstaclesToFollow, WorldSpatial.Direction orientation,
			HashMap<Coordinate, MapTile> currentView, Coordinate currentPosition);

	public boolean changeStrategyNow() {
		return changeStrategyNow;
	}

	/**
	 * Return the coordinate of the nearest tile on the other side of the following side of the car
	 * @param currentView
	 * @param orientation
	 * @param currentPosition
	 * @return Coordinate
	 */
	public abstract Coordinate findTileOnOtherSide(HashMap<Coordinate, MapTile> currentView, Direction orientation,
			Coordinate currentPosition);
	
	public ArrayList<MapTile> getTilesToAvoid() {
		return tilesToAvoid;
	}
}
