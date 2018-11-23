package lemon.halite3.strategy.generator;

import java.util.ArrayDeque;
import java.util.Deque;

import lemon.halite3.util.Direction;
import lemon.halite3.util.GameConstants;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Vector;

public class DP {
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
		boolean[][] queued = new boolean[gameMap.getWidth()][gameMap.getHeight()];
		for (int i = 0; i < dp.length; ++i) {
			for (int j = 0; j < dp[0].length; ++j) {
				dp[i][j] = Integer.MAX_VALUE;
			}
		}
		Deque<Vector> queue = new ArrayDeque<Vector>();
		queued[source.getX()][source.getY()] = true;
		costs[source.getX()][source.getY()] = 0;
		dp[source.getX()][source.getY()] = gameMap.getHalite(source) / GameConstants.MOVE_COST_RATIO;
		for (Direction direction : Direction.CARDINAL_DIRECTIONS) {
			Vector v = source.add(direction, gameMap);
			queued[v.getX()][v.getY()] = true;
			queue.add(v);
		}
		while (!queue.isEmpty()) {
			Vector vector = queue.poll();
			int min = Integer.MAX_VALUE;
			for (Direction direction : Direction.CARDINAL_DIRECTIONS) {
				Vector v = vector.add(direction, gameMap);
				if (dp[v.getX()][v.getY()] == Integer.MAX_VALUE) {
					if (!queued[v.getX()][v.getY()]) {
						queued[v.getX()][v.getY()] = true;
						queue.add(v);
					}
				} else {
					min = Math.min(min, dp[v.getX()][v.getY()]);
				}
			}
			costs[vector.getX()][vector.getY()] = min;
			dp[vector.getX()][vector.getY()] = min + gameMap.getHalite(vector) / GameConstants.MOVE_COST_RATIO;
		}
		return costs;
	}
}
