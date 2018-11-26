package lemon.halite3.strategy.generator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import lemon.halite3.util.Direction;
import lemon.halite3.util.GameConstants;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Vector;

public class DP {
	private static final int UNCALCULATED = Integer.MAX_VALUE;
	private static final int QUEUED = Integer.MAX_VALUE - 1;
	private static GameMap gameMap;
	private static int[][][][] dp;
	private static int[][][][] costs;
	private static Map<Vector, Deque<Vector>> queues;
	public static void init(GameMap gameMap) {
		dp = new int[gameMap.getWidth()][gameMap.getHeight()][gameMap.getWidth()][gameMap.getHeight()];
		costs = new int[gameMap.getWidth()][gameMap.getHeight()][gameMap.getWidth()][gameMap.getHeight()];
		queues = new HashMap<Vector, Deque<Vector>>();
	}
	public static void execute(Vector source, Vector stop) {
		if (queues.containsKey(source)) {
			reset(source);
		}
		int[][] dp = DP.dp[source.getX()][source.getY()];
		int[][] costs = DP.costs[source.getX()][source.getY()];
		Deque<Vector> queue = queues.get(source);
		while (!queue.isEmpty() && (dp[stop.getX()][stop.getY()] == UNCALCULATED || dp[stop.getX()][stop.getY()] == QUEUED)) {
			Vector vector = queue.poll();
			int min = Integer.MAX_VALUE;
			for (Direction direction : Direction.CARDINAL_DIRECTIONS) {
				Vector v = vector.add(direction, gameMap);
				int x = dp[v.getX()][v.getY()];
				if (x == UNCALCULATED) {
					queue.add(v);
					dp[v.getX()][v.getY()] = QUEUED;
				} else if (x != QUEUED) {
					min = Math.min(min, x);
				}
			}
			costs[vector.getX()][vector.getY()] = min;
			dp[vector.getX()][vector.getY()] = min + gameMap.getHalite(vector) / GameConstants.MOVE_COST_RATIO;
		}
	}
	public static void reset() {
		for (Vector vector : queues.keySet()) {
			reset(vector);
		}
	}
	public static void reset(Vector vector) {
		int[][] dp = DP.dp[vector.getX()][vector.getY()];
		if (queues.containsKey(vector)) {
			queues.get(vector).clear();
		} else {
			queues.put(vector, new ArrayDeque<Vector>());
		}
		Deque<Vector> queue = queues.get(vector);
		for (int i = 0; i < dp.length; ++i) {
			for (int j = 0; j < dp[0].length; ++j) {
				dp[i][j] = UNCALCULATED;
			}
		}
		dp[vector.getX()][vector.getY()] = gameMap.getHalite(vector) / GameConstants.MOVE_COST_RATIO;
		costs[vector.getX()][vector.getY()][vector.getX()][vector.getY()] = 0;
		for (Direction direction : Direction.CARDINAL_DIRECTIONS) {
			Vector v = vector.add(direction, gameMap);
			dp[v.getX()][v.getY()] = QUEUED;
			queue.add(v);
		}
	}
	public static int[][][][] execute(GameMap gameMap){
		int[][][][] costs = new int[gameMap.getWidth()][gameMap.getHeight()][gameMap.getWidth()][gameMap.getHeight()];
		for (int i = 0; i < gameMap.getWidth(); ++i) {
			for (int j = 0; j < gameMap.getHeight(); ++j) {
				costs[i][j] = execute(gameMap, Vector.getInstance(i, j));
			}
		}
		return costs;
	}
	public static int[][] execute(GameMap gameMap, Vector source) {
		int[][] dp = new int[gameMap.getWidth()][gameMap.getHeight()];
		int[][] costs = new int[gameMap.getWidth()][gameMap.getHeight()];
		for (int i = 0; i < dp.length; ++i) {
			for (int j = 0; j < dp[0].length; ++j) {
				dp[i][j] = UNCALCULATED;
			}
		}
		Deque<Vector> queue = new ArrayDeque<Vector>();
		dp[source.getX()][source.getY()] = gameMap.getHalite(source) / GameConstants.MOVE_COST_RATIO;
		costs[source.getX()][source.getY()] = 0;
		for (Direction direction : Direction.CARDINAL_DIRECTIONS) {
			Vector v = source.add(direction, gameMap);
			dp[v.getX()][v.getY()] = QUEUED;
			queue.add(v);
		}
		while (!queue.isEmpty()) {
			Vector vector = queue.poll();
			int min = Integer.MAX_VALUE;
			for (Direction direction : Direction.CARDINAL_DIRECTIONS) {
				Vector v = vector.add(direction, gameMap);
				int x = dp[v.getX()][v.getY()];
				if (x == UNCALCULATED) {
					queue.add(v);
					dp[v.getX()][v.getY()] = QUEUED;
				} else if (x != QUEUED) {
					min = Math.min(min, x);
				}
			}
			costs[vector.getX()][vector.getY()] = min;
			dp[vector.getX()][vector.getY()] = min + gameMap.getHalite(vector) / GameConstants.MOVE_COST_RATIO;
		}
		return costs;
	}
}
