package mycontroller.strategies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import mycontroller.MyAIController;
import mycontroller.TilesChecker;
import mycontroller.strategies.CarControllerStrategy.CarControllerActions;
import tiles.MapTile;
import utilities.Coordinate;
import world.WorldSpatial;

/**
 * StrategyFactory is responsible for deciding which strategy and when to create
 * for myAIController. It itself needs to keep track of several attributes in
 * order to know when to change strategies.
 * 
 * @author Group 39
 *
 */
public class StrategyFactory {

	private ArrayList<Coordinate> obstaclesToFollow = new ArrayList<>();
	private ArrayList<Coordinate> followedObstacles = new ArrayList<>();
	private MyAIController.Strategies currentStrategyName = null;
	private CarControllerStrategy currentStrategy = null;
	// A point to indicate the car has made a loop
	// If the car reaches switchingPoint, StrategyFactory will start looking for coords in obstaclesToFollow to switch 
	//between FollowLeftObstacle and FollowRightObstacle
	private Coordinate switchingPoint = null;
	// A flag to prevent it from responding to a newly registered switching point immediately
	private boolean justFoundSwitchingPoint = false;
	// A flag to tell it to find any remaining coords in obstaclesToFollow 
	// The car will TURN away from the turning point and then implement the new strategy
	private boolean searchForTurningPoint = false;

	// TODO : add the other strategies
	/**
	 * Creates any strategy available by calling the respective contructor and returns it to myAIController.
	 * @param tileFollowingSensitivity
	 * @param distToSlowDown
	 * @param strategyName
	 * @return
	 */
	public CarControllerStrategy createCarStrategy(int tileFollowingSensitivity, int distToSlowDown,
			MyAIController.Strategies strategyName) {

		CarControllerStrategy newStrategy = null;
		switch (strategyName) {
		case FOLLOWLEFTWALL:
			currentStrategyName = MyAIController.Strategies.FOLLOWLEFTWALL;
			newStrategy = new FollowLeftObstacleStrategy(tileFollowingSensitivity, distToSlowDown);
			break;
		case FOLLOWRIGHTWALL:
			currentStrategyName = MyAIController.Strategies.FOLLOWRIGHTWALL;
			newStrategy = new FollowRightObstacleStrategy(tileFollowingSensitivity, distToSlowDown);
			break;
		case GOTHROUGHLAVA:
			currentStrategyName = MyAIController.Strategies.GOTHROUGHLAVA;
			newStrategy = new GoThroughLavaStrategy(this, tileFollowingSensitivity, distToSlowDown);
			break;
		default:
			break;
		}

		this.currentStrategy = newStrategy;
		return newStrategy;
	}

	/**
	 * Switches between FollowLeftObstacleStrategy and FollowRightObstacleStrategy during the path exploring stage
	 * @param tileFollowingSensitivity
	 * @param distToSlowDown
	 * @return
	 */
	public CarControllerStrategy changeCarStrategy(int tileFollowingSensitivity, int distToSlowDown) {
		// Sets it null to get a new switching point when a new strategy is created.
		switchingPoint = null;

		if (currentStrategyName == MyAIController.Strategies.FOLLOWLEFTWALL) {
			currentStrategyName = MyAIController.Strategies.FOLLOWRIGHTWALL;
			currentStrategy = new FollowRightObstacleStrategy(tileFollowingSensitivity, distToSlowDown);
			return currentStrategy;
		}

		else if (currentStrategyName == MyAIController.Strategies.FOLLOWRIGHTWALL) {
			currentStrategyName = MyAIController.Strategies.FOLLOWLEFTWALL;
			currentStrategy = new FollowLeftObstacleStrategy(tileFollowingSensitivity, distToSlowDown);
			return currentStrategy;
		}

		return null;
	}

	/**
	 * While using FollowLeftObstacleStrategy, this method will record down the coordinates of obstacles on the car's right 
	 * so that the car can switch to FollowRightObstacleStrategy after tagging all obstacles on the left to explore these 
	 * coords on the right later.
	 * @param currentView
	 * @param orientation the car is driving towards
	 * @param currentPosition of the car
	 */
	public void registerTilesToFollow(HashMap<Coordinate, MapTile> currentView, WorldSpatial.Direction orientation,
			Coordinate currentPosition) {

		Coordinate tileCoordinate = ((PathExplorerStrategy) currentStrategy).findTileOnOtherSide(currentView,
				orientation, currentPosition);

		if (tileCoordinate != null) {
			//Only add the coords in obstaclesToFollow if they have not been tagged
			if (!obstaclesToFollow.contains(tileCoordinate) && !followedObstacles.contains(tileCoordinate)) {
				obstaclesToFollow.add(tileCoordinate);
			}
		}
	}

