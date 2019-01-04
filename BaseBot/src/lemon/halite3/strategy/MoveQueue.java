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
	private Map<Integer, Direction[]> map;
	private Map<Integer, Direction> resolved;
	private Set<Integer> createDropoffs;
	private boolean[][] unsafe;
	private Set<Vector> ignore;
	public MoveQueue(GameMap gameMap) {
		this.gameMap = gameMap;
		this.map = new HashMap<Integer, Direction[]>();
		this.resolved = new HashMap<Integer, Direction>();
		this.createDropoffs = new HashSet<Integer>();
		this.unsafe = new boolean[gameMap.getWidth()][gameMap.getHeight()];
		this.ignore = new HashSet<Vector>();
	}
	public void createDropoff(int shipId) {
		createDropoffs.add(shipId);
	}
	public void move(int shipId, Direction... directions) {
		map.put(shipId, directions);
	}
	public void move(Ship ship, Direction... directions) {
		this.move(ship.getShipId(), directions);
	}
	public void markIgnore(Vector vector) {
		ignore.add(vector);
	}
	public void markUnsafe(Vector vector) {
		if (ignore.contains(vector)) {
			return;
		}
		unsafe[vector.getX()][vector.getY()] = true;
	}
	public boolean isUnsafe(Vector vector) {
		return unsafe[vector.getX()][vector.getY()];
	}
	public boolean isSafe(Vector vector) {
		return !isUnsafe(vector);
	}
	public void resolveCollisions() {
		// TODO use resolved HashMap
		for (int shipId : map.keySet()) {
			Ship ship = gameMap.getMyPlayer().getShips().get(shipId);
			// Marks square as unsafe
			markUnsafe(ship.getLocation().add(map.get(shipId)[0], gameMap));
		}
	}
	public void resolveCollisions(List<Integer> shipPriorities) {
		resolved.clear();
		// Handle dropoff creations
		for (int shipId : createDropoffs) {
			Networking.transformShipIntoDropoffSite(shipId);
			resolved.put(shipId, null);
		}
		// Handle ships forced to stand still
		for (int shipId : shipPriorities) {
			Ship ship = gameMap.getMyPlayer().getShips().get(shipId);
			if (ship.getHalite() < gameMap.getHalite(ship.getLocation()) / GameConstants.MOVE_COST_RATIO) {
				markUnsafe(ship.getLocation());
				resolved.put(shipId, Direction.STILL);
			}
		}
		// TODO - Handle ship spawning + Creation of dropoffs
		// TODO - Handle collisions between ships
		for (int shipId : shipPriorities) {
			if (resolved.containsKey(shipId)) {
				continue;
			}
			if(!map.containsKey(shipId)) {
				this.move(shipId, Direction.STILL);
			}
			Ship ship = gameMap.getMyPlayer().getShips().get(shipId);
			findValidDirection: {
				for (Direction direction : map.get(shipId)) {
					Vector current = ship.getLocation().add(direction, gameMap);
					if (!isUnsafe(current)) {
						markUnsafe(current);
						resolved.put(shipId, direction);
						break findValidDirection;
					}
				}
				Vector current = ship.getLocation();
				if (!isUnsafe(current)) {
					markUnsafe(current);
					resolved.put(shipId, Direction.STILL);
					break findValidDirection;
				}
				for (Direction direction : Direction.getRandomCardinalPermutation()) {
					current = ship.getLocation().add(direction, gameMap);
					if (!isUnsafe(current)) {
						markUnsafe(current);
						resolved.put(shipId, direction);
						break findValidDirection;
					}
				}
				resolved.put(shipId, Direction.STILL); // RIP CRASHED
				//throw new IllegalStateException("No Direction Found");
			}
		}
	}
	public void send() {
		for (Entry<Integer, Direction> entry : resolved.entrySet()) {
			if (entry.getValue() != null) {
				Networking.move(entry.getKey(), entry.getValue());
			}
		}
	}
}
