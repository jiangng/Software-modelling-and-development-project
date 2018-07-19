package mycontroller.strategies;

import mycontroller.MyAIController;
import mycontroller.StrategyControllerRelay;
import tiles.LavaTrap;
import tiles.MapTile;
import utilities.Coordinate;
import world.WorldSpatial;
import world.WorldSpatial.Direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Group 39 Example of Composite Pattern regarding this particular navigation
 * Strategy. Since going through lava can involve either following the left or
 * right wall, we store both of these strategies locally in order to allow for
 * reusability of these behaviours.
 */
public class GoThroughLavaStrategy extends PathExplorerStrategy {

	private PathExplorerStrategy followLeftWallStrategy;
	private PathExplorerStrategy followRightWallStrategy;

	public GoThroughLavaStrategy(StrategyFactory s, int tileFollowingSensitivity, int distToSlowDown) {
		followLeftWallStrategy = (PathExplorerStrategy) s.createCarStrategy(tileFollowingSensitivity, distToSlowDown,
				MyAIController.Strategies.FOLLOWLEFTWALL);
		followRightWallStrategy = (PathExplorerStrategy) s.createCarStrategy(tileFollowingSensitivity, distToSlowDown,
				MyAIController.Strategies.FOLLOWRIGHTWALL);

		tilesToAvoid = new ArrayList<>();
		tilesToAvoid.add(new LavaTrap());
	}

	public void decideAction(MyAIController carController) {

		CarControllerStrategy.CarControllerActions nextState = null;

		// New action is relayed by the StrategyControllerRelay singleton to
		// MyAIController
		StrategyControllerRelay.getInstance().changeState(carController, nextState);
	}

	@Override
	public boolean isDeadEnd(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView,
			Coordinate currentPosition, ArrayList<MapTile> tilesToAvoid) {
		return false;
	}

	@Override
	public boolean checkFollowingObstacle(Direction orientation, HashMap<Coordinate, MapTile> currentView,
			Coordinate currentPosition, ArrayList<MapTile> tilesToAvoid) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean peekCorner(Direction orientation, HashMap<Coordinate, MapTile> currentView,
			Coordinate currentPosition, ArrayList<MapTile> tilesToCheck) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LinkedHashMap<Coordinate, MapTile> getOrientationViewInFollowingDirection(
			HashMap<Coordinate, MapTile> currentView, Direction orientation, Coordinate currentPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CarControllerActions findTurningPointForNewStrategy(MyAIController carController,
			ArrayList<Coordinate> obstaclesToFollow, Direction orientation, HashMap<Coordinate, MapTile> currentView,
			Coordinate currentPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Coordinate findTileOnOtherSide(HashMap<Coordinate, MapTile> currentView, Direction orientation,
			Coordinate currentPosition) {
		// TODO Auto-generated method stub
		return null;
	}
}
