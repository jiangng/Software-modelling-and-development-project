package mycontroller;

import java.util.ArrayList;
import java.util.Arrays;

import tiles.LavaTrap;
import tiles.MapTile;
import tiles.TrapTile;
import tiles.MapTile.Type;

public class TilesChecker {
	private static ArrayList<String> nameOfTilesWithKeys = new ArrayList<>(Arrays.asList("lava"));
	private static ArrayList<Type> drivableTileTypes = new ArrayList<>(
			Arrays.asList(Type.TRAP, Type.ROAD, Type.START, Type.FINISH));
	private static ArrayList<String> drivableTraps = new ArrayList<>(
			Arrays.asList("health", "lava"));
	private static ArrayList<String> nameOfHealingTiles = new ArrayList<>(Arrays.asList("health"));
	
	/**
	 * ASSUME : TO HAVE A KEY YOU NEED TO BE A TRAPTILE
	 * check if it has a key
	 * @param tile
	 * @return
	 */
	public static boolean checkTileWithKeys(MapTile tile) {
		if (checkForTrapTile(tile)) {
			return checkTrapTileWithKeys(tile);
		}
		return false;
	}
	
	/**
	 * check for lava trap
	 * @param tile
	 * @return
	 */
	public static boolean checkForLavaTrap(MapTile tile) {
		if (checkForTrapTile(tile)) {
			return ((TrapTile) tile).getTrap().equals("lava");
		}
		return false;
	}
	
	/**
	 * check if it is a traptile
	 * @param tile
	 * @return
	 */
	public static boolean checkForTrapTile(MapTile tile) {
		return tile.getType() == MapTile.Type.TRAP;
	}

	public static boolean checkTrapTileWithKeys(MapTile tile) {
		return nameOfTilesWithKeys.contains(((TrapTile) tile).getTrap());
	}

	public static int getKeyFromTile(MapTile tile) {
		String trap = ((TrapTile) tile).getTrap();
		switch (trap) {
			case "lava":
				return ((LavaTrap) tile).getKey();

			default:
				return 0;
		}
	}

	/**
	 * check tile which is empty (OUTOFBOUNDS TILE)
	 * @param tile
	 * @return
	 */
	public static boolean checkTileIsEmpty(MapTile tile) {
		return tile.getType() == MapTile.Type.EMPTY;
	}

	/**
	 * return true if both tiles are the same type
	 * @param tile1
	 * @param tile2
	 * @return
	 */
	public static boolean checkTileSameType(MapTile tile1, MapTile tile2) {
		if (tile1.getType() == MapTile.Type.TRAP && tile2.getType() == MapTile.Type.TRAP) {
			if (((TrapTile) tile1).getTrap().equals(((TrapTile) tile2).getTrap())) {
				return true;
			}
		} else if (tile1.getType().equals(tile2.getType())) {
			return true;
		}
		return false;
	}

	/**
	 * Check if the car can drive on the given tile
	 * First check if the tile is of the same type as tileToAvoid (if yes, means the car cannot drive on it so return false)
	 * If tile is not same as tileToAvoid, then check if the tile's type is one of the default drivable tiles.
	 * @param tile
	 * @param tileToAvoid
	 * @return yes or no
	 */
	public static boolean checkTileTraversable(MapTile tile, MapTile tileToAvoid) {
		Type tileType = tile.getType();
		Type tileToAvoidType = tileToAvoid.getType();

		//Check if tile and tileToAvoid are of same type/trap
		if (tileType.equals(tileToAvoidType)) {
			if (tileType.equals(Type.TRAP)) {
				String tileTrap = ((TrapTile) tile).getTrap();
				String tileToAvoidTrap = ((TrapTile) tileToAvoid).getTrap();
				//If tileTrap and tileToAvoidTrap not equal, continue with the next if-block
				if (tileTrap.equals(tileToAvoidTrap)) {
					return false;
				} 
			} else {
				return false;
			}
		}

		//Check if tile is one of the drivable types
		if (drivableTileTypes.contains(tileType)) {
			if (tileType.equals(Type.TRAP)) {
				return drivableTraps.contains(((TrapTile) tile).getTrap());
			}
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * check if the tile is not one of the tilesToAvoid and is traversable
	 * @param tile
	 * @param tilesToAvoid
	 * @return true
	 */
	public static boolean checkTileTraversable(MapTile tile, ArrayList<MapTile> tilesToAvoid) {
		Type tileType = tile.getType();
		for (MapTile tileToAvoid : tilesToAvoid) {
			Type tileToAvoidType = tileToAvoid.getType();
			//Check if tile and tileToAvoid are of same type/trap
			if (tileType.equals(tileToAvoidType)) {
				if (tileType.equals(Type.TRAP)) {
					String tileTrap = ((TrapTile) tile).getTrap();
					String tileToAvoidTrap = ((TrapTile) tileToAvoid).getTrap();
					//If tileTrap and tileToAvoidTrap not equal, continue with the next if-block
					if (tileTrap.equals(tileToAvoidTrap)) {
						return false;
					} 
				} else {
					return false;
				}
			}
		}
		
		//Check if tile is one of the drivable types
		if (drivableTileTypes.contains(tileType)) {
			if (tileType.equals(Type.TRAP)) {
				return drivableTraps.contains(((TrapTile) tile).getTrap());
			}
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean checkForHealthTile(MapTile tile) {
		if (checkForTrapTile(tile)) {
			return nameOfHealingTiles.contains(((TrapTile) tile).getTrap());
		}
		
		return false;
	}
	
	public static boolean checkForTileToAvoid(MapTile tile1, MapTile tile2) {
		return checkTileSameType(tile1, tile2);
	}
}
