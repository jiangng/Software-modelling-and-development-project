package mycontroller.AStarSearch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import mycontroller.HashMapTile;
import mycontroller.TilesChecker;
import utilities.Coordinate;
import tiles.MapTile;

/**
 * Node for A* Algorithm
 *
 * @version 2.0, 2017-02-23
 * @author Marcelo Surriabre
 */
public class AStarSearch {
    private static int DEFAULT_ROAD_COST = 10; // Horizontal - Vertical Cost
    private static int DEFAULT_LAVA_COST = 200;
    private int hvCost;
    private Node[][] searchArea;
    private PriorityQueue<Node> openList;
    private List<Node> closedList;
    private Node initialNode;
    private Node finalNode;
    private HashMap<Coordinate, HashMapTile> map;
    private ArrayList<MapTile> tilesToAvoid;

    public AStarSearch(int rows, int cols, Node initialNode, Node finalNode, int hvCost,
                       HashMap<Coordinate, HashMapTile> map, ArrayList<MapTile> tilesToAvoid) {
        this.hvCost = hvCost;
        this.map = map;
        setInitialNode(initialNode);
        setFinalNode(finalNode);
        this.searchArea = new Node[rows][cols];
        this.openList = new PriorityQueue<Node>(new Comparator<Node>() {
            @Override
            public int compare(Node node0, Node node1) {
                return node0.getF() < node1.getF() ? -1 : node0.getF() > node1.getF() ? 1 : 0;
            }
        });
        setNodes();
        this.closedList = new ArrayList<>();
        this.tilesToAvoid = tilesToAvoid;
    }

    public AStarSearch(int rows, int cols, Node initialNode, Node finalNode, HashMap<Coordinate, HashMapTile> map,
                       ArrayList<MapTile> tilesToAvoid) {
    	this(rows, cols, initialNode, finalNode, DEFAULT_ROAD_COST, map, tilesToAvoid);
    }

    /**
     * set the each tile in the map as a node 
     */
    private void setNodes() {
        for (int i = 0; i < searchArea.length; i++) {
            for (int j = 0; j < searchArea[0].length; j++) {
            	HashMapTile hashMapTile = map.get(new Coordinate(i,j));
                Node node = new Node(i, j, hashMapTile.getTile());
                node.calculateHeuristic(getFinalNode());
                this.searchArea[i][j] = node;
            }
        }
    }

    /**
     * this is the start of the a star find path method
     * @return new ArrayList
     */
    public List<Node> findPath() {
        openList.add(initialNode);
        while (!isEmpty(openList)) {
            Node currentNode = openList.poll();
            closedList.add(currentNode);
            if (isFinalNode(currentNode)) {
                return getPath(currentNode);
            } else {
                addAdjacentNodes(currentNode);
            }
        }
        return new ArrayList<>();
    }

    /**
     * set the current node as the parent and return the new path
     * @param currentNode
     * @return path as a list <Node>
     */
    private List<Node> getPath(Node currentNode) {
        List<Node> path = new ArrayList<Node>();
        path.add(currentNode);
        Node parent;
        while ((parent = currentNode.getParent()) != null) {
            path.add(0, parent);
            currentNode = parent;
        }
        return path;
    }

    /**
     * A helper method to call all three addAdjacentROw
     * @param currentNode
     */
    private void addAdjacentNodes(Node currentNode) {
        addAdjacentUpperRow(currentNode);
        addAdjacentMiddleRow(currentNode);
        addAdjacentLowerRow(currentNode);
    }

    /**
     * add lower row
     * @param currentNode
     */
    private void addAdjacentLowerRow(Node currentNode) {
        int row = currentNode.getX();
        int col = currentNode.getY();
        int lowerRow = row + 1;
        if (lowerRow < getSearchArea().length) {
            if (col - 1 >= 0) {
                //checkNode(currentNode, col - 1, lowerRow, getDiagonalCost()); // Comment this line if diagonal movements are not allowed
            }
            if (col + 1 < getSearchArea()[0].length) {
                //checkNode(currentNode, col + 1, lowerRow, getDiagonalCost()); // Comment this line if diagonal movements are not allowed
            }
            checkNode(currentNode, col, lowerRow);
        }
    }

    /** add middle row
 	 *
     * @param currentNode
     */
    private void addAdjacentMiddleRow(Node currentNode) {
        int row = currentNode.getX();
        int col = currentNode.getY();
        int middleRow = row;
        if (col - 1 >= 0) {
            checkNode(currentNode, col - 1, middleRow);
        }
        if (col + 1 < getSearchArea()[0].length) {
            checkNode(currentNode, col + 1, middleRow);
        }
    }

    /**
     * add upper row
     * @param currentNode
     */
    private void addAdjacentUpperRow(Node currentNode) {
        int row = currentNode.getX();
        int col = currentNode.getY();
        int upperRow = row - 1;
        if (upperRow >= 0) {
            if (col - 1 >= 0) {
                //checkNode(currentNode, col - 1, upperRow, getDiagonalCost()); // Comment this if diagonal movements are not allowed
            }
            if (col + 1 < getSearchArea()[0].length) {
                //checkNode(currentNode, col + 1, upperRow, getDiagonalCost()); // Comment this if diagonal movements are not allowed
            }
            checkNode(currentNode, col, upperRow);
        }
    }

    /** 
     * check the node and calculate its cost of the path
     * and decide whether there is another cheaper path
     * @param currentNode
     * @param col
     * @param row
     */
    private void checkNode(Node currentNode, int col, int row) {
    	int cost = DEFAULT_ROAD_COST;
    	
		if (TilesChecker.checkForLavaTrap(currentNode.getTile())) {
			cost = DEFAULT_LAVA_COST;
		}
    		
        Node adjacentNode = getSearchArea()[row][col];
        if (!adjacentNode.isTileToAvoid(tilesToAvoid) && !getClosedList().contains(adjacentNode)) {
            if (!getOpenList().contains(adjacentNode)) {
                adjacentNode.setNodeData(currentNode, cost);
                getOpenList().add(adjacentNode);
            } else {
                boolean changed = adjacentNode.checkBetterPath(currentNode, cost);
                if (changed) {
                    // Remove and Add the changed node, so that the PriorityQueue can sort again its
                    // contents with the modified "finalCost" value of the modified node
                    getOpenList().remove(adjacentNode);
                    getOpenList().add(adjacentNode);
                }
            }
        }
    }

    private boolean isFinalNode(Node currentNode) {
        return currentNode.equals(finalNode);
    }

    private boolean isEmpty(PriorityQueue<Node> openList) {
        return openList.size() == 0;
    }

    public Node getInitialNode() {
        return initialNode;
    }

    public void setInitialNode(Node initialNode) {
        this.initialNode = initialNode;
    }

    public Node getFinalNode() {
        return finalNode;
    }

    public void setFinalNode(Node finalNode) {
        this.finalNode = finalNode;
    }

    public Node[][] getSearchArea() {
        return searchArea;
    }

    public void setSearchArea(Node[][] searchArea) {
        this.searchArea = searchArea;
    }

    public PriorityQueue<Node> getOpenList() {
        return openList;
    }

    public void setOpenList(PriorityQueue<Node> openList) {
        this.openList = openList;
    }

    public List<Node> getClosedList() {
        return closedList;
    }

    public void setClosedList(List<Node> closedList) {
        this.closedList = closedList;
    }

    public int getHvCost() {
        return hvCost;
    }

    public void setHvCost(int hvCost) {
        this.hvCost = hvCost;
    }

}