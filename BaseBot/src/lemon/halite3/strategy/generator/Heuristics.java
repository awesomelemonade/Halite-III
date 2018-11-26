package lemon.halite3.strategy.generator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

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
	public static HeuristicsPlan execute(Vector start, int halite, int haliteNeeded, Set<Vector> vectors, Vector end, Map<Vector, Integer> mineMap) {
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
			addPath(current, bestVector, totalPath);
			vectors.remove(bestVector);
			current = bestVector;
		}
		totalPath.add(current);
		HeuristicsPlan plan = new HeuristicsPlan(totalPath);
		heuristic(totalPath, halite, haliteNeeded, plan, mineMap);
		plan.addTotalTurns(current.getManhattanDistance(end, gameMap));
		return plan;
	}
	public static HeuristicsPlan heuristic(List<Vector> path, int halite, int haliteNeeded, HeuristicsPlan plan, Map<Vector, Integer> mineMap) {
		Map<Vector, Integer> totalCounts = getCounts(path);
		Map<Vector, Integer> mineValues = new HashMap<Vector, Integer>();
		Map<Vector, Integer> tempMineMap = new HashMap<Vector, Integer>();
		Map<Vector, Integer> counts = new HashMap<Vector, Integer>();
		for (Vector vector : path) {
			mineValues.put(vector, getMineValue(vector, mineMap, tempMineMap, totalCounts));
		}
		PriorityQueue<Vector> queue = new PriorityQueue<Vector>(new Comparator<Vector>() {
			@Override
			public int compare(Vector a, Vector b) {
				return Integer.compare(mineValues.get(b), mineValues.get(a)); // Descending
			}
		});
		for (Vector vector : path) {
			if (!queue.contains(vector)) { // TODO: Make it not O(n)
				queue.add(vector); // Make vector available for mining
			}
			int costOfMovingOutOfThisSquare = (gameMap.getHalite(vector) - mineMap.getOrDefault(vector, 0) - tempMineMap.getOrDefault(vector, 0)) / GameConstants.MOVE_COST_RATIO;
			if (costOfMovingOutOfThisSquare > halite) { // we cannot pass normally w/o mining
				// mine some halite
				Vector mineLocation = queue.poll();
				plan.incrementMineCount(mineLocation);
				int haliteLeft = gameMap.getHalite(mineLocation) - mineMap.getOrDefault(mineLocation, 0) - tempMineMap.getOrDefault(mineLocation, 0);
				int mined = getMined(haliteLeft);
				halite += mined + counts.getOrDefault(mineLocation, 0) * 
						(haliteLeft / GameConstants.MOVE_COST_RATIO - (haliteLeft - mined) / GameConstants.MOVE_COST_RATIO);
				tempMineMap.put(mineLocation, tempMineMap.getOrDefault(mineLocation, 0) + mined);
				// Put mineLocation back to queue
				mineValues.put(mineLocation, getMineValue(mineLocation, mineMap, tempMineMap, totalCounts));
				queue.add(mineLocation);
			}
			counts.put(vector, counts.getOrDefault(vector, 0) + 1);
			halite -= (gameMap.getHalite(vector) - tempMineMap.getOrDefault(vector, 0)) / GameConstants.MOVE_COST_RATIO; // Subtract move cost from halite
		}
		// Pop out desired amount
		while (halite < haliteNeeded) {
			Vector mineLocation = queue.poll();
			plan.incrementMineCount(mineLocation);
			int haliteLeft = gameMap.getHalite(mineLocation) - mineMap.getOrDefault(mineLocation, 0) - tempMineMap.getOrDefault(mineLocation, 0);
			if (haliteLeft <= 0) {
				plan.addTotalTurns(9999);
				return plan;
			}
			int mined = getMined(haliteLeft);
			halite += mined + counts.get(mineLocation) * 
					(haliteLeft / GameConstants.MOVE_COST_RATIO - (haliteLeft - mined) / GameConstants.MOVE_COST_RATIO);
			tempMineMap.put(mineLocation, tempMineMap.getOrDefault(mineLocation, 0) + mined);
			// Put mineLocation back to queue
			mineValues.put(mineLocation, getMineValue(mineLocation, mineMap, tempMineMap, totalCounts));
			queue.add(mineLocation);
		}
		plan.setMineMap(tempMineMap);
		return plan;
	}
	public static int getMined(int halite) {
		return (halite + GameConstants.EXTRACT_RATIO - 1) / GameConstants.EXTRACT_RATIO; // Rounds up without Math.ceil()
	}
	public static int getMineValue(Vector vector, Map<Vector, Integer> mineMap, Map<Vector, Integer> tempMineMap, Map<Vector, Integer> totalCounts) {
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
	public static void addPath(Vector start, Vector end, List<Vector> path) {
		int[][] dp = Heuristics.dp[end.getX()][end.getY()];
		Direction a = getDirection(start.getX(), end.getX(), gameMap.getWidth(), Direction.WEST, Direction.EAST);
		Direction b = getDirection(start.getY(), end.getY(), gameMap.getHeight(), Direction.NORTH, Direction.SOUTH);
		Vector current = start;
		while (!current.equals(end)) {
			path.add(current);
			Vector minVector = null;
			int minValue = Integer.MAX_VALUE;
			if (current.getX() != end.getX()) {
				Vector v = current.add(a, gameMap);
				if (dp[v.getX()][v.getY()] < minValue) {
					minValue = dp[v.getX()][v.getY()];
					minVector = v;
				}
			}
			if (current.getY() != end.getY()) {
				Vector v = current.add(b, gameMap);
				if (dp[v.getX()][v.getY()] < minValue) {
					minValue = dp[v.getX()][v.getY()];
					minVector = v;
				}
			}
			current = minVector;
		}
	}
	public static Direction getDirection(int start, int end, int mod, Direction neg, Direction pos) {
		return end > start ? (end - start < mod - end + start ? pos : neg) : (start - end < mod - start + end ? neg : pos);
	}
	static class HeuristicsPlan {
		private List<Vector> totalPath;
		private Map<Vector, Integer> mineCounts;
		private Map<Vector, Integer> mineMap;
		private int totalTurns;
		public HeuristicsPlan(List<Vector> totalPath) {
			this.totalPath = totalPath;
			this.mineCounts = new HashMap<Vector, Integer>();
			this.mineMap = new HashMap<Vector, Integer>();
			this.totalTurns = totalPath.size();
		}
		public List<Vector> getTotalPath(){
			return totalPath;
		}
		public void incrementMineCount(Vector vector) {
			mineCounts.put(vector, mineCounts.getOrDefault(vector, 0) + 1);
			totalTurns++;
		}
		public void setMineMap(Map<Vector, Integer> mineMap) {
			this.mineMap = mineMap;
		}
		public Map<Vector, Integer> getMineMap() {
			return mineMap;
		}
		public Map<Vector, Integer> getMineCounts() {
			return mineCounts;
		}
		public void addTotalTurns(int turns) {
			totalTurns += turns;
		}
		public int getTotalTurns() {
			return totalTurns;
		}
	}
}
