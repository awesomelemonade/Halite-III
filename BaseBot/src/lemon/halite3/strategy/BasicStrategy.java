package lemon.halite3.strategy;

import java.util.HashMap;
import java.util.Map;

import lemon.halite3.util.DebugLog;
import lemon.halite3.util.Direction;
import lemon.halite3.util.GameConstants;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Networking;
import lemon.halite3.util.Ship;
import lemon.halite3.util.Vector;

public class BasicStrategy implements Strategy {
	public static final Map<Vector, Integer> EMPTY_MINEMAP = new HashMap<Vector, Integer>();
	@Override
	public String init(GameMap gameMap) {
		return "Lemon's BasicStrategy :D";
	}
	@Override
	public void run(GameMap gameMap) {
		while (true) {
			gameMap.update();
			DebugLog.log("New Turn: " + gameMap.getCurrentTurn() + " - numShips=" + gameMap.getMyPlayer().getShips().size() + 
					" ***********************************************");
			if (gameMap.getMyPlayer().getShips().size() == 0 && 
					gameMap.getMyPlayer().getHalite() >= GameConstants.SHIP_COST &&
					isSafeToSpawnShip(gameMap) && 
					gameMap.getCurrentTurn() + 20 < GameConstants.MAX_TURNS) {
				Networking.spawnShip();
			}
			DP dp = new DP(gameMap, gameMap.getMyPlayer().getShipyardLocation());
			for (Ship ship : gameMap.getMyPlayer().getShips().values()) {
				/*for (int i = 0; i <= 10; ++i) {
					DebugLog.log("Calculating DP " + i + ": " + calculateDP(dp, ship, i));
				}*/
				//Networking.move(ship, calculate(dp, ship));
				Networking.move(ship, calculateDirection(dp, ship, 15 - gameMap.getCurrentTurn() % 15));
			}
			Networking.endTurn();
		}
	}
	public Direction calculate(DP dp, Ship ship) {
		int turns = 0;
		int lastCalculation = 0;
		int lastDelta = 0;
		int calculation;
		while ((calculation = Math.max(calculateDP(dp, ship, turns), 0)) - lastCalculation >= lastDelta) {
			DebugLog.log("Calculation: " + calculation + " - " + (turns - 1) + " - " + lastDelta);
			lastDelta = calculation - lastCalculation;
			lastCalculation = calculation;
			turns += 2;
		}
		DebugLog.log("Final Calculation: " + calculation + " - " + (turns - 1) + " - " + lastDelta);
		return dp.trace(ship.getHalite(), turns - 2, ship.getLocation(), EMPTY_MINEMAP);
	}
	public boolean isSafeToSpawnShip(GameMap gameMap) {
		Ship ship = gameMap.getShip(gameMap.getMyPlayer().getShipyardLocation());
		return ship == null || ship.getPlayerId() != gameMap.getMyPlayerId();
	}
	public int calculateDP(DP dp, Ship ship, int turns) {
		return dp.calculate(ship.getHalite(), turns, ship.getLocation(), EMPTY_MINEMAP);
	}
	public Direction calculateDirection(DP dp, Ship ship, int turns) {
		DebugLog.log(String.format("\tDP for Ship %d - %s - %d halite",
				ship.getShipId(), ship.getLocation().toString(), ship.getHalite()));
		int calculated = calculateDP(dp, ship, turns);
		DebugLog.log("\t\tCalculated: " + calculated);
		Direction direction = dp.trace(ship.getHalite(), turns, ship.getLocation(), EMPTY_MINEMAP);
		DebugLog.log("\t\tDirection: " + direction);
		return direction;
	}
}
