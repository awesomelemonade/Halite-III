package lemon.halite3.strategy.generator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import lemon.halite3.strategy.greedy.Quad;
import lemon.halite3.util.GameConstants;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Vector;

public class MinePlan {
	private GameMap gameMap;
	private Quad quad;
	private Map<Vector, Integer> mineMap;
	private Map<Vector, Integer> tempMineMap;
	private Map<Vector, Integer> mineValues;
	private PriorityQueue<Vector> queue;
	public MinePlan(GameMap gameMap, Quad quad, Map<Vector, Integer> mineMap) {
		this.gameMap = gameMap;
		this.quad = quad;
		this.mineMap = mineMap;
		this.tempMineMap = new HashMap<Vector, Integer>();
		this.mineValues = new HashMap<Vector, Integer>();
		this.queue = new PriorityQueue<Vector>(new Comparator<Vector>() {
			@Override
			public int compare(Vector a, Vector b) {
				// Reversed for descending order
				return Integer.compare(mineValues.get(b), mineValues.get(a));
			}
		});
		for (Vector vector : quad) {
			mineValues.put(vector, getMineValue(vector, mineMap, tempMineMap));
		}
	}
	// Supplier of <Integer, Vector> : <AmountOfHaliteMined, LocationOfHalite>
	public Quad getQuad() {
		return quad;
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
}