	/**
	 * Removes the coord of the obstacle being followed currently from obstaclesToFollow (if present) and adds it to 
	 * followedObstacles list. This list is to prevent already tagged obstacles from being added to obstaclesToFollow.
	 * @param currentView
	 * @param orientation the car is driving towards
	 * @param currentPosition of the car
	 * @param types of tiles that can be followed/tagged along
	 */
	public void deregisterFollowedObstacles(HashMap<Coordinate, MapTile> currentView,
			WorldSpatial.Direction orientation, Coordinate currentPosition, ArrayList<MapTile> tilesToCheck) {

		LinkedHashMap<Coordinate, MapTile> viewInFollowingDirection = ((PathExplorerStrategy) currentStrategy)
				.getOrientationViewInFollowingDirection(currentView, orientation, currentPosition);

		for (Map.Entry<Coordinate, MapTile> tileInView : viewInFollowingDirection.entrySet()) {
			for (MapTile tile : tilesToCheck) {
				if (TilesChecker.checkTileSameType(tileInView.getValue(), tile)) {
					obstaclesToFollow.remove(tileInView.getKey());

					if (!followedObstacles.contains(tileInView.getKey())) {
						followedObstacles.add(tileInView.getKey());
					}

					return;
				}
			}
		}
	}

	/**
	 * Keeps track of obstaclesToFollow in order to know when to turn away from the currently tagged obstacles to
	 * implement a new PathExplorerStrategy.
	 * @param carController
	 * @param actionAtTurningPoint: the action taken when the car approaches/reaches the turning point
	 * @param currentView
	 * @param orientation the car is driving towards
	 * @param currentPosition of the car
	 * @return
	 */
	public CarControllerActions monitorStrategyChange(MyAIController carController,
			CarControllerActions actionAtTurningPoint, HashMap<Coordinate, MapTile> currentView,
			WorldSpatial.Direction orientation, Coordinate currentPosition) {
		Coordinate currentFollowingObstacle = ((PathExplorerStrategy) currentStrategy).getFollowingObstacle(currentView,
				orientation, currentPosition);
		
		//Initialises the switching point when a new PathExplorerStrategy is created
		if (currentFollowingObstacle != null && switchingPoint == null) {
			switchingPoint = currentFollowingObstacle;
			justFoundSwitchingPoint = true;
		}

		//Prevents it from falsely recognising the switching point 
		if (justFoundSwitchingPoint && currentFollowingObstacle != null
				&& !switchingPoint.equals(currentFollowingObstacle)) {
			justFoundSwitchingPoint = false;
		}

		//Determines if the switching point is reached
		//If yes, start searching for the turning point
		if (!justFoundSwitchingPoint && actionAtTurningPoint == null && currentFollowingObstacle != null
				&& switchingPoint.equals(currentFollowingObstacle)) {
			searchForTurningPoint = true;
		}

		//Searching for the turning point
		if (searchForTurningPoint) {
			actionAtTurningPoint = ((PathExplorerStrategy) currentStrategy).findTurningPointForNewStrategy(carController,
					getObstaclesToFollow(), orientation, currentView, currentPosition);
			if (actionAtTurningPoint == CarControllerActions.ISTURNINGRIGHT
					|| actionAtTurningPoint == CarControllerActions.ISTURNINGLEFT) {
				searchForTurningPoint = false;
			}
		}

		return actionAtTurningPoint;
	}

	public Coordinate getSwitchingPoint() {
		return switchingPoint;
	}

	public void setSwitchingPoint(Coordinate switchingPoint) {
		this.switchingPoint = switchingPoint;
	}

	public ArrayList<Coordinate> getObstaclesToFollow() {
		return obstaclesToFollow;
	}

	public void setObstaclesToFollow(ArrayList<Coordinate> obstaclesToFollow) {
		this.obstaclesToFollow = obstaclesToFollow;
	}

	public MyAIController.Strategies getCurrentStrategyName() {
		return currentStrategyName;
	}
}
