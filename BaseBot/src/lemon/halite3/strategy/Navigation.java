package lemon.halite3.strategy;

import lemon.halite3.util.Direction;
import lemon.halite3.util.GameConstants;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Vector;

public class Navigation {
	private GameMap gameMap;
	public Navigation(GameMap gameMap) {
		this.gameMap = gameMap;
	}
	public int getCost(Vector start, Vector end) {
		if (start.equals(end)) {
			return 0;
		}
		Direction a = getDirection(start.getX(), end.getX(), gameMap.getWidth(), Direction.WEST, Direction.EAST);
		Direction b = getDirection(start.getY(), end.getY(), gameMap.getHeight(), Direction.NORTH, Direction.SOUTH);
		int[][] dp = new int[gameMap.getWidth()][gameMap.getHeight()];
		for (int i = 0; i < dp.length; ++i) {
			for (int j = 0; j < dp[0].length; ++j) {
				dp[i][j] = Integer.MAX_VALUE;
			}
		}
		dp[end.getX()][end.getY()] = 0;
		return dp(start, end, dp, a, b);
	}
	public Direction navigate(Vector start, Vector end) {
		if (start.equals(end)) {
			return Direction.STILL;
		}
		Direction a = getDirection(start.getX(), end.getX(), gameMap.getWidth(), Direction.WEST, Direction.EAST);
		Direction b = getDirection(start.getY(), end.getY(), gameMap.getHeight(), Direction.NORTH, Direction.SOUTH);
		int[][] dp = new int[gameMap.getWidth()][gameMap.getHeight()];
		for (int i = 0; i < dp.length; ++i) {
			for (int j = 0; j < dp[0].length; ++j) {
				dp[i][j] = Integer.MAX_VALUE;
			}
		}
		dp[end.getX()][end.getY()] = 0;
		dp(start, end, dp, a, b);
		Vector aVector = start.add(a, gameMap);
		Vector bVector = start.add(b, gameMap);
		return dp[aVector.getX()][aVector.getY()] <
				dp[bVector.getX()][bVector.getY()] ? a : b;
	}
	public int dp(Vector vector, Vector end, int[][] dp, Direction a, Direction b) {
		if (dp[vector.getX()][vector.getY()] != Integer.MAX_VALUE) {
			return dp[vector.getX()][vector.getY()];
		}
		Vector aVector = vector.add(a, gameMap);
		Vector bVector = vector.add(b, gameMap);
		int aDP = vector.getX() == end.getX() ? Integer.MAX_VALUE : dp(aVector, end, dp, a, b);
		int bDP = vector.getY() == end.getY() ? Integer.MAX_VALUE : dp(bVector, end, dp, a, b);
		int result = Math.min(aDP, bDP) + gameMap.getHalite(vector) / GameConstants.MOVE_COST_RATIO;
		dp[vector.getX()][vector.getY()] = result;
		return result;
	}
	public Direction getDirection(int start, int end, int mod, Direction neg, Direction pos) {
		return end > start ? (end - start < mod - end + start ? pos : neg) : (start - end < mod - start + end ? neg : pos);
	}
}
