package lemon.halite3.visualizer;

import java.util.StringTokenizer;

public class Quad {
	private Vector location;
	private Vector size;
	public Quad(StringTokenizer tokenizer) {
		this(new Vector(tokenizer), new Vector(tokenizer));
	}
	public Quad(Vector location, Vector size) {
		this.location = location;
		this.size = size;
	}
	public Vector getLocation() {
		return location;
	}
	public Vector getSize() {
		return size;
	}
}
