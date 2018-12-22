package lemon.halite3.strategy;

import java.util.ArrayDeque;
import java.util.Deque;

import lemon.halite3.strategy.generator.DP;
import lemon.halite3.util.Direction;
import lemon.halite3.util.Dropoff;
import lemon.halite3.util.GameConstants;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Vector;

public class Navigation {
	private static GameMap gameMap;
	public static void init(GameMap gameMap) {
		Navigation.gameMap = gameMap;
		endgameQueue = new ArrayDeque<Vector>();
		endgameDP = new int[gameMap.getWidth()][gameMap.getHeight()];
		endgameDirections = new Direction[gameMap.getWidth()][gameMap.getHeight()];
	}
	public static Direction[] navigate(Vector start, Vector end) {
		if (start.equals(end)) {
			return new Direction[] {Direction.STILL};
		}
		Direction a = getDirection(start.getX(), end.getX(), gameMap.getWidth(), Direction.WEST, Direction.EAST);
		Direction b = getDirection(start.getY(), end.getY(), gameMap.getHeight(), Direction.NORTH, Direction.SOUTH);
		if (start.getX() == end.getX()) {
			return new Direction[] {b};
		}
		if (start.getY() == end.getY()) {
			return new Direction[] {a};
		}
		return DP.getCost(start.add(a, gameMap), end) < DP.getCost(start.add(b, gameMap), end) ? new Direction[] {a, b} : new Direction[] {b, a};
	}
	public static Direction getDirection(int start, int end, int mod, Direction neg, Direction pos) {
		return end > start ? (end - start < mod - end + start ? pos : neg) : (start - end < mod - start + end ? neg : pos);
	}
	private static Deque<Vector> endgameQueue;
	private static int[][] endgameDP;
	private static Direction[][] endgameDirections;
	private static int[][] endgameDistance;
	private static final int UNCALCULATED = Integer.MAX_VALUE;
	private static final int QUEUED = Integer.MAX_VALUE - 1;
	private static void setEndgameDP(Vector location, int value) {
		endgameDP[location.getX()][location.getY()] = value;
	}
	// Init endgame
	public static void initEndgame() {
		// BFS dropoffs & shipyard as source
		for (int i = 0; i < endgameDP.length; ++i) {
			for (int j = 0; j < endgameDP[0].length; ++j) {
				endgameDP[i][j] = UNCALCULATED;
			}
		}
		endgameQueue.clear();
		endgameQueue.add(gameMap.getMyPlayer().getShipyardLocation());
		setEndgameDP(gameMap.getMyPlayer().getShipyardLocation(), 0);
		for (Dropoff dropoff : gameMap.getMyPlayer().getDropoffs().values()) {
			endgameQueue.add(dropoff.getLocation());
			setEndgameDP(dropoff.getLocation(), 0);
		}
		while (!endgameQueue.isEmpty()) {
			Vector vector = endgameQueue.poll();
			int min = Integer.MAX_VALUE;
			Direction minDirection = null;
			for (Direction direction : Direction.CARDINAL_DIRECTIONS) {
				Vector v = vector.add(direction, gameMap);
				int x = endgameDP[v.getX()][v.getY()];
				if (x == UNCALCULATED) {
					endgameQueue.add(v);
					endgameDP[v.getX()][v.getY()] = QUEUED;
				} else if (x != QUEUED) {
					min = Math.min(min, x);
					minDirection = direction;
				}
			}
			endgameDP[vector.getX()][vector.getY()] = min + gameMap.getHalite(vector) / GameConstants.MOVE_COST_RATIO;
			endgameDirections[vector.getX()][vector.getY()] = minDirection.invert();
		}
	}
	// Shipyard Crashing Strategy
	public static Direction[] navigateEndgame(Vector location) {
		
	}
}
