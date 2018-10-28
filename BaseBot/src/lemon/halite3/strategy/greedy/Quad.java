package lemon.halite3.strategy.greedy;

import java.util.Iterator;
import java.util.NoSuchElementException;

import lemon.halite3.util.GameMap;
import lemon.halite3.util.Vector;

public class Quad implements Iterable<Vector> {
	private GameMap gameMap;
	private Vector location;
	private Vector size;
	public Quad(GameMap gameMap, Vector location, Vector size) {
		this.gameMap = gameMap;
		this.location = location;
		this.size = size;
	}
	public Vector getCenter() {
		return location.add(size.getX() / 2, size.getY() / 2, gameMap);
	}
	public Vector getLocation() {
		return location;
	}
	public Vector getSize() {
		return size;
	}
	@Override
	public Iterator<Vector> iterator() {
		return new QuadIterator(location, size, gameMap.getWidth(), gameMap.getHeight());
	}
	@Override
	public String toString() {
		return String.format("Quad[location=%s, size=%s]", location, size);
	}
	class QuadIterator implements Iterator<Vector> {
		private Vector location;
		private Vector size;
		private int modX;
		private int modY;
		private int count;
		public QuadIterator(Vector location, Vector size, int modX, int modY) {
			this.location = location;
			this.size = size;
			this.modX = modX;
			this.modY = modY;
			this.count = 0;
		}
		@Override
		public boolean hasNext() {
			return count < size.getX() * size.getY();
		}
		@Override
		public Vector next() {
			if (count >= size.getX() * size.getY()) {
				throw new NoSuchElementException();
			}
			count++;
			return location.add(count % size.getX(), count / size.getX(), modX, modY);
		}
	}
}
