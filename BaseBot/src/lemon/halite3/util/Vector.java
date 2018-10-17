package lemon.halite3.util;

import java.util.StringTokenizer;

public class Vector implements Comparable<Vector> {
	private int x;
	private int y;
	public Vector(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public Vector(StringTokenizer tokenizer) {
		this(Integer.parseInt(tokenizer.nextToken()), Integer.parseInt(tokenizer.nextToken()));
	}
	public Vector add(Direction direction, int modX, int modY) {
		return this.add(direction.getOffsetVector(), modX, modY);
	}
	public Vector add(Vector offset, int modX, int modY) {
		return new Vector(((x + offset.getX()) % modX + modX) % modX, ((y + offset.getY()) % modY + modY) % modY);
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
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
}
