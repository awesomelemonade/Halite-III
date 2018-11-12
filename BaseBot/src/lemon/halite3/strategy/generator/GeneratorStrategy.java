package lemon.halite3.strategy.generator;

import lemon.halite3.strategy.Strategy;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Networking;

public class GeneratorStrategy implements Strategy {
	private GameMap gameMap;
	@Override
	public String init(GameMap gameMap) {
		// Generator Strategy because MinePlans are now generator-type rather than statically defined based off squares. Therefore, thresholds can be more flexible.
		this.gameMap = gameMap;
		return "GeneratorStrategy";
	}
	@Override
	public void run(GameMap gameMap) {
		while (true) {
			gameMap.update();
			
			Networking.endTurn();
		}
	}
}
