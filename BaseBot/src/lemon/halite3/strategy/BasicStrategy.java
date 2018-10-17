package lemon.halite3.strategy;

import lemon.halite3.util.GameMap;
import lemon.halite3.util.Networking;

public class BasicStrategy implements Strategy {
	@Override
	public String init(GameMap gameMap) {
		return "Lemon's BasicStrategy :D";
	}
	@Override
	public void run(GameMap gameMap) {
		while (true) {
			gameMap.update();
			
			Networking.endTurn();
		}
	}
}
