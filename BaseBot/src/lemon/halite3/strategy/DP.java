package lemon.halite3.strategy;

import java.util.HashMap;
import java.util.Map;

import lemon.halite3.util.Direction;
import lemon.halite3.util.GameConstants;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Vector;

public class DP {
	private Map<Map<Vector, Integer>, int[][][][]> dp;
	private GameMap gameMap;
	private Vector targetLocation;
	public DP(GameMap gameMap, Vector targetLocation) {
		this.dp = new HashMap<Map<Vector, Integer>, int[][][][]>();
		this.targetLocation = targetLocation;
	}
	public int calculate(int halite, int turns, Vector location, Map<Vector, Integer> mineMap) {
		// Check DP
		if (dp.containsKey(mineMap)) {
			int candidate = dp.get(mineMap)[halite][turns][location.getX()][location.getY()];
			if (candidate != 0) {
				return candidate;
			}
		}
		// Should I check dumping?
		
		// Base Case
		if (turns == 0 && location.equals(targetLocation)) {
			if (!dp.containsKey(mineMap)) {
				dp.put(mineMap, new int[GameConstants.MAX_HALITE][GameConstants.MAX_TURNS]
						[gameMap.getWidth()][gameMap.getHeight()]);
			}
			if (halite == 0) {
				halite = -1;
			}
			dp.get(mineMap)[halite][turns][location.getX()][location.getY()] = halite;
			return halite;
		}
		int bestHalite = -1;
		// Option 1-4 - Move in cardinal directions
		// TODO: pruning with Manhatten distance
		// TODO: Inspired Move Cost?
		int moveCost = (int) Math.floor(gameMap.getHalite(location) *
				Math.pow(1.0 - 1.0 / GameConstants.EXTRACT_RATIO, 
						mineMap.getOrDefault(location, 0)) * 1.0 / GameConstants.MOVE_COST_RATIO);
		if (halite >= moveCost) { // Check if there is enough halite to move
			for (Direction direction: Direction.CARDINAL_DIRECTIONS) {
				Vector newLocation = location.add(direction, gameMap.getWidth(), gameMap.getHeight());
				bestHalite = Math.max(bestHalite, calculate(halite - moveCost, turns - 1, newLocation, mineMap));
			}
		}
		// Option 5 - Stand still and mine
		return bestHalite;
	}
	public Map<Vector, Integer> increment(Map<Vector, Integer> map, Vector vector, int value){
		Map<Vector, Integer> newMap = new HashMap<Vector, Integer>(map);
		newMap.put(vector, newMap.getOrDefault(vector, 0) + value);
		return newMap;
	}
}
