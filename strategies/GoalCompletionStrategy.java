package mycontroller.strategies;

import mycontroller.AStarSearch.AStarSearch;
import mycontroller.HashMapTile;
import mycontroller.MyAIController;
import mycontroller.AStarSearch.Node;
import tiles.MapTile;
import utilities.Coordinate;
import world.World;
import world.WorldSpatial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class GoalCompletionStrategy implements CarControllerStrategy {

    protected ArrayList<MapTile> tilesToAvoid;
    protected Node carCurrentNode;
    protected WorldSpatial.Direction currentOrientation;
    protected List<Node> path;
    protected List<GoalCompletionStrategy.Movement> movement;

    public abstract void decideAction(MyAIController carController);
    
    public enum Movement {STRAIGHT, LEFT, RIGHT}

    /**
     * creating a star class and find the path here
     * @param targetNode
     * @param carController
     */
    public void startAStarSearch(Node targetNode, MyAIController carController) {
        HashMap<Coordinate, HashMapTile> updateMap = carController.getLatestGameMap().getUpdatedMap();
        int carX = carController.getCurrentPosition().x;
        int carY = carController.getCurrentPosition().y;
        carCurrentNode = new Node(carX, carY, updateMap.get(new Coordinate(carX, carY)).getTile());
        AStarSearch aStar = new AStarSearch(World.MAP_WIDTH, World.MAP_HEIGHT, carCurrentNode, targetNode, updateMap,
                tilesToAvoid);
        path = aStar.findPath();
    }

    /**
     * interprets the path as it would be a list of nodes,
     * return a list of movements for the car to follow
     * @param path
     * @param orientation
     */
    public void routeInterpretor(List<Node> path, WorldSpatial.Direction orientation) {
        while (path.size() > 1) {
            Node startPosition = path.get(0);
            Node nextPosition = path.get(1);
            movement.add(checkNextMove(startPosition, nextPosition));
            path.remove(0);
        }
    }

    /**
     * based on the orientation, spit out the next movement for the car
     * @param startPosition
     * @param nextPosition
     * @return
     */
    public GoalCompletionStrategy.Movement checkNextMove(Node startPosition, Node nextPosition) {
        int xChanges = startPosition.getX() - nextPosition.getX();
        int yChanges = startPosition.getY() - nextPosition.getY();
        switch (currentOrientation) {
            case SOUTH:
                if (xChanges < 0) {
                    // turn right
                    currentOrientation = WorldSpatial.Direction.WEST;
                    return GoalCompletionStrategy.Movement.RIGHT;
                } else if (xChanges > 0) {
                    // turnleft
                    currentOrientation = WorldSpatial.Direction.EAST;
                    return GoalCompletionStrategy.Movement.LEFT;
                } else {
                    // straight
                    return GoalCompletionStrategy.Movement.STRAIGHT;
                }
            case NORTH:
                if (xChanges < 0) {
                    // turn left
                    currentOrientation = WorldSpatial.Direction.WEST;
                    return GoalCompletionStrategy.Movement.LEFT;
                } else if (xChanges > 0) {
                    // turnright
                    currentOrientation = WorldSpatial.Direction.EAST;
                    return GoalCompletionStrategy.Movement.RIGHT;
                } else {
                    // straight
                    return GoalCompletionStrategy.Movement.STRAIGHT;
                }
            case EAST:
                if (yChanges < 0) {
                    // left
                    currentOrientation = WorldSpatial.Direction.SOUTH;
                    return GoalCompletionStrategy.Movement.RIGHT;
                } else if (yChanges > 0) {
                    // right
                    currentOrientation = WorldSpatial.Direction.NORTH;
                    return GoalCompletionStrategy.Movement.LEFT;
                } else {
                    // straight
                    return GoalCompletionStrategy.Movement.STRAIGHT;
                }
            case WEST:
                if (yChanges < 0) {
                    // left
                    currentOrientation = WorldSpatial.Direction.SOUTH;
                    return GoalCompletionStrategy.Movement.LEFT;
                } else if (yChanges > 0) {
                    // right
                    currentOrientation = WorldSpatial.Direction.NORTH;
                    return GoalCompletionStrategy.Movement.RIGHT;
                } else {
                    // straight
                    return GoalCompletionStrategy.Movement.STRAIGHT;
                }
        }
        return null;
    }

    public ArrayList<MapTile> getTilesToAvoid() {
        return tilesToAvoid;
    }

    /**
     * by referring to the movement list, change the state of the carcontroller
     * @param carController
     * @return
     */
    public CarControllerActions determineState(MyAIController carController) {
        CarControllerActions nextState;
        int distUntilTurn = getDistUntilNextTurn(carController.DISTANCE_TO_SLOW_DOWN);

        // If the car is to immediately turn, decide which way to turn
        if (movement.get(0) == Movement.LEFT || movement.get(0) == Movement.RIGHT) {
            Movement nextMovement = movement.remove(0);
            switch (nextMovement) {
                case LEFT:
                    nextState = CarControllerActions.ISTURNINGLEFT;
                    break;
                case RIGHT:
                    nextState = CarControllerActions.ISTURNINGRIGHT;
                    break;
                default:
                    nextState = CarControllerActions.ACCELERATE;
            }
        }
        //Prepare the car to turn
        else if (distUntilTurn <= carController.DISTANCE_TO_SLOW_DOWN) {
            nextState = CarControllerActions.SLOWDOWN;
        }

        else {
            nextState = CarControllerActions.ACCELERATE;
        }
        return nextState;
    }

    /**
     * check when is the next turning (LEFT/RIGHT)
     * if it is more than 4 movements away, return 4
     * @param distToSlowDown (<= 4)
     * @return
     */
    public int getDistUntilNextTurn(int distToSlowDown) {
        int i;
        for (i = 0; i < movement.size(); i++) {
            int j = i+1;
            if (j < movement.size()) {
                if (j > distToSlowDown) {
                    return distToSlowDown;
                }
                else if ((movement.get(j) == Movement.LEFT || movement.get(j) == Movement.RIGHT)) {
                    return j;
                }
            }
        }
        return i;
    }
}
