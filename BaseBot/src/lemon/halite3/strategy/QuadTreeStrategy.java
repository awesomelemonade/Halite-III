package lemon.halite3.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

import lemon.halite3.strategy.QuadTree.Quad;
import lemon.halite3.util.DebugLog;
import lemon.halite3.util.Direction;
import lemon.halite3.util.GameConstants;
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
			if (gameMap.getMyPlayer().getShips().size() == 0 && 
					gameMap.getMyPlayer().getHalite() >= GameConstants.SHIP_COST &&
					isSafeToSpawnShip(gameMap) && 
					gameMap.getCurrentTurn() + 20 < GameConstants.MAX_TURNS) {
				Networking.spawnShip();
			}
			MoveQueue moveQueue = new MoveQueue(gameMap);
			QuadTree tree = new QuadTree(gameMap);
			List<Ship> myShips = new ArrayList<Ship>(gameMap.getMyPlayer().getShips().values());
			List<Quad> calculated = tree.calculate(GameConstants.MAX_HALITE - 50);
			if (myShips.size() > 0) {
				double[][] costMatrix = new double[myShips.size()][calculated.size()];
				for (int j = 0; j < calculated.size(); ++j) {
					int turnsToMine = getTurnsToMine(calculated.get(j), GameConstants.MAX_HALITE, gameMap) - 1;
					for (int i = 0; i < myShips.size(); ++i) {
						costMatrix[i][j] = minDistance(gameMap, calculated.get(j), myShips.get(i).getLocation()) + turnsToMine;
					}
				}
				DebugLog.log("Cost Matrix: " + myShips.size() + " - " + calculated.size());
				HungarianAlgorithm hungarianAlgorithm = new HungarianAlgorithm(costMatrix);
				int[] assignments = hungarianAlgorithm.execute();
				DebugLog.log(Arrays.toString(assignments));
				
				Navigation navigation = new Navigation(gameMap);
				
				// TODO what to do when they reach the quad; store quad over turns and mark them not harvestable
				for (int i = 0; i < myShips.size(); ++i) {
					if (assignments[i] != -1) {
						DebugLog.log("Assigning ship: " + myShips.get(i) + " - " + calculated.get(assignments[i]));
						Vector startVector = myShips.get(i).getLocation();
						Vector targetVector = minVectorByDistance(gameMap, calculated.get(assignments[i]), startVector);
						Direction direction = navigation.navigate(startVector, targetVector);
						moveQueue.move(myShips.get(i), direction);
					} else {
						DebugLog.log("No assignment for ship: " + myShips.get(i));
					}
				}
			} else {
				DebugLog.log("No Ships :(");
			}
			moveQueue.resolveCollisions();
			moveQueue.send();
			Networking.endTurn();
		}
	}
	public int getTurnsToMine(Quad quad, int threshold, GameMap gameMap) {
		// LOL this whole thing is ridiculously inefficient
		PriorityQueue<Integer> queue = new PriorityQueue<Integer>(Collections.reverseOrder());
		for (int i = 0; i < quad.getSize().getX(); ++i) {
			for (int j = 0; j < quad.getSize().getY(); ++j) {
				int halite = gameMap.getHalite(quad.getLocation().add(i, j, gameMap));
				while (halite > 0) { // Some really inefficient way :)
					int mined = (int) Math.ceil(((double) halite) / ((double) GameConstants.EXTRACT_RATIO));
					queue.add(mined);
					halite -= mined;
				}
			}
		}
		int halite = 0;
		int turns = 0;
		while (!queue.isEmpty()) {
			halite += queue.poll();
			turns++;
			if (halite >= threshold) {
				return turns;
			}
		}
		return Integer.MAX_VALUE;
	}
	public double minDistance(GameMap gameMap, Quad quad, Vector vector) {
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
	public Vector minVectorByDistance(GameMap gameMap, Quad quad, Vector vector) {
		return minVectorByDistance(gameMap, vector, quad.getLocation(), quad.getLocation().add(quad.getSize().getX(), 0, gameMap),
				quad.getLocation().add(0, quad.getSize().getY(), gameMap), quad.getLocation().add(quad.getSize(), gameMap));
	}
	public Vector minVectorByDistance(GameMap gameMap, Vector vector, Vector... vectors) {
		double min = Double.MAX_VALUE;
		Vector ret = null;
		for (Vector v : vectors) {
			int distance = vector.getManhattanDistance(v, gameMap);
			if (distance < min) {
				min = distance;
				ret = v;
			}
		}
		return ret;
	}
	public boolean isSafeToSpawnShip(GameMap gameMap) {
		Ship ship = gameMap.getShip(gameMap.getMyPlayer().getShipyardLocation());
		return ship == null || ship.getPlayerId() != gameMap.getMyPlayerId();
	}
}
