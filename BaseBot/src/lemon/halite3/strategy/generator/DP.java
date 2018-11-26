package lemon.halite3.strategy.generator;

import java.util.ArrayDeque;
import java.util.Deque;

import lemon.halite3.util.Direction;
import lemon.halite3.util.GameConstants;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Vector;

public class DP {
	private static final int UNCALCULATED = Integer.MAX_VALUE;
	private static final int QUEUED = Integer.MAX_VALUE - 1;
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
		dp[source.getX()][source.getY()] = QUEUED;
		costs[source.getX()][source.getY()] = 0;
		dp[source.getX()][source.getY()] = gameMap.getHalite(source) / GameConstants.MOVE_COST_RATIO;
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
