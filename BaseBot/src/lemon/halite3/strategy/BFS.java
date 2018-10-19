package lemon.halite3.strategy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lemon.halite3.util.Direction;
import lemon.halite3.util.GameConstants;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Vector;

public class BFS {
	private GameMap gameMap;
	public BFS(GameMap gameMap) {
		this.gameMap = gameMap;
	}
	public int calculate(Vector location, int currentHalite, int targetHalite, Map<Vector, Integer> mineMap, Set<Vector> visited, int maxTurns) {
		if (currentHalite >= targetHalite) { // Base Case
			return 0;
		}
		if (visited.contains(location) || maxTurns <= 0) {
			return Integer.MAX_VALUE;
		}
		int bestTurns = maxTurns;
		int haliteLeft = gameMap.getHalite(location) - mineMap.getOrDefault(location, 0);
		int moveCost = (int) Math.floor(haliteLeft * 1.0 / GameConstants.MOVE_COST_RATIO);
		if (currentHalite >= moveCost) {
			Set<Vector> newVisited = addVisited(visited, location);
			for (Direction direction: Direction.CARDINAL_DIRECTIONS) {
				Vector newLocation = location.add(direction, gameMap);
				bestTurns = Math.min(bestTurns, calculate(newLocation, currentHalite - moveCost, targetHalite, mineMap, newVisited, bestTurns) + 1);
			}
		}
		int mined = (int) Math.ceil(haliteLeft * 1.0 / GameConstants.EXTRACT_RATIO);
		mined = Math.min(mined, GameConstants.MAX_HALITE - currentHalite);
		bestTurns = Math.min(bestTurns, calculate(location, currentHalite + mined, targetHalite, incrementMineMap(mineMap, location, mined), visited, bestTurns) + 1);
		return bestTurns;
	}
	public Set<Vector> addVisited(Set<Vector> visited, Vector vector) {
		Set<Vector> newVisited = new HashSet<Vector>(visited);
		newVisited.add(vector);
		return newVisited;
	}
	public Map<Vector, Integer> incrementMineMap(Map<Vector, Integer> map, Vector vector, int value) {
		Map<Vector, Integer> newMap = new HashMap<Vector, Integer>(map);
		newMap.put(vector, newMap.getOrDefault(vector, 0) + value);
		return newMap;
	}
}
