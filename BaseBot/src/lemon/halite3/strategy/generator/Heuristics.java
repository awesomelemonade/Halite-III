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
	public static void init(GameMap gameMap) {
		Heuristics.gameMap = gameMap;
	}
	public static HeuristicsPlan execute(Vector start, Vector end, int halite, int haliteNeeded, Map<Vector, Integer> mineMap, int cutoff) {
		List<Vector> path = getPath(start, end, cutoff);
		if (path == null) {
			return null;
		}
		HeuristicsPlan plan = getPlan(path, halite, haliteNeeded, mineMap, cutoff);
		if (plan != null) {
			Direction a = getDirection(start.getX(), end.getX(), gameMap.getWidth(), Direction.WEST, Direction.EAST);
			Direction b = getDirection(start.getY(), end.getY(), gameMap.getHeight(), Direction.NORTH, Direction.SOUTH);
			Direction planned = path.get(0).getDirectionTo(path.get(1), gameMap);
			plan.setAlternateDirection(a == planned ? (start.getY() != end.getY() ? b : Direction.STILL) : (start.getX() != end.getX() ? a : Direction.STILL));
		}
		return plan;
	}
	public static HeuristicsPlan execute(Vector start, int halite, int haliteNeeded, Set<Vector> vectors, Vector end, Map<Vector, Integer> mineMap, int cutoff) {
		List<Vector> order = getOrder(start, vectors, end, cutoff);
		if (order == null) {
			return null;
		}
		List<Vector> path = getPath(order);
		HeuristicsPlan plan = getPlan(path, halite, haliteNeeded, mineMap, cutoff);
		if (plan != null) {
			Direction a = getDirection(start.getX(), order.get(1).getX(), gameMap.getWidth(), Direction.WEST, Direction.EAST);
			Direction b = getDirection(start.getY(), order.get(1).getY(), gameMap.getHeight(), Direction.NORTH, Direction.SOUTH);
			Direction planned = path.get(0).getDirectionTo(path.get(1), gameMap);
			plan.setAlternateDirection(a == planned ? (start.getY() != order.get(1).getY() ? b : Direction.STILL) : (start.getX() != order.get(1).getX() ? a : Direction.STILL));
		}
		return plan;
	}
	public static List<Vector> getPath(Vector start, Vector end, int cutoff){
		if (start.getManhattanDistance(end, gameMap) >= cutoff) {
			return null;
		}
		List<Vector> path = new ArrayList<Vector>();
		addPath(start, end, path);
		path.add(end);
		return path;
	}
	public static List<Vector> getOrder(Vector start, Set<Vector> vectors, Vector end, int cutoff){
		List<Vector> order = new ArrayList<Vector>();
		int totalDistance = 0;
		Vector current = start;
		order.add(current);
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
			totalDistance += bestDistance;
			if (totalDistance >= cutoff) {
				return null;
			}
			order.add(bestVector);
			vectors.remove(bestVector);
			current = bestVector;
		}
		totalDistance += current.getManhattanDistance(end, gameMap);
		if (totalDistance >= cutoff) {
			return null;
		}
		order.add(end);
		return order;
	}
	public static List<Vector> getPath(List<Vector> order){
		List<Vector> totalPath = new ArrayList<Vector>();
		for (int i = 0; i < order.size() - 1; ++i) {
			addPath(order.get(i), order.get(i + 1), totalPath);
		}
		totalPath.add(order.get(order.size() - 1));
		return totalPath;
	}
	public static HeuristicsPlan getPlan(List<Vector> path, int halite, int haliteNeeded, Map<Vector, Integer> mineMap, int cutoff) {
		HeuristicsPlan plan = new HeuristicsPlan(path);
		if (!heuristic(path, halite, haliteNeeded, plan, mineMap, cutoff)) {
			return null;
		}
		return plan;
	}
	public static boolean heuristic(List<Vector> path, int halite, int haliteNeeded, HeuristicsPlan plan, Map<Vector, Integer> mineMap, int cutoff) {
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
			if (plan.getTotalTurns() >= cutoff) {
				return false;
			}
			int haliteLeft = gameMap.getHalite(mineLocation) - mineMap.getOrDefault(mineLocation, 0) - tempMineMap.getOrDefault(mineLocation, 0);
			if (haliteLeft <= 0) {
				plan.addTotalTurns(9999);
				plan.setMineMap(tempMineMap);
				return plan.getTotalTurns() < cutoff;
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
		return true;
	}
	public static int getMined(int halite) {
		return (halite + GameConstants.EXTRACT_RATIO - 1) / GameConstants.EXTRACT_RATIO; // Rounds up without Math.ceil()
	}
	public static int getMineValue(Vector vector, Map<Vector, Integer> mineMap, Map<Vector, Integer> tempMineMap, Map<Vector, Integer> totalCounts) {
		int haliteLeft = gameMap.getHalite(vector) - mineMap.getOrDefault(vector, 0) - tempMineMap.getOrDefault(vector, 0);
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
		Direction a = getDirection(start.getX(), end.getX(), gameMap.getWidth(), Direction.WEST, Direction.EAST);
		Direction b = getDirection(start.getY(), end.getY(), gameMap.getHeight(), Direction.NORTH, Direction.SOUTH);
		Vector current = start;
		while (!current.equals(end)) {
			path.add(current);
			Vector minVector = null;
			int minValue = Integer.MAX_VALUE;
			if (current.getX() != end.getX()) {
				Vector v = current.add(a, gameMap);
				if (DP.getCost(end, v) < minValue) {
					minValue = DP.getCost(end, v);
					minVector = v;
				}
			}
			if (current.getY() != end.getY()) {
				Vector v = current.add(b, gameMap);
				if (DP.getCost(end, v) < minValue) {
					minValue = DP.getCost(end, v);
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
		private Direction alternateDirection;
		public HeuristicsPlan(List<Vector> totalPath) {
			this.totalPath = totalPath;
			this.mineCounts = new HashMap<Vector, Integer>();
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
		public void setAlternateDirection(Direction direction) {
			this.alternateDirection = direction;
		}
		public Direction getAlternateDirection() {
			return alternateDirection;
		}
	}
}
