package lemon.halite3.strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lemon.halite3.util.Direction;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Networking;
import lemon.halite3.util.Ship;

public class MoveQueue {
	private GameMap gameMap;
	private Map<Ship, Direction> map;
	public MoveQueue(GameMap gameMap) {
		this.gameMap = gameMap;
		this.map = new HashMap<Ship, Direction>();
	}
	public void move(Ship ship, Direction direction) {
		map.put(ship, direction);
	}
	public void resolveCollisions() {
		// TODO - Handle ship spawning + Creation of dropoffs
		// TODO - Handle collisions between ships
	}
	public void send() {
		for (Entry<Ship, Direction> entry : map.entrySet()) {
			Networking.move(entry.getKey(), entry.getValue());
		}
	}
}
