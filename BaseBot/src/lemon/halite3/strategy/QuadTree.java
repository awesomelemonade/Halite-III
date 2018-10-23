package lemon.halite3.strategy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import lemon.halite3.util.GameMap;
import lemon.halite3.util.Vector;

public class QuadTree {
	private GameMap gameMap;
	private Quad quad;
	public QuadTree(GameMap gameMap) {
		this.gameMap = gameMap;
		this.quad = getQuad(0, 0, gameMap.getWidth(), gameMap.getHeight());
	}
	public List<Quad> calculate(int threshold) {
		List<Quad> quads = new ArrayList<Quad>();
		Deque<Quad> toProcess = new ArrayDeque<Quad>();
		toProcess.add(quad);
		while (!toProcess.isEmpty()) {
			Quad quad = toProcess.poll();
			if (quad.getQuads() == null || quad.getHalite() <= threshold) {
				quads.add(quad);
			} else {
				for (Quad q : quad.getQuads()) {
					if (q != null) {
						quads.add(q);
					}
				}
			}
		}
		return quads;
	}
	public Quad getQuad(int x, int y, int width, int height) {
		if ((width < 1 && height <= 1) || (width <= 1 && height < 1)) {
			return new Quad(x, y, width, height, 0);
		}
		if (width == 1 && height == 1) {
			return new Quad(x, y, width, height, gameMap.getHalite(x, y));
		}
		Quad topLeft = getQuad(x, y, width / 2, height / 2);
		Quad topRight = getQuad(x + width / 2, y, (width + 1) / 2, height / 2);
		Quad bottomLeft = getQuad(x, y + height / 2, width, (height + 1) / 2);
		Quad bottomRight = getQuad(x + width / 2, y + height / 2, (width + 1) / 2, (height + 1) / 2);
		return new Quad(topLeft, topRight, bottomLeft, bottomRight);
	}
	class Quad {
		private Vector location;
		private Vector size;
		private int halite;
		private Quad[] quads;
		public Quad(int x, int y, int width, int height, int halite) {
			this(new Vector(x, y), new Vector(width, height), halite);
		}
		public Quad(Vector location, Vector size, int halite, Quad[] quads) {
			this.location = location;
			this.size = size;
			this.halite = halite;
			this.quads = quads;
		}
		public Quad(Vector location, Vector size, int halite) {
			this(location, size, halite, null);
		}
		public Quad(Quad topLeft, Quad topRight, Quad bottomLeft, Quad bottomRight) {
			this(topLeft.getLocation(), topLeft.getSize().add(bottomRight.getSize()),
					topLeft.getHalite() + topRight.getHalite() + bottomLeft.getHalite() + bottomRight.getHalite(),
					new Quad[] {topLeft, topRight, bottomLeft, bottomRight});
		}
		public Vector getLocation() {
			return location;
		}
		public Vector getSize() {
			return size;
		}
		public int getHalite() {
			return halite;
		}
		public Quad[] getQuads() {
			return quads;
		}
		public Quad getTopLeftQuad() {
			return quads[0];
		}
		public Quad getTopRightQuad() {
			return quads[1];
		}
		public Quad getBottomLeftQuad() {
			return quads[2];
		}
		public Quad getBottomRightQuad() {
			return quads[3];
		}
	}
}
