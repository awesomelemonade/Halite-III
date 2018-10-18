package lemon.halite3.strategy;

import java.util.HashMap;

import lemon.halite3.util.DebugLog;
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
				DebugLog.log(String.format("DP for Ship %d - %s - w/ %d halite",
						ship.getShipId(), ship.getLocation().toString(), ship.getHalite()));
				DebugLog.log("Calculated: " + dp.calculate(ship.getHalite(), 5, ship.getLocation(),
						new HashMap<Vector, Integer>()));
			}
			Networking.endTurn();
		}
	}
}
