package lemon.halite3.strategy;

import lemon.halite3.strategy.generator.DP;
import lemon.halite3.util.Direction;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Vector;

public class Navigation {
	private static GameMap gameMap;
	public static void init(GameMap gameMap) {
		Navigation.gameMap = gameMap;
	}
	public static Direction[] navigate(Vector start, Vector end) {
		if (start.equals(end)) {
			return new Direction[] {Direction.STILL};
		}
		Direction a = getDirection(start.getX(), end.getX(), gameMap.getWidth(), Direction.WEST, Direction.EAST);
		Direction b = getDirection(start.getY(), end.getY(), gameMap.getHeight(), Direction.NORTH, Direction.SOUTH);
		int aDP = start.getX() == end.getX() ? Integer.MAX_VALUE : DP.getCost(start.add(a, gameMap), end);
		int bDP = start.getY() == end.getY() ? Integer.MAX_VALUE : DP.getCost(start.add(b, gameMap), end);
		return aDP < bDP ? new Direction[] {a, b} : new Direction[] {b, a};
	}
	public static Direction getDirection(int start, int end, int mod, Direction neg, Direction pos) {
		return end > start ? (end - start < mod - end + start ? pos : neg) : (start - end < mod - start + end ? neg : pos);
	}
}
