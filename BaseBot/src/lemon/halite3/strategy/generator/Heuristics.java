package lemon.halite3.strategy.generator;

import java.util.ArrayList;
import java.util.Collections;
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
	public static int execute(Vector start, int halite, Set<Vector> vectors, Vector end) {
		Vector current = start;
		int currentHalite = halite;
		while (!vectors.isEmpty()) {
			int bestDistance = Integer.MAX_VALUE;
			Vector bestVector = null;
			for (Vector vector : vectors) {
				
			}
			Vector best = vectors.stream().min((a, b) -> Integer.compare(current.getManhattanDistance(a, gameMap),
					current.getManhattanDistance(bestVector, gameMap))).get();
		}
		
		return 999999;
	}
	// Needs to return <AmountOfHalite, AmountOfTurnsItTook>
	// Theoretically we COULD put traversed vectors in the priority queue to make it even more optimal
	public static int execute(Vector start, int halite, Vector end) {
		if (dp[start.getX()][start.getY()][end.getX()][end.getY()] <= halite) {
			
		} else {
			int[][] dp = Heuristics.dp[start.getX()][start.getY()];
			List<Vector> path = getPath(start, end);
			PriorityQueue<Vector> queue = new PriorityQueue<Vector>(new Comparator<Vector>() {
				@Override
				public int compare(Vector a, Vector b) {
					return 0;
				}
			});
			Vector current = start;
			int index = 0;
			Map<Vector, Integer> mineMap = new HashMap<Vector, Integer>();
			Map<Vector, Integer> mineValues = new HashMap<Vector, Integer>();
			for (Vector vector : path) {
				if (dp[vector.getX()][vector.getY()] <= halite) {
					queue.add(vector);
				} else {
					while (dp[vector.getX()][vector.getY()] > halite) {
						Vector v = queue.poll();
						halite += mineValues.get(v);
						// Update Mine Value
						// Update Mine Map
						queue.add(v);
					}
					// Eventually...
					// Add Mine Value
					queue.add(vector);
				}
			}
		}
	}
	public static int heuristic(List<Vector> path, int halite) {
		PriorityQueue<Vector> queue = new PriorityQueue<Vector>(new Comparator<Vector>() {
			@Override
			public int compare(Vector a, Vector b) {
				return 0;
			}
		});
		for (Vector vector : path) {
			if (/* we cannot pass normally w/o mining*/) {
				// mine some halite
			}
			queue.add(vector); // Make vector available for mining
			halite -= /* */;// Subtract move cost from halite
		}
		// Pop out desired amount
		while (halite < GameConstants.MAX_HALITE - 50) {
			Vector toMine = queue.poll();
		}
		// Return # of turns and where to mine/what to do next
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
