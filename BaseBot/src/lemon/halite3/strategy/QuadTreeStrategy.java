package lemon.halite3.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lemon.halite3.strategy.QuadTree.Quad;
import lemon.halite3.util.DebugLog;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Networking;
import lemon.halite3.util.Ship;
import lemon.halite3.util.Vector;

public class QuadTreeStrategy implements Strategy {
	@Override
	public String init(GameMap gameMap) {
		return "QuadTreeStrategy";
	}
	@Override
	public void run(GameMap gameMap) {
		while (true) {
			gameMap.update();
			DebugLog.log("New Turn: " + gameMap.getCurrentTurn() + " - numShips=" + gameMap.getMyPlayer().getShips().size() + 
					" ***********************************************");
			QuadTree tree = new QuadTree(gameMap);
			List<Quad> calculated = tree.calculate(1200);
			List<Ship> myShips = new ArrayList<Ship>(gameMap.getMyPlayer().getShips().values());
			double[][] costMatrix = new double[calculated.size()][myShips.size()];
			for (int i = 0; i < calculated.size(); ++i) {
				for (int j = 0; j < myShips.size(); ++j) {
					costMatrix[i][j] = getCost(calculated.get(i), myShips.get(j).getLocation(), gameMap);
				}
			}
			HungarianAlgorithm hungarianAlgorithm = new HungarianAlgorithm(costMatrix);
			int[] assignments = hungarianAlgorithm.execute();
			System.out.println(Arrays.toString(assignments));
			Networking.endTurn();
		}
	}
	public double getCost(Quad quad, Vector vector, GameMap gameMap) {
		return minDistance(gameMap, vector, quad.getLocation(), quad.getLocation().add(quad.getSize().getX(), 0, gameMap),
				quad.getLocation().add(0, quad.getSize().getY(), gameMap), quad.getLocation().add(quad.getSize(), gameMap));
	}
	public double minDistance(GameMap gameMap, Vector vector, Vector... vectors) {
		double min = Double.MAX_VALUE;
		for (Vector v : vectors) {
			min = Math.min(min, vector.getManhattanDistance(v, gameMap));
		}
		return min;
	}
}
