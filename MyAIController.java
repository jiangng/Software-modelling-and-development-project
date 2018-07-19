package mycontroller;

import java.util.HashMap;
import controller.CarController;
import mycontroller.strategies.*;
import tiles.MapTile;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;

public class MyAIController extends CarController {

	private boolean isFollowingWall = false; // This is initialized when the car sticks to a wall.
	private WorldSpatial.RelativeDirection lastTurnDirection = null; // Shows the last turn direction the car takes.

	private boolean isTurningLeft = false;
	private boolean isTurningRight = false;
	private Coordinate currentPosition;

	private WorldSpatial.Direction previousState = null; // Keeps track of the previous state(orientation)
	private boolean justChangedState = false; // Indicates the car just makes a turn (90 degree)
	private GameMap latestGameMap; //GameMap keeps being updated as the car explores the map

	// Car Speed to move at
	public final float MAX_CAR_SPEED = 3;
	public final float MAX_TURNING_SPEED = 1.4f; //Where an obstacle is ahead
	public final float MIN_CORNER_SPEED = 1.15f; //Where an obstacle is ahead
	public final float MIN_ROTATING_SPEED = 0.5f; //Where the obstacles on the following side end

	public final int TILE_FOLLOWING_SENSITIVITY = 2; // Max tile distance b/w the car and the tagged obstacles
	public final int DISTANCE_TO_TURN = 1; // THe car can only turn when the obstacle is 1 tile ahead
	public final int DISTANCE_TO_SLOW_DOWN = getViewSquare();  //Slow down car when an obstacle is 4 tiles ahead

	// Offset used to differentiate between 0 and 360 degrees
	private int EAST_THRESHOLD = 3;

	private StrategyFactory strategyFactory;
	private CarControllerStrategy carNavigationStrategy; //currentStrategy employed
	//What happens when the car approaches/reaches the turning point
	private CarControllerStrategy.CarControllerActions actionAtTurningPoint = null; 

	public enum Strategies {
		FOLLOWLEFTWALL, FOLLOWRIGHTWALL, GOTHROUGHLAVA, HEALING
	}

	public MyAIController(Car car){
		super(car);
		latestGameMap = new GameMap(getMap(), getKey()-1);

		//default to following left wall when simulation starts 
		strategyFactory = new StrategyFactory();
		carNavigationStrategy = strategyFactory.createCarStrategy(TILE_FOLLOWING_SENSITIVITY,
				DISTANCE_TO_SLOW_DOWN, Strategies.FOLLOWLEFTWALL);
	}

