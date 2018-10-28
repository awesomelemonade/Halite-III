package lemon.halite3.util;

import java.util.StringTokenizer;

public class Vector implements Comparable<Vector> {
	private int x;
	private int y;
	private static Vector[][] array;
	public static void init(GameMap gameMap) {
		array = new Vector[gameMap.getWidth()][gameMap.getHeight()];
	}
	public static Vector getInstance(int x, int y) {
		if (array[x][y] == null) {
			array[x][y] = new Vector(x, y);
		}
		return array[x][y];
	}
	public static Vector getInstance(StringTokenizer tokenizer) {
		return Vector.getInstance(Integer.parseInt(tokenizer.nextToken()), Integer.parseInt(tokenizer.nextToken()));
	}
	protected Vector(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public Vector add(Direction direction, GameMap gameMap) {
		return this.add(direction, gameMap.getWidth(), gameMap.getHeight());
	}
	public Vector add(Direction direction, int modX, int modY) {
		return this.add(direction.getOffsetVector(), modX, modY);
	}
	public Vector add(Vector offset) {
		return Vector.getInstance(x + offset.getX(), y + offset.getY());
	}
	public Vector add(Vector offset, GameMap gameMap) {
		return add(offset, gameMap.getWidth(), gameMap.getHeight());
	}
	public Vector add(Vector offset, int modX, int modY) {
		return add(offset.getX(), offset.getY(), modX, modY);
	}
	public Vector add(int x, int y, GameMap gameMap) {
		return add(x, y, gameMap.getWidth(), gameMap.getHeight());
	}
	public Vector add(int x, int y, int modX, int modY) {
		return Vector.getInstance(((this.x + x) % modX + modX) % modX, ((this.y + y) % modY + modY) % modY);
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public int getManhattanDistance(Vector vector, GameMap gameMap) {
		return getManhattanDistance(vector, gameMap.getWidth(), gameMap.getHeight());
	}
	public int getManhattanDistance(Vector vector, int modX, int modY) {
		return getWrapDistance(x, vector.getX(), modX) + getWrapDistance(y, vector.getY(), modY);
	}
	public int getWrapDistance(int a, int b, int mod) {
		if (a > b) {
			return getWrapDistance(b, a, mod);
		}
		return Math.min(a + mod - b, b - a);
	}
	@Override
	public int hashCode() {
		return GameConstants.MAX_MAP_HEIGHT * x + y;
	}
	@Override
	public boolean equals(Object object) {
		if (object instanceof Vector) {
			Vector vector = (Vector) object;
			return x == vector.getX() && y == vector.getY();
		}
		return false;
	}
	@Override
	public int compareTo(Vector vector) {
		return Integer.compareUnsigned(this.hashCode(), vector.hashCode());
	}
	@Override
	public String toString() {
		return String.format("Vector[%d, %d]", x, y);
	}
}
