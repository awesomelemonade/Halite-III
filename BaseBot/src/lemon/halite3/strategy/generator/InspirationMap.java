package lemon.halite3.strategy.generator;

import lemon.halite3.util.Direction;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.GamePlayer;
import lemon.halite3.util.Ship;
import lemon.halite3.util.Vector;

public class InspirationMap {
	private GameMap gameMap;
	private int[][][] inspiration;
	private int size;
	private int distance;
	private Vector[][] offsets;
	public InspirationMap(GameMap gameMap, int size, int distance) {
		this.gameMap = gameMap;
		this.size = size;
		this.distance = distance;
		this.inspiration = new int[gameMap.getWidth()][gameMap.getHeight()][size];
		this.offsets = getOffsets(Math.max(1, distance));
	}
	public static Vector[][] getOffsets(int distance) {
		Vector[][] offsets = new Vector[distance][]; // Jagged Array
		offsets[0] = new Vector[] {Vector.getOffsetVector(0, 0)};
		offsets[1] = new Vector[] {Direction.NORTH.getOffsetVector(), Direction.EAST.getOffsetVector(),
				Direction.SOUTH.getOffsetVector(), Direction.WEST.getOffsetVector()};
		for (int i = 2; i < distance; ++i) {
			Vector[] vectors = new Vector[4 * i];
			for (int j = 0; j < 4; ++j) {
				vectors[i * j] = multiplyOffsetVector(offsets[1][j], i);
			}
			for (int j = 0; j < offsets[i - 1].length; ++j) {
				vectors[j + j / (offsets[i - 1].length / 4) + 1] = addOffsetVector(offsets[i - 1][j], offsets[1][(j / (offsets[i - 1].length / 4) + 1) % 4]);
			}
			offsets[i] = vectors;
		}
		return offsets;
	}
	private static Vector addOffsetVector(Vector a, Vector b) {
		return Vector.getOffsetVector(a.getX() + b.getX(), a.getY() + b.getY());
	}
	private static Vector multiplyOffsetVector(Vector vector, int num) {
		return Vector.getOffsetVector(vector.getX() * num, vector.getY() * num);
	}
	public void calculateInspiration() {
		for (GamePlayer player : gameMap.getPlayers()) {
			if (player.getPlayerId() == gameMap.getMyPlayerId()) {
				continue; // Cannot be inspired by your own ships
			}
			for (Ship ship : player.getShips().values()) {
				for (int i = 0; i < distance; ++i) {
					for (int j = 0; j < offsets[i].length; ++j) {
						addInspiration(ship.getLocation().add(offsets[i][j]), distance - i + 1);
					}
				}
			}
		}
	}
	public void addInspiration(Vector vector, int value) {
		int min = Integer.MAX_VALUE;
		int minIndex = -1;
		for (int i = 0; i < size; ++i) {
			if (inspiration[vector.getX()][vector.getY()][i] < min) {
				min = inspiration[vector.getX()][vector.getY()][i];
				minIndex = i;
			}
		}
		if (value > min) {
			inspiration[vector.getX()][vector.getY()][minIndex] = value;
		}
	}
	public int getInspiration(int x, int y) {
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < size; ++i) {
			if (inspiration[x][y][i] < min) {
				min = inspiration[x][y][i];
			}
		}
		return min; 
	}
}
