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
		
		int targetHalite = GameConstants.MAX_HALITE - 50;
		Map<Vector, Integer> mineMap = new HashMap<Vector, Integer>();
		for (int i = 0; i < gameMap.getWidth(); ++i) {
			for (int j = 0; j < gameMap.getHeight(); ++j) {
				Vector vector = Vector.getInstance(i, j);
				HeuristicsPlan plan = getPlan(vector, mineMap, gameMap.getMyPlayer().getShipyardLocation(), 0, targetHalite, GameConstants.MAX_TURNS);
				if (plan == null) {
					shipyardScores.put(vector, 99999999);
				} else if (plan.getHalite() < targetHalite) {
					shipyardScores.put(vector, 99999 - plan.getHalite());
				} else {
					shipyardScores.put(vector, plan.getTotalTurns());
				}
				shipyardQueue.add(vector);
			}
		}
		return "GeneratorStrategy";
	}
	public HeuristicsPlan getPlan(Vector vector, Map<Vector, Integer> mineMap, Vector location, int currentHalite, int targetHalite, int cutoff) {
		if (location.equals(vector) || gameMap.getMyPlayer().getShipyardLocation().equals(vector)) {
			return null;
		}
		return Heuristics.execute(location, vector, gameMap.getMyPlayer().getShipyardLocation(), currentHalite, targetHalite, mineMap, cutoff);
	}
	public void updateShipyardScores(Map<Vector, Integer> mineMap, int targetHalite, int num, int turnsLeft) {
		try (Benchmark b = new Benchmark("Updating " + num + " Shipyard Scores: %ss")) {
			// Update shipyardScores and shipyardQueue
			Vector[] updated = new Vector[num];
			for (int i = 0; i < num; ++i) {
				Vector polled = shipyardQueue.poll();
				HeuristicsPlan plan = getPlan(polled, mineMap, gameMap.getMyPlayer().getShipyardLocation(), 0, targetHalite, turnsLeft);
				if (plan == null) {
					shipyardScores.put(polled, 99999999);
				} else if (plan.getHalite() < targetHalite) {
					shipyardScores.put(polled, 99999 - plan.getHalite());
				} else {
					shipyardScores.put(polled, plan.getTotalTurns());
				}
				updated[i] = polled;
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
	public int getTotalShips() {
		int ships = 0;
		for (GamePlayer player : gameMap.getPlayers()) {
			ships += player.getShips().size();
		}
		return ships;
	}
	@Override
	public void run(GameMap gameMap) {
		// shipPriority can be optimized using a balanced tree to have O(log(n)) reordering rather than O(n)
		List<Integer> shipPriorities = new ArrayList<Integer>(); // In order of the last time they dropped off halite
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
				final int turnBuffer = Math.min(gameMap.getMyPlayer().getShips().size() / 2 + 1, 10);
				final int turnsLeft = GameConstants.MAX_TURNS - gameMap.getCurrentTurn() - turnBuffer;
				int[][] inspired = new int[gameMap.getWidth()][gameMap.getHeight()];
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
				int targetHalite = GameConstants.MAX_HALITE - 50;
				try (Benchmark b3 = new Benchmark("Executed Ships: %ss")) {
					for (int shipId : shipPriorities) {
						Ship ship = gameMap.getMyPlayer().getShips().get(shipId);
						if (ship.getLocation().equals(gameMap.getMyPlayer().getShipyardLocation())) { // Special case for ships just spawned or just dropped off halite
							updateShipyardScores(mineMap, targetHalite, 50, turnsLeft);
							// Execute plan
							Vector vector = shipyardQueue.peek();
							HeuristicsPlan plan = getPlan(vector, mineMap, gameMap.getMyPlayer().getShipyardLocation(), 0, targetHalite, turnsLeft);
							if (plan == null) {
								moveQueue.move(shipId, Direction.STILL);
							} else {
								lastPlan.put(shipId, vector);
								// Apply bestPlan's mineMap
								for (Vector v : plan.getMineMap().keySet()) {
									mineMap.put(v, mineMap.getOrDefault(v, 0) + plan.getMineMap().get(v));
								}
								handleMicro(moveQueue, ship, plan);
							}
							continue;
						}
						// State Saver Info
						Map<Vector, Quad> quads = new HashMap<Vector, Quad>();
						Map<Vector, HeuristicsPlan> plans = new HashMap<Vector, HeuristicsPlan>();
						// BFS search for mine target
						Deque<Vector> queue = new ArrayDeque<Vector>();
						Set<Vector> visited = new HashSet<Vector>();
						HeuristicsPlan bestPlan = null;
						for (int i = 0; bestPlan == null && i <= turnBuffer; i++) { // lol lazy hack
							bestPlan = Heuristics.execute(ship.getLocation(), gameMap.getMyPlayer().getShipyardLocation(),
									ship.getHalite(), targetHalite, mineMap, turnsLeft + i);
						}
						Vector bestVector = null;
						queue.add(ship.getLocation());
						visited.add(ship.getLocation());
						boolean condition = debug && shipId == shipPriorities.get(shipPriorities.size() - 1) && false;
						while (!queue.isEmpty()) {
							Vector vector = queue.poll();
							if (condition) {
								try (Benchmark b = new Benchmark()){
									HeuristicsPlan plan = getPlan(vector, mineMap, ship.getLocation(), ship.getHalite(), targetHalite, Integer.MAX_VALUE);
									plans.put(vector, plan);
									stateSaverTime += b.peek();
								}
							}
							if ((lastPlan.containsKey(shipId) && lastPlan.get(shipId) != null && vector.getManhattanDistance(lastPlan.get(shipId), gameMap) < 3) || 
									((!isLowOnTime(benchmark, 50)) && ship.getLocation().equals(gameMap.getMyPlayer().getShipyardLocation())) || 
									((!isLowOnTime(benchmark, 200)) && (Math.random() < (0.05 - 0.02 * ((gameMap.getWidth() >= 48 ? 1 : 0) + (gameMap.getMyPlayer().getShips().size() >= 20 ? 1 : 0)))))) {
								// TODO: break when there's no point of looking for more (bestPlanScore < vector.getManhattanDistance(vector, gameMap))
								int cutoff = bestPlan == null || bestPlan.getHalite() < targetHalite ? turnsLeft : bestPlan.getTotalTurns();
								HeuristicsPlan plan = getPlan(vector, mineMap, ship.getLocation(), ship.getHalite(), targetHalite, cutoff);
								if (plan != null) {
									if (bestPlan == null) {
										bestPlan = plan;
										bestVector = vector;
									} else {
										if ((bestPlan.getHalite() >= targetHalite) ? (plan.getHalite() >= targetHalite) : (plan.getHalite() >= bestPlan.getHalite())) {
											bestPlan = plan;
											bestVector = vector;
										}
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
							handleMicro(moveQueue, ship, bestPlan);
						} else {
							//moveQueue.createDropoff(shipId);
							moveQueue.move(shipId, Direction.STILL);
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
				updateShipyardScores(mineMap, targetHalite, 100, turnsLeft);
				// Check for endgame ignore crashing
				Vector vector = shipyardQueue.peek();
				HeuristicsPlan plan = getPlan(vector, mineMap, gameMap.getMyPlayer().getShipyardLocation(), 0, GameConstants.MAX_HALITE - 50, turnsLeft);
				if (plan == null) {
					moveQueue.markIgnore(gameMap.getMyPlayer().getShipyardLocation());
				} else {
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
				}
				// Resolve Collisions
				moveQueue.resolveCollisions(shipPriorities);
				// Try to spawn a ship
				if (moveQueue.isSafe(gameMap.getMyPlayer().getShipyardLocation())) {
					if (gameMap.getMyPlayer().getHalite() >= GameConstants.SHIP_COST) {
						if (plan != null && plan.getHalite() >= GameConstants.MAX_HALITE - 50) {
							Networking.spawnShip();
						}
					}
					/*if (gameMap.getMyPlayer().getShips().size() >= 0 && 
							gameMap.getMyPlayer().getHalite() >= GameConstants.SHIP_COST && // TODO - consider cost of building dropoffs in the same turn
							gameMap.getCurrentTurn() < GameConstants.MAX_TURNS / 2) {
						Networking.spawnShip();
					}*/
				}
				moveQueue.send();
				Networking.endTurn();
			}
		}
	}
	public void handleMicro(MoveQueue moveQueue, Ship ship, HeuristicsPlan plan) {
		if (plan.getMineCounts().getOrDefault(ship.getLocation(), 0) > 0 && 
				(ship.getHalite() + Heuristics.getMined(gameMap.getHalite(ship.getLocation())) <= GameConstants.MAX_HALITE)) {
			moveQueue.move(ship, Direction.STILL);
		} else {
			moveQueue.move(ship, ship.getLocation().getDirectionTo(plan.getTotalPath().get(1), gameMap), plan.getAlternateDirection());
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
