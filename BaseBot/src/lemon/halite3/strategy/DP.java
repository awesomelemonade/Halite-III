package lemon.halite3.strategy;

import java.util.HashMap;
import java.util.Map;

import lemon.halite3.util.DebugLog;
import lemon.halite3.util.Direction;
import lemon.halite3.util.GameConstants;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Vector;

public class DP {
	private Map<Map<Vector, Integer>, Map<Integer, Map<Integer, Map<Integer, Map<Integer, Integer>>>>> dp;
	private GameMap gameMap;
	private Vector targetLocation;
	public DP(GameMap gameMap, Vector targetLocation) {
		this.dp = new HashMap<Map<Vector, Integer>, Map<Integer, Map<Integer, Map<Integer, Map<Integer, Integer>>>>>();
		this.gameMap = gameMap;
		this.targetLocation = targetLocation;
	}
	private int maxRecursions = 9999;
	
	private void putDP(Map<Vector, Integer> mineMap, int halite, int turns, int x, int y, int value) {
		if (!dp.containsKey(mineMap)) {
			dp.put(mineMap, new HashMap<Integer, Map<Integer, Map<Integer, Map<Integer, Integer>>>>());
		}
		if (!dp.get(mineMap).containsKey(halite)) {
			dp.get(mineMap).put(halite, new HashMap<Integer, Map<Integer, Map<Integer, Integer>>>());
		}
		if (!dp.get(mineMap).get(halite).containsKey(turns)) {
			dp.get(mineMap).get(halite).put(turns, new HashMap<Integer, Map<Integer, Integer>>());
		}
		if (!dp.get(mineMap).get(halite).get(turns).containsKey(x)) {
			dp.get(mineMap).get(halite).get(turns).put(x, new HashMap<Integer, Integer>());
		}
		dp.get(mineMap).get(halite).get(turns).get(x).put(y, value);
	}
	private int getDP(Map<Vector, Integer> mineMap, int halite, int turns, int x, int y) {
		if (dp.containsKey(mineMap)) {
			if (dp.get(mineMap).containsKey(halite)) {
				if (dp.get(mineMap).get(halite).containsKey(turns)) {
					if (dp.get(mineMap).get(halite).get(turns).containsKey(x)) {
						if (dp.get(mineMap).get(halite).get(turns).get(x).containsKey(y)) {
							return dp.get(mineMap).get(halite).get(turns).get(x).get(y);
						}
					}
				}
			}
		}
		return -1;
	}
	
	public int calculate(int halite, int turns, Vector location, Map<Vector, Integer> mineMap) {
		maxRecursions--;
		if(maxRecursions <= 0) {
			DebugLog.log("Exiting - Reached maxRecursions");
			return -999999;
		}
		// DebugLog.log("dp: " + halite + " - " + turns + " - " + location + " - " + mineMap + " | " + ((System.nanoTime() - BasicStrategy.startTime) / 1000000000.0));
		// Check DP
		int candidateFromDP = getDP(mineMap, halite, turns, location.getX(), location.getY());
		if (candidateFromDP != -1) {
			// DebugLog.log("Got le DP: " + candidateFromDP);
			return candidateFromDP;
		}
		// Should I check dumping?
		
		// Base Case
		if (turns == 0) {
			if (location.equals(targetLocation)) {
				putDP(mineMap, halite, turns, location.getX(), location.getY(), halite);
				return halite;
			} else {
				return -9999;
			}
		}
		int bestHalite = -9999;
		// Option 1-4 - Move in cardinal directions
		// TODO: pruning with Manhatten distance
		// TODO: Inspired Move Cost?
		// Cannot use pow because they round each time in between
		int haliteLeft = getHaliteLeft(gameMap.getHalite(location), mineMap.getOrDefault(location, 0));
		int moveCost = (int) Math.floor(haliteLeft * 1.0 / GameConstants.MOVE_COST_RATIO);
		
		if (halite >= moveCost) { // Check if there is enough halite to move
			for (Direction direction: Direction.CARDINAL_DIRECTIONS) {
				Vector newLocation = location.add(direction, gameMap.getWidth(), gameMap.getHeight());
				if (newLocation.getManhattanDistance(targetLocation, gameMap.getWidth(), gameMap.getHeight()) <= turns) {
					bestHalite = Math.max(bestHalite, calculate(halite - moveCost, turns - 1, newLocation, mineMap));
				}
			}
		}
		// Option 5 - Stand still and mine
		if (location.getManhattanDistance(targetLocation, gameMap.getWidth(), gameMap.getHeight()) <= turns) {
			int mined = (int) Math.ceil(haliteLeft * 1.0 / GameConstants.EXTRACT_RATIO);
			bestHalite = Math.max(bestHalite, calculate(halite + mined, turns - 1, location, increment(mineMap, location, 1)));
		}
		putDP(mineMap, halite, turns, location.getX(), location.getY(), bestHalite);
		return bestHalite;
	}
	public int getHaliteLeft(int initial, int mines) {
		for (int i = 0; i < mines; ++i) {
			initial -= Math.ceil(initial * 1.0 / GameConstants.EXTRACT_RATIO); // TODO: Why doesn't this error?
		}
		return initial;
	}
	public Map<Vector, Integer> increment(Map<Vector, Integer> map, Vector vector, int value){
		Map<Vector, Integer> newMap = new HashMap<Vector, Integer>(map);
		newMap.put(vector, newMap.getOrDefault(vector, 0) + value);
		return newMap;
	}
}