	@Override
	public void update(float delta) {
		// Gets what the car can see
		HashMap<Coordinate, MapTile> currentView = getView();
		currentPosition = updateCoordinate();
		getLatestGameMap().updateMap(currentView);
		checkStateChange();

		// If you are not following a wall initially, find a wall to stick to!
		if (!isFollowingWall) {
			if (getSpeed() < MAX_CAR_SPEED) {
				applyForwardAcceleration();
			}
			// Turn towards the north
			if (!getOrientation().equals(WorldSpatial.Direction.NORTH)) {
				setLastTurnDirection(WorldSpatial.RelativeDirection.LEFT);
				applyLeftTurn(getOrientation(), delta);
			}

			int distToObstacleAhead = ((PathExplorerStrategy) carNavigationStrategy).checkDistToObstacleAhead(
					WorldSpatial.Direction.NORTH, currentView, currentPosition,
					((PathExplorerStrategy) carNavigationStrategy).getTilesToAvoid());

			if (distToObstacleAhead <= DISTANCE_TO_SLOW_DOWN && distToObstacleAhead > DISTANCE_TO_TURN) {
				if (getSpeed() > MAX_TURNING_SPEED)
					applyReverseAcceleration();
			}

			if (distToObstacleAhead == DISTANCE_TO_TURN) {
				// Turn right until we go back to east!
				if (!getOrientation().equals(WorldSpatial.Direction.EAST)) {
					setLastTurnDirection(WorldSpatial.RelativeDirection.RIGHT);
					applyRightTurn(getOrientation(), delta);
				} else {
					isFollowingWall = true;
				}
			}
		}

		// Once the car is already stuck to a wall, apply the following logic
		else {
			// Readjust the car if it is misaligned.
			readjust(getLastTurnDirection(), delta);

			//Will automatically stops turning when the car makes a 90 degree turn
			if (getIsTurningRight()) {
				applyRightTurn(getOrientation(), delta);
			}

			//Will automatically stops turning when the car makes a 90 degree turn
			else if (getIsTurningLeft()) {
				applyLeftTurn(getOrientation(), delta);
			}

			else {
				//Changes PathExplorerStrategy when the car tags along obstacles on the other side
				if (((PathExplorerStrategy) carNavigationStrategy).changeStrategyNow()) {
					carNavigationStrategy = strategyFactory.changeCarStrategy(TILE_FOLLOWING_SENSITIVITY,
							DISTANCE_TO_SLOW_DOWN);
				}

				//Adding/Removing coordinates from/to obstaclesToFollow and followedObstacles
				strategyFactory.registerTilesToFollow(currentView, getOrientation(), currentPosition);
				strategyFactory.deregisterFollowedObstacles(currentView, getOrientation(), currentPosition,
						((PathExplorerStrategy) carNavigationStrategy).getTilesToAvoid());
				
				//Checks when the car is changing its PathExplorerStrategy
				actionAtTurningPoint = strategyFactory.monitorStrategyChange(this, actionAtTurningPoint, currentView, getOrientation(),
						currentPosition);
				//Current strategy will decide the next action for the carController 
				((PathExplorerStrategy) carNavigationStrategy).decideAction(this);
			}
		}
	}

	/**
	 * Note: Trying implementing moving away from wall if crashed Readjust the car
	 * to the orientation we are in.
	 * 
	 * @param lastTurnDirection
	 * @param delta
	 */
	private void readjust(WorldSpatial.RelativeDirection lastTurnDirection, float delta) {
		if (lastTurnDirection != null) {
			if (!isTurningRight && lastTurnDirection.equals(WorldSpatial.RelativeDirection.RIGHT)) {
				adjustRight(getOrientation(), delta);
			} else if (!isTurningLeft && lastTurnDirection.equals(WorldSpatial.RelativeDirection.LEFT)) {
				adjustLeft(getOrientation(), delta);
			}
		}
	}

	/**
	 * Try to orient myself to a degree that I was supposed to be at if I am
	 * misaligned.
	 */
	private void adjustLeft(WorldSpatial.Direction orientation, float delta) {

		switch (orientation) {
		case EAST:
			if (getAngle() > WorldSpatial.EAST_DEGREE_MIN + EAST_THRESHOLD) {
				turnRight(delta);
			}
			break;
		case NORTH:
			if (getAngle() > WorldSpatial.NORTH_DEGREE) {
				turnRight(delta);
			}
			break;
		case SOUTH:
			if (getAngle() > WorldSpatial.SOUTH_DEGREE) {
				turnRight(delta);
			}
			break;
		case WEST:
			if (getAngle() > WorldSpatial.WEST_DEGREE) {
				turnRight(delta);
			}
			break;

		default:
			break;
		}

	}

	private void adjustRight(WorldSpatial.Direction orientation, float delta) {
		switch (orientation) {
		case EAST:
			if (getAngle() > WorldSpatial.SOUTH_DEGREE && getAngle() < WorldSpatial.EAST_DEGREE_MAX) {
				turnLeft(delta);
			}
			break;
		case NORTH:
			if (getAngle() < WorldSpatial.NORTH_DEGREE) {
				turnLeft(delta);
			}
			break;
		case SOUTH:
			if (getAngle() < WorldSpatial.SOUTH_DEGREE) {
				turnLeft(delta);
			}
			break;
		case WEST:
			if (getAngle() < WorldSpatial.WEST_DEGREE) {
				turnLeft(delta);
			}
			break;
		default:
			break;
		}

	}

