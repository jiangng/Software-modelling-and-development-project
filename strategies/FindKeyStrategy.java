package mycontroller.strategies;
import mycontroller.GameMap;
import mycontroller.MyAIController;
import mycontroller.AStarSearch.Node;
import mycontroller.StrategyControllerRelay;
import tiles.MapTile;
import utilities.Coordinate;

public class FindKeyStrategy extends GoalCompletionStrategy {

	private Node keyNode;

	public FindKeyStrategy(MyAIController c) {
        keyNode = findNextKey(c);
		startAStarSearch(keyNode, c);
		currentOrientation = c.getOrientation();
		routeInterpretor(path, currentOrientation);
	}
	
	@Override
	public void decideAction(MyAIController carController) {
        GameMap gameMap = carController.getLatestGameMap();
        Coordinate nextKeyCoordinate = gameMap.getNextKeyCoordinate();

        if (carController.getCurrentPosition().x == nextKeyCoordinate.x && carController.getCurrentPosition().y == nextKeyCoordinate.y) {
            keyNode = findNextKey(carController);
            startAStarSearch(keyNode, carController);
            currentOrientation = carController.getOrientation();
            routeInterpretor(path, currentOrientation);
        }
        else {
            CarControllerActions nextState = determineState(carController);
            StrategyControllerRelay.getInstance().changeState(carController, nextState);
        }
    }

	/**
	 * find the next key node to go to
	 * @param carController
	 * @return
	 */
    private Node findNextKey(MyAIController carController) {
		GameMap gameMap = carController.getLatestGameMap();
		Coordinate keyPosition = gameMap.getNextKeyCoordinate();
		MapTile keyTile = gameMap.getUpdatedMap().get(keyPosition).getTile();
		keyNode = new Node(keyPosition.x, keyPosition.y, keyTile);
		return keyNode;
	}
}
