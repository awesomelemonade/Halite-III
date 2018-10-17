package lemon.halite3.strategy;

import lemon.halite3.util.GameMap;

public interface Strategy {
	public String init(GameMap gameMap);
	public void run(GameMap gameMap);
}
