package lemon.halite3.strategy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lemon.halite3.util.Direction;
import lemon.halite3.util.GameConstants;
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
		Set<Integer> handled = new HashSet<Integer>();
		// Handle ships forced to stand still
		for (int shipId : shipPriorities) {
			Ship ship = gameMap.getMyPlayer().getShips().get(shipId);
			if (ship.getHalite() < gameMap.getHalite(ship.getLocation()) / GameConstants.MOVE_COST_RATIO) {
				unsafe.add(ship.getLocation());
				map.put(shipId, Direction.STILL);
				handled.add(shipId);
			}
		}
		// TODO - Handle ship spawning + Creation of dropoffs
		// TODO - Handle collisions between ships
		for (int shipId : shipPriorities) {
			if (handled.contains(shipId)) {
				continue;
			}
			if(!map.containsKey(shipId)) {
				map.put(shipId, Direction.STILL);
			}
			// Check if current square is unsafe
			Vector current = gameMap.getMyPlayer().getShips().get(shipId).getLocation().add(map.get(shipId), gameMap);
			if (unsafe.contains(current)) {
				if (gameMap.getHalite(gameMap.getMyPlayer().getShips().get(shipId).getLocation()) > 100) {
					map.put(shipId, Direction.STILL); // Still could actually still result in a collision.. TODO
					current = gameMap.getMyPlayer().getShips().get(shipId).getLocation().add(map.get(shipId), gameMap);
				}
				if (unsafe.contains(current)) {
					// Randomly Selected - TODO: Make direction priorities
					for (Direction direction : Direction.getRandomCardinalPermutation()) {
						map.put(shipId, direction);
						current = gameMap.getMyPlayer().getShips().get(shipId).getLocation().add(map.get(shipId), gameMap);
						if (!unsafe.contains(current)) {
							break;
						}
					}
				}
			}
			// Marks square as unsafe
			unsafe.add(gameMap.getMyPlayer().getShips().get(shipId).getLocation().add(map.get(shipId), gameMap));
			handled.add(shipId);
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