	/**
	 * Checks whether the car's state has changed or not, stops turning if it
	 * already has.
	 */
	private void checkStateChange() {
		if (previousState == null) {
			previousState = getOrientation();
		} else {
			if (previousState != getOrientation()) {
				if (isTurningLeft) {
					isTurningLeft = false;
				}
				if (isTurningRight) {
					isTurningRight = false;
				}
				previousState = getOrientation();
				setJustChangedState(true);
			}
		}
	}

	/**
	 * Turn the car counter clock wise (think of a compass going counter clock-wise)
	 */
	public void applyLeftTurn(WorldSpatial.Direction orientation, float delta) {
		switch (orientation) {
		case EAST:
			if (!getOrientation().equals(WorldSpatial.Direction.NORTH)) {
				turnLeft(delta);
			}
			break;
		case NORTH:
			if (!getOrientation().equals(WorldSpatial.Direction.WEST)) {
				turnLeft(delta);
			}
			break;
		case SOUTH:
			if (!getOrientation().equals(WorldSpatial.Direction.EAST)) {
				turnLeft(delta);
			}
			break;
		case WEST:
			if (!getOrientation().equals(WorldSpatial.Direction.SOUTH)) {
				turnLeft(delta);
			}
			break;
		default:
			break;

		}
		if (getSpeed() < MIN_CORNER_SPEED) {
			applyForwardAcceleration();
		} else if (getSpeed() > MAX_TURNING_SPEED) {
			applyReverseAcceleration();
		}
	}

	/**
	 * Turn the car clock wise (think of a compass going clock-wise)
	 */
	public void applyRightTurn(WorldSpatial.Direction orientation, float delta) {
		switch (orientation) {
		case EAST:
			if (!getOrientation().equals(WorldSpatial.Direction.SOUTH)) {
				turnRight(delta);
			}
			break;
		case NORTH:
			if (!getOrientation().equals(WorldSpatial.Direction.EAST)) {
				turnRight(delta);
			}
			break;
		case SOUTH:
			if (!getOrientation().equals(WorldSpatial.Direction.WEST)) {
				turnRight(delta);
			}
			break;
		case WEST:
			if (!getOrientation().equals(WorldSpatial.Direction.NORTH)) {
				turnRight(delta);
			}
			break;
		default:
			break;
		}
		if (getSpeed() > MAX_TURNING_SPEED) {
			applyReverseAcceleration();
		}
		// else if (carController.getSpeed() < carController.getMinCarSpeed()) {
		else if (getSpeed() < MIN_ROTATING_SPEED) {
			applyForwardAcceleration();
		}
	}

	public boolean justChangedState() {
		return justChangedState;
	}

	public void setCarNavigationStrategy(CarControllerStrategy strategy) {
		this.carNavigationStrategy = strategy;
	}

	public boolean getIsTurningLeft() {
		return isTurningLeft;
	}

	public boolean getIsTurningRight() {
		return isTurningRight;
	}

	public void setTurningLeft(boolean turningLeft) {
		isTurningLeft = turningLeft;
	}

	public void setTurningRight(boolean turningRight) {
		isTurningRight = turningRight;
	}

	public void setLastTurnDirection(WorldSpatial.RelativeDirection lastTurnDirection) {
		this.lastTurnDirection = lastTurnDirection;
	}

	public Coordinate getCurrentPosition() {
		return currentPosition;
	}

	private Coordinate updateCoordinate() {
		return new Coordinate(getPosition());
	}

	public void setJustChangedState(boolean justChangedState) {
		this.justChangedState = justChangedState;
	}

	public WorldSpatial.RelativeDirection getLastTurnDirection() {
		return lastTurnDirection;
	}

	public GameMap getLatestGameMap() {
		return latestGameMap;
	}

	public void setLatestGameMap(GameMap latestGameMap) {
		this.latestGameMap = latestGameMap;
	}

	public CarControllerStrategy.CarControllerActions getActionAtTurningPoint() {
		return actionAtTurningPoint;
	}

	public void setActionAtTurningPoint( CarControllerStrategy.	CarControllerActions action) {
		actionAtTurningPoint = action;
	}

}
