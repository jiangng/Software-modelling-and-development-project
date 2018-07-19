package mycontroller.AStarSearch;

import mycontroller.TilesChecker;
import tiles.MapTile;

import java.util.ArrayList;

/**
 * Node for A* Algorithm
 *
 * @version 2.0, 2017-02-23
 * @author Marcelo Surriabre
 */
public class Node {

    private int g;
    private int f;
    private int h;
    private int xPos;
    private int yPos;
    private MapTile tile;
    private Node parent;

    public Node(int x, int y, MapTile tile) {
        this.tile = tile;
        this.xPos = x;
        this.yPos = y;
    }

    /**
     * calculate the heuristic cost
     * @param finalNode
     */
    public void calculateHeuristic(Node finalNode) {
        this.h = Math.abs(finalNode.getX() - getX()) + Math.abs(finalNode.getY() - getY());
    }

    public void setNodeData(Node currentNode, int cost) {
        int gCost = currentNode.getG() + cost;
        setParent(currentNode);
        setG(gCost);
        calculateFinalCost();
    }

    public boolean checkBetterPath(Node currentNode, int cost) {
        int gCost = currentNode.getG() + cost;
        if (gCost < getG()) {
            setNodeData(currentNode, cost);
            return true;
        }
        return false;
    }

    private void calculateFinalCost() {
        int finalCost = getG() + getH();
        setF(finalCost);
    }

    @Override
    public boolean equals(Object arg0) {
        Node other = (Node) arg0;
        return this.getX() == other.getX() && this.getY() == other.getY();
    }

    @Override
    public String toString() {
        return "Node [row=" + xPos + ", col=" + yPos + "]";
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public int getF() {
        return f;
    }

    public void setF(int f) {
        this.f = f;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public boolean isTileToAvoid(ArrayList<MapTile> tilesToAvoid) {
        for (MapTile tileToAvoid : tilesToAvoid) {
            if (TilesChecker.checkForTileToAvoid(tileToAvoid, this.tile)) {
                return true;
            }
        }
        return false;
    }

    public int getX() {
        return xPos;
    }

    public void setX(int xPos) {
        this.xPos = xPos;
    }

    public int getY() {
        return yPos;
    }

    public void setCol(int yPos) {
        this.yPos = yPos;
    }
    
    public MapTile getTile() {
    	return this.tile;
    }
}