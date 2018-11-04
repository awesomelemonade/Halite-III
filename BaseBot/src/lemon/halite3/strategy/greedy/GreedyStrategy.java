package lemon.halite3.strategy.greedy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import lemon.halite3.strategy.MoveQueue;
import lemon.halite3.strategy.Navigation;
import lemon.halite3.strategy.Strategy;
import lemon.halite3.util.Benchmark;
import lemon.halite3.util.DebugLog;
import lemon.halite3.util.Direction;
import lemon.halite3.util.Dropoff;
import lemon.halite3.util.GameConstants;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Networking;
import lemon.halite3.util.Ship;
import lemon.halite3.util.Vector;

public class GreedyStrategy implements Strategy {
	private GameMap gameMap;
	private Navigation navigation;
	@Override
	public String init(GameMap gameMap) {
		this.gameMap = gameMap;
		Vector.init(gameMap);
		this.navigation = new Navigation(gameMap);
		return "GreedyStrategy";
	}
	@Override
	public void run(GameMap gameMap) {
		// shipPriority can be optimized using a balanced tree to have O(log(n)) reordering rather than O(n)
		List<Integer> shipPriorities = new ArrayList<Integer>(); // In order of the last time they dropped off halite
		Set<Integer> returningShips = new HashSet<Integer>();
		while (true) {
			gameMap.update();
			try (Benchmark benchmark = new Benchmark()) {
				DebugLog.log("New Turn: " + gameMap.getCurrentTurn() + " - numShips=" + gameMap.getMyPlayer().getShips().size() + 
						" ***********************************************");
				MoveQueue moveQueue = new MoveQueue(gameMap);
				Map<Vector, Integer> mineMap = new HashMap<Vector, Integer>();
				// updates shipPriority
				DebugLog.log("Updating Ship Priorities...");
				Ship shipyardCandidate = gameMap.getShip(gameMap.getMyPlayer().getShipyardLocation());
				if (shipyardCandidate != null) {
					shipPriorities.remove((Object) shipyardCandidate.getShipId());
					shipPriorities.add(shipyardCandidate.getShipId());
				}
				for (Dropoff dropoff : gameMap.getMyPlayer().getDropoffs().values()) {
					Ship candidate = gameMap.getShip(dropoff.getLocation());
					if (candidate != null) {
						shipPriorities.remove((Object) candidate.getShipId());
						shipPriorities.add(candidate.getShipId());
					}
				}
				// Remove dead ships
				for (Iterator<Integer> iterator = shipPriorities.iterator(); iterator.hasNext();) {
					int shipId = iterator.next();
					if (!gameMap.getMyPlayer().getShips().containsKey(shipId)) {
						iterator.remove();
					}
				}
				// executes turn in order of shipPriority
				DebugLog.log("Executing Ships: ");
				for (int shipId : shipPriorities) {
					Ship ship = gameMap.getMyPlayer().getShips().get(shipId);
					DebugLog.log("Executing Ship: " + ship.toString());
					// Check if the ship's only option is Direction.STILL
					if (ship.getHalite() < gameMap.getHalite(ship.getLocation()) / GameConstants.MOVE_COST_RATIO) {
						moveQueue.move(ship, Direction.STILL);
						continue;
					}
					int haliteNeeded = GameConstants.MAX_HALITE - ship.getHalite() - 50;
					DebugLog.log("\tHalite Needed: " + haliteNeeded);
					if (haliteNeeded <= 100) {
						returningShips.add(ship.getShipId());
					}
					if (returningShips.contains(ship.getShipId())) {
						if (ship.getHalite() <= GameConstants.MAX_HALITE / 5) {
							returningShips.remove(ship.getShipId());
						} else {
							DebugLog.log("Returning to shipyard");
							moveQueue.move(ship, navigation.navigate(ship.getLocation(), gameMap.getMyPlayer().getShipyardLocation())); // TODO dropoffs
							continue;
						}
					}
					// BFS search for mine target
					Deque<Vector> queue = new ArrayDeque<Vector>();
					Set<Vector> visited = new HashSet<Vector>();
					MinePlan bestPlan = null;
					int bestPlanScore = Integer.MAX_VALUE;
					queue.add(ship.getLocation());
					visited.add(ship.getLocation());
					while (!queue.isEmpty()) {
						Vector vector = queue.poll();
						// TODO: break when there's no point of looking for more (bestPlanScore < vector.getManhattanDistance(vector, gameMap))
						for (int i = 0; i < 8; ++i) {
							Quad quad = getQuad(vector, i);
							if (getHaliteCount(quad) > haliteNeeded * 4) { // Arbitrary threshold greater than GameConstants.MAX_HALITE
								MinePlan plan = getMinePlan(mineMap, quad, haliteNeeded);
								int score = getScore(plan, ship, navigation);
								//benchmark.peek("\t\tFound MinePlan: " + plan + " - " + turns + " - " + bestPlanTurns + " - %s");
								if (score < bestPlanScore) {
									bestPlan = plan;
									bestPlanScore = score;
								}
								if (score == bestPlanScore && plan.getQuad().getSize().getX() < bestPlan.getQuad().getSize().getX()) {
									bestPlan = plan;
								}
								break;
							}
						}
						for (Direction direction : Direction.CARDINAL_DIRECTIONS) {
							Vector candidate = vector.add(direction, gameMap);
							if (!visited.contains(candidate)) {
								queue.add(candidate);
								visited.add(candidate);
							}
						}
					}
					// Execute bestPlan
					if (bestPlan != null) {
						DebugLog.log("\tBest Plan: " + bestPlan.toString());
						// Apply mineMap
						for (Vector vector : bestPlan.getMineMap().keySet()) {
							mineMap.put(vector, mineMap.getOrDefault(vector, 0) + bestPlan.getMineMap().get(vector));
						}
						handleMicro(moveQueue, ship, bestPlan);
					} else {
						DebugLog.log("\tNo Plan :(");
					}
				}
				moveQueue.resolveCollisions(shipPriorities);
				// Try to spawn a ship
				if (moveQueue.isSafe(gameMap.getMyPlayer().getShipyardLocation())) {
					if (gameMap.getMyPlayer().getShips().size() >= 0 && 
							gameMap.getMyPlayer().getHalite() >= GameConstants.SHIP_COST && // TODO - consider cost of building dropoffs in the same turn
							gameMap.getCurrentTurn() + 150 < GameConstants.MAX_TURNS) {
						Networking.spawnShip();
					}
				}
				// End Turn
				moveQueue.send();
				Networking.endTurn();
			}
		}
	}
	public int getScore(MinePlan plan, Ship ship, Navigation navigation) { // Lower score is better
		// Half arbitrary heuristic - TODO: tune weighting
		List<Vector> vectors = new ArrayList<Vector>(plan.getMineMap().keySet());
		int bestDistance = Integer.MAX_VALUE;
		Vector bestVector = null;
		for (Vector vector : vectors) {
			int distance = ship.getLocation().getManhattanDistance(vector, gameMap);
			if (distance < bestDistance) {
				bestDistance = distance;
				bestVector = vector;
			}
		}
		return plan.getCount() + navigation.getCost(ship.getLocation(), bestVector) + 
				getDistance(ship.getLocation(), vectors);
	}
	public int getDistance(Vector start, List<Vector> vectors) {
		int totalDistance = 0;
		Vector currentVector = start;
		while (!vectors.isEmpty()) {
			Vector bestVector = null;
			int bestDistance = Integer.MAX_VALUE;
			for (Vector vector : vectors) {
				int distance = currentVector.getManhattanDistance(vector, gameMap);
				if (distance < bestDistance) {
					bestDistance = distance;
					bestVector = vector;
				}
			}
			totalDistance += bestDistance;
			vectors.remove(bestVector);
			currentVector = bestVector;
		}
		return totalDistance;
	}
	public void handleMicro(MoveQueue moveQueue, Ship ship, MinePlan plan) {
		// TODO: Traveling Salesman? - directionCurrently a greedy algorithm
		Vector bestVector = null;
		int bestDistance = Integer.MAX_VALUE;
		for (Vector vector : plan.getMineMap().keySet()) {
			int distance = ship.getLocation().getManhattanDistance(vector, gameMap);
			if (distance < bestDistance) {
				bestDistance = distance;
				bestVector = vector;
			}
		}
		DebugLog.log("\tNavigating: " + ship.getLocation() + " to " + bestVector);
		moveQueue.move(ship, navigation.navigate(ship.getLocation(), bestVector));
	}
	public int getHaliteCount(Quad quad) {
		int halite = 0;
		for (Vector vector : quad) {
			halite += gameMap.getHalite(vector);
		}
		return halite;
	}
	public int getMineValue(Vector vector, Map<Vector, Integer> mineMap, Map<Vector, Integer> tempMineMap) {
		int haliteLeft = gameMap.getHalite(vector) - mineMap.getOrDefault(vector, 0) - tempMineMap.getOrDefault(vector, 0);
		int mine = (int) Math.ceil(((double) haliteLeft) / ((double) GameConstants.EXTRACT_RATIO));
		if (tempMineMap.getOrDefault(vector, 0) != 0) {
			return mine + haliteLeft / GameConstants.MOVE_COST_RATIO - (haliteLeft - mine) / GameConstants.MOVE_COST_RATIO;
		} else {
			return mine - (haliteLeft - mine) / GameConstants.MOVE_COST_RATIO;
		}
	}
	public MinePlan getMinePlan(Map<Vector, Integer> mineMap, Quad quad, int threshold) {
		Map<Vector, Integer> tempMineMap = new HashMap<Vector, Integer>();
		PriorityQueue<Vector> queue = new PriorityQueue<Vector>(new Comparator<Vector>() {
			@Override
			public int compare(Vector a, Vector b) {
				// Reversed for descending order
				return Integer.compare(getMineValue(b, mineMap, tempMineMap), getMineValue(a, mineMap, tempMineMap));
			}
		});
		for (Vector vector : quad) {
			queue.add(vector);
		}
		int halite = 0;
		int count = 0;
		while (!queue.isEmpty()) {
			Vector vector = queue.poll();
			int mine = (int) Math.ceil(((double) (gameMap.getHalite(vector) - mineMap.getOrDefault(vector, 0) - tempMineMap.getOrDefault(vector, 0))) /
					((double) GameConstants.EXTRACT_RATIO));
			if (mine == 0) {
				return null;
			}
			halite += mine;
			count++;
			tempMineMap.put(vector, tempMineMap.getOrDefault(vector, 0) + mine);
			if (halite >= threshold) {
				return new MinePlan(quad, tempMineMap, count);
			}
			queue.add(vector);
		}
		return null;
	}
	public Quad getQuad(Vector location, int radius) {
		return new Quad(gameMap, location.add(-radius, -radius, gameMap), Vector.getInstance(radius * 2 + 1, radius * 2 + 1));
	}
	class MinePlan {
		private Quad quad;
		private Map<Vector, Integer> mineMap;
		private int count;
		public MinePlan(Quad quad, Map<Vector, Integer> mineMap, int count) {
			this.quad = quad;
			this.mineMap = mineMap;
			this.count = count;
		}
		public Quad getQuad() {
			return quad;
		}
		public Map<Vector, Integer> getMineMap() {
			return mineMap;
		}
		public int getCount() {
			return count;
		}
		@Override
		public String toString() {
			return String.format("MinePlan[quad=%s, count=%d, map=%s]", quad.toString(), count, mineMap.toString());
		}
	}
}
