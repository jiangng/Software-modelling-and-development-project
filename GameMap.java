package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import tiles.MapTile;
import utilities.Coordinate;

public class GameMap {
	private HashMap<Coordinate, HashMapTile> updatedMap = new HashMap<>();
	private int totalNumberOfKeys;
	private int numOfKeysFound = 0;
	private ArrayList<Integer> findingKeyOrder;
	private Coordinate nearestHealthTile = null;

	public GameMap(HashMap<Coordinate, MapTile> map, int totalNumberOfKeys) {
		createMap(map);
		this.totalNumberOfKeys = totalNumberOfKeys;
		findingKeyOrder = descendingKeyOrder(totalNumberOfKeys);
	}

	/**
	 * create an empty map to be stored based on getMap()
	 * @param map
	 */
	private void createMap(HashMap<Coordinate, MapTile> map) {
		for (Coordinate key : map.keySet()) {
			System.out.println(map.get(key).getType());
			getUpdatedMap().put(key, new HashMapTile(map.get(key)));
		}
	}

	/**
	 * update map with getView()
	 * set the key value if it is a lavatrap with key
	 * check for health trap
	 * @param currentView
	 */
    public void updateMap(HashMap<Coordinate, MapTile> currentView) {

	    //iterate currentView
		for (Coordinate key : currentView.keySet()) {
            //get the corresponding tile from view based on coordinate
            MapTile tileFromView = currentView.get(key);

			if (TilesChecker.checkTileIsEmpty(currentView.get(key))){
				break;
			}
			HashMapTile tileFromMap = getUpdatedMap().get(key);

			//get from map the object to check if it has been explored
			if (tileFromMap.getExplored() == 0) {
			
				//check if its a lava trap by using TilesWithKeysChecker class
				if (TilesChecker.checkTileWithKeys(tileFromView)) {

					//Assume without key, key value = 0
					//if it contains a key, set key value 
					if (TilesChecker.getKeyFromTile(tileFromView) != 0) {
						tileFromMap.setKeyValue(TilesChecker.getKeyFromTile(tileFromView));
						numOfKeysFound++;
					}
				}

				//set explored and change tile type
				tileFromMap.setExplored(1);
				tileFromMap.setTile(tileFromView);
				updatedMap.put(key, tileFromMap);
			}

            //TODO: check logic here
            if (TilesChecker.checkForHealthTile(tileFromView)) {
                nearestHealthTile = key;
            }
		}

		if(numOfKeysFound == totalNumberOfKeys){
			System.out.println("Start finding key strategy");
		}
	}

    /**
     * get the next key coordinate using the updatedMap
     * @return
     */
    public Coordinate getNextKeyCoordinate() {
	     int nextKey = findingKeyOrder.remove(0);
	     for (Map.Entry<Coordinate, HashMapTile> entry : updatedMap.entrySet()) {
	         if (entry.getValue().getKeyValue() == nextKey) {
	             return entry.getKey();
             }
         }
         return null;
    }

    /**
     * return a list on the order to look for keys
     * @param maxKey
     * @return
     */
	public ArrayList<Integer> descendingKeyOrder(int maxKey){
		ArrayList<Integer> newList = new ArrayList<>();
		for(int i = maxKey; i > 0; i--){
			newList.add(i);
		}
		return newList;
	}

	public HashMap<Coordinate, HashMapTile> getUpdatedMap() {
		return updatedMap;
	}

	public void setUpdatedMap(HashMap<Coordinate, HashMapTile> updatedMap) {
		this.updatedMap = updatedMap;
	}
	
    public Coordinate getNearestHealthTile() {
        return nearestHealthTile;
    }

}
