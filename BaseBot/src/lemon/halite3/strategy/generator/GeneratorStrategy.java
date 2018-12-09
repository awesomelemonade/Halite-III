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
import lemon.halite3.util.GamePlayer;
import lemon.halite3.util.Networking;
import lemon.halite3.util.Ship;
import lemon.halite3.util.Vector;

public class GeneratorStrategy implements Strategy {
	private GameMap gameMap;
	private boolean debug;
	private double timeout;
	private Map<Vector, Integer> shipyardScores;
	private PriorityQueue<Vector> shipyardQueue;
	@Override
	public String init(GameMap gameMap, boolean debug, double timeout) {
		// Generator Strategy because MinePlans are now generator-type rather than statically defined based off squares. Therefore, thresholds can be more flexible.
		this.gameMap = gameMap;
		this.debug = debug;
		this.timeout = timeout;
		Vector.init(gameMap);
		DP.init(gameMap);
		Navigation.init(gameMap);
		Heuristics.init(gameMap);
		
		shipyardScores = new HashMap<Vector, Integer>();
		shipyardQueue = new PriorityQueue<Vector>(new Comparator<Vector>() {
			@Override
			public int compare(Vector a, Vector b) {
				return Integer.compare(shipyardScores.get(a), shipyardScores.get(b));
			}
		});
		
		int haliteNeeded = GameConstants.MAX_HALITE - 50;
		Map<Vector, Integer> mineMap = new HashMap<Vector, Integer>();
		for (int i = 0; i < gameMap.getWidth(); ++i) {
			for (int j = 0; j < gameMap.getHeight(); ++j) {
				Vector vector = Vector.getInstance(i, j);
				for (int k = 0; k < 8; ++k) {
					Quad quad = getQuad(vector, k);
					if (getHaliteCount(quad, mineMap) > haliteNeeded * 4) {
						Set<Vector> vectors = getVectors(mineMap, quad, haliteNeeded);
						HeuristicsPlan plan = getPlan(mineMap, vectors, 0, GameConstants.MAX_HALITE - 50, gameMap.getMyPlayer().getShipyardLocation(), Integer.MAX_VALUE);
						shipyardScores.put(vector, plan.getTotalTurns());
						shipyardQueue.add(vector);
						break;
					}
				}
			}
		}
		return "GeneratorStrategy";
	}
	public void updateShipyardScores(Map<Vector, Integer> mineMap, int num) {
		try (Benchmark b = new Benchmark("Updating " + num + " Shipyard Scores: %ss")) {
			// Update shipyardScores and shipyardQueue
			List<Vector> updated = new ArrayList<Vector>();
			for (int i = 0; i < num; ++i) {
				Vector polled = shipyardQueue.poll();
				for (int k = 0; k < 8; ++k) {
					Quad quad = getQuad(polled, k);
					if (getHaliteCount(quad, mineMap) > 950 * 4) {
						Set<Vector> vectors = getVectors(mineMap, quad, 950);
						HeuristicsPlan plan = getPlan(mineMap, vectors, 0, GameConstants.MAX_HALITE - 50, gameMap.getMyPlayer().getShipyardLocation(), Integer.MAX_VALUE);
						shipyardScores.put(polled, plan.getTotalTurns());
						break;
					}
				}
				updated.add(polled);
			}
			for (Vector vector : updated) {
				shipyardQueue.add(vector);
			}
			// TODO update squares around dropped halite
		}
	}
	private long stateSaverTime = 0;
	public boolean isLowOnTime(Benchmark benchmark, double millis) {
		return ((double) (benchmark.peek() - stateSaverTime)) / 1000000000.0 > (timeout - millis);
	}
	@Override
	public void run(GameMap gameMap) {
		// shipPriority can be optimized using a balanced tree to have O(log(n)) reordering rather than O(n)
		List<Integer> shipPriorities = new ArrayList<Integer>(); // In order of the last time they dropped off halite
		Set<Integer> returningShips = new HashSet<Integer>();
		Map<Integer, Vector> lastPlan = new HashMap<Integer, Vector>();
		while (true) {
			gameMap.update();
			stateSaverTime = 0;
			try (Benchmark benchmark = new Benchmark("Benchmark: %ss")) {
				DebugLog.log("New Turn: " + gameMap.getCurrentTurn() + " - numShips=" + gameMap.getMyPlayer().getShips().size() + 
						" ***********************************************");
				try (Benchmark b = new Benchmark("DP: %ss")) {
					DP.reset();
				}
				MoveQueue moveQueue = new MoveQueue(gameMap);
				// Mark enemies to not crash into them
				for (GamePlayer player : gameMap.getPlayers()) {
					if (player.getPlayerId() == gameMap.getMyPlayerId()) {
						continue;
					}
					for (Ship ship : player.getShips().values()) {
						if (ship.getHalite() < GameConstants.MAX_HALITE * 4 / 5) {
							if (ship.getLocation().getManhattanDistance(gameMap.getMyPlayer().getShipyardLocation(), gameMap) > 1) { // Prevent shipyard blocking - TODO: prevent dropoff blocking
								for (Direction direction : Direction.values()) {
									moveQueue.markUnsafe(ship.getLocation().add(direction, gameMap));
								}
							}
						}
					}
				}
				Map<Vector, Integer> mineMap = new HashMap<Vector, Integer>();
				// updates shipPriority
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
						if (ship.getLocation().equals(gameMap.getMyPlayer().getShipyardLocation())) {
							updateShipyardScores(mineMap, 50);
							// Execute plan
							Vector vector = shipyardQueue.peek();
							for (int k = 0; k < 8; ++k) {
								Quad bestQuad = getQuad(vector, k);
								if (getHaliteCount(bestQuad, mineMap) > 950 * 4) {
									Set<Vector> vectors = getVectors(mineMap, bestQuad, 950);
									HeuristicsPlan bestPlan = getPlan(mineMap, vectors, 0, GameConstants.MAX_HALITE - 50, gameMap.getMyPlayer().getShipyardLocation(), Integer.MAX_VALUE);
									lastPlan.put(shipId, vector);
									// Apply bestPlan's mineMap
									for (Vector v : bestPlan.getMineMap().keySet()) {
										mineMap.put(v, mineMap.getOrDefault(v, 0) + bestPlan.getMineMap().get(v));
									}
									handleMicro(moveQueue, ship, bestPlan, bestQuad);
									break;
								}
							}
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
								moveQueue.move(ship, Navigation.navigate(ship.getLocation(), gameMap.getMyPlayer().getShipyardLocation())); // TODO dropoffs
								continue;
							}
						}
						// State Saver Info
						Map<Vector, Quad> quads = new HashMap<Vector, Quad>();
						Map<Vector, HeuristicsPlan> plans = new HashMap<Vector, HeuristicsPlan>();
						// BFS search for mine target
						Deque<Vector> queue = new ArrayDeque<Vector>();
						Set<Vector> visited = new HashSet<Vector>();
						HeuristicsPlan bestPlan = null;
						int bestPlanScore = Integer.MAX_VALUE;
						Quad bestQuad = null;
						Vector bestVector = null;
						queue.add(ship.getLocation());
						visited.add(ship.getLocation());
						boolean condition = debug && shipId == shipPriorities.get(shipPriorities.size() - 1);
						while (!queue.isEmpty()) {
							Vector vector = queue.poll();
							if (condition) {
								try (Benchmark b = new Benchmark()){
									for (int i = 0; i < 8; ++i) {
										Quad quad = getQuad(vector, i);
										if (getHaliteCount(quad, mineMap) > haliteNeeded * 4) {
											Set<Vector> vectors = getVectors(mineMap, quad, haliteNeeded);
											HeuristicsPlan plan = getPlan(mineMap, vectors, ship.getHalite(), GameConstants.MAX_HALITE - 50, ship.getLocation(), Integer.MAX_VALUE);
											quads.put(vector, quad);
											plans.put(vector, plan);
											break;
										}
									}
									stateSaverTime += b.peek();
								}
							}
							if ((lastPlan.containsKey(shipId) && vector.getManhattanDistance(lastPlan.get(shipId), gameMap) < 3) || 
									((!isLowOnTime(benchmark, 50)) && ship.getLocation().equals(gameMap.getMyPlayer().getShipyardLocation())) || 
									((!isLowOnTime(benchmark, 200)) && (Math.random() < (0.05 - 2 * ((gameMap.getWidth() >= 56 ? 1 : 0) + (gameMap.getMyPlayer().getShips().size() >= 30 ? 1 : 0)))))) {
								// TODO: break when there's no point of looking for more (bestPlanScore < vector.getManhattanDistance(vector, gameMap))
								for (int i = 0; i < 8; ++i) {
									Quad quad = getQuad(vector, i);
									if (getHaliteCount(quad, mineMap) > haliteNeeded * 4) { // Arbitrary threshold greater than haliteNeeded
										Set<Vector> vectors = getVectors(mineMap, quad, haliteNeeded);
										HeuristicsPlan plan = getPlan(mineMap, vectors, ship.getHalite(), GameConstants.MAX_HALITE - 50, ship.getLocation(), bestPlanScore);
										if (plan != null) {
											int score = plan.getTotalTurns();
											if (score < bestPlanScore) {
												bestPlan = plan;
												bestPlanScore = score;
												bestQuad = quad;
												bestVector = vector;
											}
										}
										break;
									}
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
							lastPlan.put(shipId, bestVector);
							// Apply bestPlan's mineMap
							for (Vector vector : bestPlan.getMineMap().keySet()) {
								mineMap.put(vector, mineMap.getOrDefault(vector, 0) + bestPlan.getMineMap().get(vector));
							}
							handleMicro(moveQueue, ship, bestPlan, bestQuad);
						}
						// State Saver
						if (bestPlan != null && condition) {
							try (Benchmark b = new Benchmark()) {
								String filename = String.format("gamestates/lol%d-%03d-%03d", gameMap.getMyPlayerId(), gameMap.getCurrentTurn(), shipId);
								StateSaver.save(filename, ship.getLocation(), bestVector, gameMap, mineMap, plans, quads);
								stateSaverTime += b.peek();
								DebugLog.log(String.format("State Saver Time: %ss", Double.toString(((double) stateSaverTime) / 1000000000.0)));
							}
						}
					}
				}
				updateShipyardScores(mineMap, 100);
				// Resolve Collisions
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
	public HeuristicsPlan getPlan(Map<Vector, Integer> mineMap, Set<Vector> vectors, int currentHalite, int haliteNeeded, Vector shipLocation, int cutoff) {
		return Heuristics.execute(shipLocation, currentHalite, haliteNeeded, vectors, gameMap.getMyPlayer().getShipyardLocation(), mineMap, cutoff);
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
