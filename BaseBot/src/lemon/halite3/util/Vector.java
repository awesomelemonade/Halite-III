package lemon.halite3.util;

import java.util.StringTokenizer;

public class Vector {
	private int x;
	private int y;
	public Vector(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public Vector(StringTokenizer tokenizer) {
		this(Integer.parseInt(tokenizer.nextToken()), Integer.parseInt(tokenizer.nextToken()));
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
}
