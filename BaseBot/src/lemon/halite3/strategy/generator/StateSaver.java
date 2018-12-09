package lemon.halite3.strategy.generator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;

import lemon.halite3.strategy.generator.Heuristics.HeuristicsPlan;
import lemon.halite3.strategy.greedy.Quad;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Vector;

public class StateSaver {
	public static void save(String filename, Vector shipLocation, Vector bestPlan, GameMap gameMap, Map<Vector, Integer> mineMap, Map<Vector, HeuristicsPlan> plans, Map<Vector, Quad> quads) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(filename));
			writer.println(shipLocation.getX() + " " + shipLocation.getY());
			writer.println(bestPlan.getX() + " " + bestPlan.getY());
			writer.println(gameMap.getWidth() + " " + gameMap.getHeight());
			for (int i = 0; i < gameMap.getWidth(); ++i) {
				for (int j = 0; j < gameMap.getHeight(); ++j) {
					writer.print(gameMap.getHalite(i, j) - mineMap.getOrDefault(Vector.getInstance(i, j), 0));
					writer.print(' ');
				}
				writer.println();
			}
			for (int i = 0; i < gameMap.getWidth(); ++i) {
				for (int j = 0; j < gameMap.getHeight(); ++j) {
					Vector vector = Vector.getInstance(i, j);
					HeuristicsPlan plan = plans.get(vector);
					writer.printf("%d %d %d %d ", quads.get(vector).getLocation().getX(), quads.get(vector).getLocation().getY(),
							quads.get(vector).getSize().getX(), quads.get(vector).getSize().getY());
					writer.print(plan.getMineMap().size() + " ");
					for (Entry<Vector, Integer> entry : plan.getMineMap().entrySet()) {
						writer.printf("%d %d %d ", entry.getKey().getX(), entry.getKey().getY(), entry.getValue());
					}
					writer.print(plan.getTotalPath().size() + " ");
					for (Vector v : plan.getTotalPath()) {
						writer.printf("%d %d ", v.getX(), v.getY());
					}
					writer.println(plan.getTotalTurns());
				}
			}
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
