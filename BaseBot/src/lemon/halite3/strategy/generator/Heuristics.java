package lemon.halite3.strategy.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import lemon.halite3.util.DebugLog;
import lemon.halite3.util.Direction;
import lemon.halite3.util.GameConstants;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Vector;

public class Heuristics {
	private static GameMap gameMap;
	private static int[][][][] dp;
	public static void init(GameMap gameMap, int[][][][] dp) {
		Heuristics.gameMap = gameMap;
		Heuristics.dp = dp;
	}
	public static int execute(Vector start, int halite, Set<Vector> vectors, Vector end) {
		List<Vector> totalPath = new ArrayList<Vector>();
		Vector current = start;
		while (!vectors.isEmpty()) {
			int bestDistance = Integer.MAX_VALUE;
			Vector bestVector = null;
			for (Vector vector : vectors) {
				int distance = current.getManhattanDistance(vector, gameMap);
				if (distance < bestDistance) {
					bestDistance = distance;
					bestVector = vector;
				}
			}
			totalPath.addAll(getPath(current, bestVector));
			vectors.remove(bestVector);
			current = bestVector;
		}
		totalPath.addAll(getPath(current, end));
		DebugLog.log("TotalPath: " + totalPath.size());
		return heuristic(totalPath, halite)	;
	}
	public static int heuristic(List<Vector> path, int halite) {
		Map<Vector, Integer> totalCounts = getCounts(path);
		Map<Vector, Integer> mineValues = new HashMap<Vector, Integer>();
		Map<Vector, Integer> mineMap = new HashMap<Vector, Integer>();
		Map<Vector, Integer> counts = new HashMap<Vector, Integer>();
		for (Vector vector : path) {
			mineValues.put(vector, getMineValue(vector, mineMap, totalCounts));
		}
		PriorityQueue<Vector> queue = new PriorityQueue<Vector>(new Comparator<Vector>() {
			@Override
			public int compare(Vector a, Vector b) {
				return Integer.compare(mineValues.get(b), mineValues.get(a)); // Descending
			}
		});
		for (Vector vector : path) {
			int costOfMovingOutOfThisSquare = gameMap.getHalite(vector) / GameConstants.MOVE_COST_RATIO;
			if (costOfMovingOutOfThisSquare > halite) { // we cannot pass normally w/o mining
				// mine some halite
				Vector mineLocation = queue.poll();
				int haliteLeft = gameMap.getHalite(mineLocation) - mineMap.getOrDefault(mineLocation, 0);
				int mined = getMined(haliteLeft);
				halite += mined + counts.get(mineLocation) * 
						(haliteLeft / GameConstants.MOVE_COST_RATIO - (haliteLeft - mined) / GameConstants.MOVE_COST_RATIO);
				mineMap.put(mineLocation, mineMap.getOrDefault(mineLocation, 0) + mined);
				// Put mineLocation back to queue
				mineValues.put(mineLocation, getMineValue(mineLocation, mineMap, totalCounts));
				queue.add(mineLocation);
			}
			counts.put(vector, counts.getOrDefault(vector, 0) + 1);
			queue.add(vector); // Make vector available for mining
			halite -= (gameMap.getHalite(vector) - mineMap.getOrDefault(vector, 0)) / GameConstants.MOVE_COST_RATIO; // Subtract move cost from halite
		}
		// Pop out desired amount
		while (halite < GameConstants.MAX_HALITE - 50) {
			Vector mineLocation = queue.poll();
			int haliteLeft = gameMap.getHalite(mineLocation) - mineMap.getOrDefault(mineLocation, 0);
			int mined = getMined(haliteLeft);
			halite += mined + counts.get(mineLocation) * 
					(haliteLeft / GameConstants.MOVE_COST_RATIO - (haliteLeft - mined) / GameConstants.MOVE_COST_RATIO);
			mineMap.put(mineLocation, mineMap.getOrDefault(mineLocation, 0) + mined);
			// Put mineLocation back to queue
			mineValues.put(mineLocation, getMineValue(mineLocation, mineMap, totalCounts));
			queue.add(mineLocation);
		}
		// Return # of turns and where to mine/what to do next
		return path.size();
	}
	public static int getMined(int halite) {
		return (halite + GameConstants.EXTRACT_RATIO - 1) / GameConstants.EXTRACT_RATIO; // Rounds up without Math.ceil()
	}
	public static int getMineValue(Vector vector, Map<Vector, Integer> mineMap, Map<Vector, Integer> totalCounts) {
		int haliteLeft = gameMap.getHalite(vector) - mineMap.getOrDefault(vector, 0);
		int mine = getMined(haliteLeft);
		return mine + totalCounts.get(vector) * (haliteLeft / GameConstants.MOVE_COST_RATIO - (haliteLeft - mine) / GameConstants.MOVE_COST_RATIO);
	}
	public static Map<Vector, Integer> getCounts(List<Vector> path) {
		Map<Vector, Integer> map = new HashMap<Vector, Integer>();
		for (Vector vector : path) {
			map.put(vector, map.getOrDefault(vector, 0) + 1);
		}
		return map;
	}
	public static List<Vector> getPath(Vector start, Vector end) {
		int[][] dp = Heuristics.dp[start.getX()][start.getY()];
		List<Vector> path = new ArrayList<Vector>();
		Vector current = end;
		while (!current.equals(start)) {
			path.add(current);
			Vector minVector = null;
			int minValue = Integer.MAX_VALUE;
			for (Direction direction : Direction.CARDINAL_DIRECTIONS) {
				Vector v = current.add(direction, gameMap);
				if (dp[v.getX()][v.getY()] < minValue) {
					minValue = dp[v.getX()][v.getY()];
					minVector = v;
				}
			}
			current = minVector;
		}
		path.add(current);
		Collections.reverse(path);
		return path;
	}
}
