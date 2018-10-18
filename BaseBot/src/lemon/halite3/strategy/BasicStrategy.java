package lemon.halite3.strategy;

import java.util.HashMap;

import lemon.halite3.util.DebugLog;
import lemon.halite3.util.Direction;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Networking;
import lemon.halite3.util.Ship;
import lemon.halite3.util.Vector;

public class BasicStrategy implements Strategy {
	@Override
	public String init(GameMap gameMap) {
		return "Lemon's BasicStrategy :D";
	}
	@Override
	public void run(GameMap gameMap) {
		gameMap.update();
		Networking.spawnShip();
		Networking.endTurn();
		while (true) {
			gameMap.update();
			DebugLog.log("New Turn: " + gameMap.getCurrentTurn() + " - numShips=" + gameMap.getMyPlayer().getShips().size());
			DP dp = new DP(gameMap, gameMap.getMyPlayer().getShipyardLocation());
			for (Ship ship : gameMap.getMyPlayer().getShips().values()) {
				Networking.move(ship, calculateDirection(dp, ship, 5));
			}
			Networking.endTurn();
		}
	}
	public Direction calculateDirection(DP dp, Ship ship, int turns) {
		DebugLog.log(String.format("DP for Ship %d - %s - %d halite",
				ship.getShipId(), ship.getLocation().toString(), ship.getHalite()));
		int calculated = dp.calculate(ship.getHalite(), turns, ship.getLocation(),
				new HashMap<Vector, Integer>());
		DebugLog.log("\tCalculated: " + calculated);
		Direction direction = dp.trace(ship.getHalite(), turns, ship.getLocation(), new HashMap<Vector, Integer>());
		DebugLog.log("\tDirection: " + direction);
		return direction;
	}
}
