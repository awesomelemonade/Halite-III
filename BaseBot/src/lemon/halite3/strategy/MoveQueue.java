package lemon.halite3.strategy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lemon.halite3.util.Direction;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Networking;
import lemon.halite3.util.Ship;
import lemon.halite3.util.Vector;

public class MoveQueue {
	private GameMap gameMap;
	private Map<Integer, Direction> map;
	private Set<Vector> unsafe;
	public MoveQueue(GameMap gameMap) {
		this.gameMap = gameMap;
		this.map = new HashMap<Integer, Direction>();
		this.unsafe = new HashSet<Vector>();
	}
	public void move(Ship ship, Direction direction) {
		map.put(ship.getShipId(), direction);
	}
	public void resolveCollisions() {
		for (int shipId : map.keySet()) {
			// Marks square as unsafe
			unsafe.add(gameMap.getMyPlayer().getShips().get(shipId).getLocation().add(map.get(shipId), gameMap));
		}
	}
	public void resolveCollisions(List<Integer> shipPriorities) {
		// TODO - Handle ship spawning + Creation of dropoffs
		// TODO - Handle collisions between ships
		for (int shipId : shipPriorities) {
			// Marks square as unsafe
			unsafe.add(gameMap.getMyPlayer().getShips().get(shipId).getLocation().add(map.get(shipId), gameMap));
		}
	}
	public boolean isSafe(Vector vector) {
		return !unsafe.contains(vector);
	}
	public void send() {
		for (Entry<Integer, Direction> entry : map.entrySet()) {
			Networking.move(entry.getKey(), entry.getValue());
		}
	}
}
