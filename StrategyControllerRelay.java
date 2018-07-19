package mycontroller;

import mycontroller.strategies.CarControllerStrategy;
import world.WorldSpatial;

/**
 * Group 39
 *
 * Example of pure fabrication, a form of indirection. We used a relay class
 * that calls the appropriate methods in MyAIController according to how the
 * Strategy decides the Controller should act.
 */
public class StrategyControllerRelay {

	private static StrategyControllerRelay instance;
	
	public static StrategyControllerRelay getInstance() {
		if (instance == null) {
			instance = new StrategyControllerRelay();
		}
		return instance;
	}
	/**
	 * Relay messages from Strategy to the AIController
	 * @param carController
	 * @param actionMessage
	 */
	public void changeState(MyAIController carController, CarControllerStrategy.CarControllerActions action) {
		switch (action) {
			case ACCELERATE:
				if (carController.getSpeed() < carController.MAX_CAR_SPEED) {
					carController.applyForwardAcceleration();
				}
				break;
			case SLOWDOWN:
				//Ensure the car is travelling around the maximum turning speed
				carController.applyForwardAcceleration();
				if (carController.getSpeed() > carController.MAX_TURNING_SPEED) {
					carController.applyReverseAcceleration();
				}
				break;
			case REVERSE:
				carController.applyReverseAcceleration();
				break;
			case ISTURNINGLEFT:
				carController.setLastTurnDirection(WorldSpatial.RelativeDirection.LEFT);
				carController.setTurningLeft(true);
				break;
			case ISTURNINGRIGHT:
				carController.setLastTurnDirection(WorldSpatial.RelativeDirection.RIGHT);
				carController.setTurningRight(true);
				break;
			default:
				break;
		}
	}
}