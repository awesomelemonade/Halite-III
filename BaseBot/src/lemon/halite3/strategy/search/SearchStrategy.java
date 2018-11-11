package lemon.halite3.strategy.search;

import lemon.halite3.strategy.Strategy;
import lemon.halite3.util.Benchmark;
import lemon.halite3.util.DebugLog;
import lemon.halite3.util.GameConstants;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Networking;
import lemon.halite3.util.Ship;
import lemon.halite3.util.Vector;

public class SearchStrategy implements Strategy {
	private GameMap gameMap;
	@Override
	public String init(GameMap gameMap) {
		this.gameMap = gameMap;
		Vector.init(gameMap);
		return "SearchStrategy";
	}
	@Override
	public void run(GameMap gameMap) {
		while (true) {
			gameMap.update();
			try (Benchmark benchmark = new Benchmark("Benchmark: %ss")) {
				DebugLog.log("New Turn: " + gameMap.getCurrentTurn() + " - numShips=" + gameMap.getMyPlayer().getShips().size() + 
						" ***********************************************");
				DebugLog.log("hello?");
				Temp temp = new Temp(gameMap);
				for (Ship ship : gameMap.getMyPlayer().getShips().values()) {
					temp.search(ship.getLocation(), ship.getHalite(), gameMap.getMyPlayer().getShipyardLocation(), GameConstants.MAX_HALITE - 50);
				}
				DebugLog.log("hi");
				
				// Try to spawn a ship
				if (gameMap.getMyPlayer().getShips().size() == 0 && 
						gameMap.getMyPlayer().getHalite() >= GameConstants.SHIP_COST && // TODO - consider cost of building dropoffs in the same turn
						gameMap.getCurrentTurn() + 150 < GameConstants.MAX_TURNS) {
					Networking.spawnShip();
				}
			}
			Networking.endTurn();
		}
	}
}
