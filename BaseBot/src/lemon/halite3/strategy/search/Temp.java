package lemon.halite3.strategy.search;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import lemon.halite3.util.DebugLog;
import lemon.halite3.util.Direction;
import lemon.halite3.util.GameConstants;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Vector;

public class Temp {
	private GameMap gameMap;
	public Temp(GameMap gameMap) {
		this.gameMap = gameMap;
	}
	public void search(Vector vector, int halite, Vector target, int threshold) {
		PriorityQueue<State> states = new PriorityQueue<State>();
		states.add(new State(vector, halite, 0));
		int counter = 0;
		while (!states.isEmpty()) {
			State state = states.poll();
			DebugLog.log("eyooooo: " + state.getVector() + " - " + counter + " - " + states.size());
			// Check if exit condition
			if (state.getVector().equals(target) && state.getHalite() >= threshold) {
				// Done!
				break;
			}
			// Mine 1 turn
			int mined = getMined(gameMap.getHalite(state.getVector()) - state.getMineMap().getOrDefault(state.getVector(), 0));
			states.add(new State(state.getVector(), state.getHalite() + mined,
					getMineMap(state.getMineMap(), state.getVector(), mined), state.getPriority() + 1));
			// Try moving
			int[][] dp = new int[gameMap.getWidth()][gameMap.getHeight()];
			for (int i = 0; i < dp.length; ++i) {
				for (int j = 0; j < dp[0].length; ++j) {
					dp[i][j] = Integer.MAX_VALUE;
				}
			}
			Deque<Vector> deque = new ArrayDeque<Vector>();
			for (Direction direction : Direction.CARDINAL_DIRECTIONS) {
				deque.add(state.getVector().add(direction, gameMap));
			}
			dp[state.getVector().getX()][state.getVector().getY()] = 
					gameMap.getHalite(state.getVector().getX(), state.getVector().getY()) / GameConstants.MOVE_COST_RATIO;
			while (!deque.isEmpty()) {
				Vector v = deque.poll();
				counter++;
				for (Direction direction : Direction.CARDINAL_DIRECTIONS) {
					Vector x = v.add(direction, gameMap);
					if (dp[x.getX()][x.getY()] != Integer.MAX_VALUE) {
						dp[v.getX()][v.getY()] = Math.min(dp[v.getX()][v.getY()], dp[x.getX()][x.getY()]);
					}
				}
				if (dp[v.getX()][v.getY()] <= state.getHalite()) {
					states.add(new State(v, state.getHalite() - dp[v.getX()][v.getY()],
							state.getMineMap(), state.getPriority() + state.getVector().getManhattanDistance(v, gameMap)));
					dp[v.getX()][v.getY()] += gameMap.getHalite(v) / GameConstants.MOVE_COST_RATIO;
					if (dp[v.getX()][v.getY()] <= state.getHalite()) {
						for (Direction direction : Direction.CARDINAL_DIRECTIONS) {
							Vector x = v.add(direction, gameMap);
							if (dp[x.getX()][x.getY()] == Integer.MAX_VALUE) {
								deque.add(x);
							}
						}
					}
				}
				//DebugLog.log(v.toString() + ": " + dp[v.getX()][v.getY()]);
			}
		}
	}
	public int getMined(int halite) {
		return (halite + GameConstants.EXTRACT_RATIO - 1) / GameConstants.EXTRACT_RATIO; // Rounds up without Math.ceil()
	}
	public Map<Vector, Integer> getMineMap(Map<Vector, Integer> mineMap, Vector vector, int change){
		Map<Vector, Integer> newMineMap = new HashMap<Vector, Integer>(mineMap);
		newMineMap.put(vector, newMineMap.getOrDefault(vector, 0) + change);
		return newMineMap;
	}
	class State implements Comparable<State> {
		private Vector vector;
		private int halite;
		private Map<Vector, Integer> mineMap;
		private int priority;
		// prevState to track history?
		public State(Vector vector, int halite, Map<Vector, Integer> mineMap, int priority) {
			this.vector = vector;
			this.halite = halite;
			this.mineMap = mineMap;
			this.priority = priority;
		}
		public State(Vector vector, int halite, int priority) {
			this(vector, halite, new HashMap<Vector, Integer>(), priority);
		}
		@Override
		public int compareTo(State a) {
			return Integer.compare(priority, a.getPriority());
		}
		public Vector getVector() {
			return vector;
		}
		public int getHalite() {
			return halite;
		}
		public Map<Vector, Integer> getMineMap() {
			return mineMap;
		}
		public int getPriority() {
			return priority;
		}
	}
}
