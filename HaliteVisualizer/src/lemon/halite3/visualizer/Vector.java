package lemon.halite3.visualizer;

import java.util.StringTokenizer;

public class Vector implements Comparable<Vector> {
	private int x;
	private int y;
	public Vector(StringTokenizer tokenizer) {
		this(Integer.parseInt(tokenizer.nextToken()), Integer.parseInt(tokenizer.nextToken()));
	}
	public Vector(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	@Override
	public int hashCode() {
		return 999 * x + y;
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
