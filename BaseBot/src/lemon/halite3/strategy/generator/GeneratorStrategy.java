package lemon.halite3.strategy.generator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import lemon.halite3.strategy.MoveQueue;
import lemon.halite3.strategy.Navigation;
import lemon.halite3.strategy.Strategy;
import lemon.halite3.strategy.generator.Heuristics.HeuristicsPlan;
import lemon.halite3.strategy.greedy.Quad;
import lemon.halite3.util.Benchmark;
import lemon.halite3.util.DebugLog;
import lemon.halite3.util.Direction;
import lemon.halite3.util.Dropoff;
import lemon.halite3.util.GameConstants;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Networking;
import lemon.halite3.util.Ship;
import lemon.halite3.util.Vector;

public class GeneratorStrategy implements Strategy {
	private GameMap gameMap;
	@Override
	public String init(GameMap gameMap) {
		// Generator Strategy because MinePlans are now generator-type rather than statically defined based off squares. Therefore, thresholds can be more flexible.
		this.gameMap = gameMap;
		Vector.init(gameMap);
		DP.init(gameMap);
		return "GeneratorStrategy";
	}
	@Override
	public void run(GameMap gameMap) {
		// shipPriority can be optimized using a balanced tree to have O(log(n)) reordering rather than O(n)
		List<Integer> shipPriorities = new ArrayList<Integer>(); // In order of the last time they dropped off halite
		Set<Integer> returningShips = new HashSet<Integer>();
		Heuristics.init(gameMap);
		while (true) {
			gameMap.update();
			try (Benchmark benchmark = new Benchmark("Benchmark: %ss")) {
				DebugLog.log("New Turn: " + gameMap.getCurrentTurn() + " - numShips=" + gameMap.getMyPlayer().getShips().size() + 
						" ***********************************************");
				try (Benchmark b = new Benchmark("DP: %ss")) {
					DP.reset();
				}
				MoveQueue moveQueue = new MoveQueue(gameMap);
				Map<Vector, Integer> mineMap = new HashMap<Vector, Integer>();
				// updates shipPriority
				DebugLog.log("Updating Ship Priorities...");
				try (Benchmark b2 = new Benchmark("Updated Ship Priorities: %ss")) {
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
					shipPriorities.removeIf(shipId -> !gameMap.getMyPlayer().getShips().containsKey(shipId));
				}
				try (Benchmark b3 = new Benchmark("Executed Ships: %ss")) {
					for (int shipId : shipPriorities) {
						Ship ship = gameMap.getMyPlayer().getShips().get(shipId);
						DebugLog.log("Executing Ship: " + ship.toString());
						// Check if the ship's only option is Direction.STILL
						if (ship.getHalite() < gameMap.getHalite(ship.getLocation()) / GameConstants.MOVE_COST_RATIO) {
							moveQueue.move(ship, Direction.STILL);
							continue;
						}
						int haliteNeeded = GameConstants.MAX_HALITE - ship.getHalite() - 50;
						if (haliteNeeded <= 0) {
							returningShips.add(ship.getShipId());
						}
						if (returningShips.contains(ship.getShipId())) {
							if (ship.getHalite() <= GameConstants.MAX_HALITE / 5) {
								returningShips.remove(ship.getShipId());
							} else {
								moveQueue.move(ship, new Navigation(gameMap).navigate(ship.getLocation(), gameMap.getMyPlayer().getShipyardLocation())); // TODO dropoffs
								continue;
							}
						}
						// BFS search for mine target
						Deque<Vector> queue = new ArrayDeque<Vector>();
						Set<Vector> visited = new HashSet<Vector>();
						HeuristicsPlan bestPlan = null;
						int bestPlanScore = Integer.MAX_VALUE;
						Quad bestQuad = null;
						queue.add(ship.getLocation());
						visited.add(ship.getLocation());
						while (!queue.isEmpty()) {
							Vector vector = queue.poll();
							// TODO: break when there's no point of looking for more (bestPlanScore < vector.getManhattanDistance(vector, gameMap))
							for (int i = 0; i < 8; ++i) {
								Quad quad = getQuad(vector, i);
								if (getHaliteCount(quad, mineMap) > haliteNeeded * 4) { // Arbitrary threshold greater than haliteNeeded
									Set<Vector> vectors = getVectors(mineMap, quad, haliteNeeded);
									HeuristicsPlan plan = getPlan(mineMap, vectors, GameConstants.MAX_HALITE - 50, ship);
									int score = plan.getTotalTurns();
									//benchmark.peek("\t\tFound MinePlan: " + plan + " - " + turns + " - " + bestPlanTurns + " - %s");
									if (score < bestPlanScore) {
										bestPlan = plan;
										bestPlanScore = score;
										bestQuad = quad;
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
						if (bestPlan != null) {
							// Apply bestPlan's mineMap
							for (Vector vector : bestPlan.getMineMap().keySet()) {
								mineMap.put(vector, mineMap.getOrDefault(vector, 0) + bestPlan.getMineMap().get(vector));
							}
							handleMicro(moveQueue, ship, bestPlan, bestQuad);
						}
					}
				}
				moveQueue.resolveCollisions(shipPriorities);
				// Try to spawn a ship
				if (moveQueue.isSafe(gameMap.getMyPlayer().getShipyardLocation())) {
					if (gameMap.getMyPlayer().getShips().size() >= 0 && 
							gameMap.getMyPlayer().getHalite() >= GameConstants.SHIP_COST && // TODO - consider cost of building dropoffs in the same turn
							gameMap.getCurrentTurn() < GameConstants.MAX_TURNS / 2) {
						Networking.spawnShip();
					}
				}
				moveQueue.send();
				Networking.endTurn();
			}
		}
	}
	public void handleMicro(MoveQueue moveQueue, Ship ship, HeuristicsPlan plan, Quad quad) {
		if (plan.getMineCounts().getOrDefault(ship.getLocation(), 0) > 0) {
			moveQueue.move(ship, Direction.STILL);
		} else {
			moveQueue.move(ship, ship.getLocation().getDirectionTo(plan.getTotalPath().get(1), gameMap));
		}
	}
	public Set<Vector> getVectors(Map<Vector, Integer> mineMap, Quad quad, int haliteNeeded) {
		Map<Vector, Integer> haliteLeft = new HashMap<Vector, Integer>();
		PriorityQueue<Vector> queue = new PriorityQueue<Vector>(new Comparator<Vector>() {
			@Override
			public int compare(Vector a, Vector b) {
				return Integer.compare(haliteLeft.get(b), haliteLeft.get(a)); // Descending
			}
		});
		for (Vector vector : quad) {
			haliteLeft.put(vector, gameMap.getHalite(vector) - mineMap.getOrDefault(vector, 0));
			queue.add(vector);
		}
		Set<Vector> vectors = new HashSet<Vector>();
		int halite = 0;
		Vector current = queue.poll();
		while (!queue.isEmpty() && halite < haliteNeeded) {
			vectors.add(current);
			Vector polled = queue.poll();
			halite += vectors.size() * (haliteLeft.get(current) - haliteLeft.get(polled));
			current = polled;
		}
		if (halite < haliteNeeded) {
			 vectors.add(current);
		}
		return vectors;
	}
	public HeuristicsPlan getPlan(Map<Vector, Integer> mineMap, Set<Vector> vectors, int haliteNeeded, Ship ship) {
		return Heuristics.execute(ship.getLocation(), ship.getHalite(), haliteNeeded, vectors, gameMap.getMyPlayer().getShipyardLocation(), mineMap);
	}
	public int getHaliteCount(Quad quad, Map<Vector, Integer> mineMap) {
		int halite = 0;
		for (Vector vector : quad) {
			halite += gameMap.getHalite(vector) - mineMap.getOrDefault(vector, 0);
		}
		return halite;
	}
	private Map<Vector, Map<Integer, Quad>> quads = new HashMap<Vector, Map<Integer, Quad>>();
	public Quad getQuad(Vector location, int radius) {
		if (!quads.containsKey(location)) {
			quads.put(location, new HashMap<Integer, Quad>());
		}
		if (!quads.get(location).containsKey(radius)) {
			quads.get(location).put(radius, 
					new Quad(gameMap, location.add(-radius, -radius, gameMap), 
							Vector.getInstance(radius * 2 + 1, radius * 2 + 1)));
		}
		return quads.get(location).get(radius);
	}
}
