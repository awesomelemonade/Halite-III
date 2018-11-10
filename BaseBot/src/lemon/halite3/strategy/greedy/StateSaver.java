package lemon.halite3.strategy.greedy;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lemon.halite3.strategy.greedy.GreedyStrategy.MinePlan;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Vector;

public class StateSaver {
	public static void save(String filename, Vector shipLocation, Vector bestPlan, GameMap gameMap, Map<Vector, Integer> mineMap,
			Map<Vector, MinePlan> minePlans, Map<Vector, Integer> scores, Map<Vector, List<Vector>> paths) {
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
					MinePlan plan = minePlans.get(Vector.getInstance(i, j));
					writer.printf("%d %d %d %d ", plan.getQuad().getLocation().getX(), plan.getQuad().getLocation().getY(),
							plan.getQuad().getSize().getX(), plan.getQuad().getSize().getY());
					writer.print(plan.getMineMap().size() + " ");
					for (Entry<Vector, Integer> entry : plan.getMineMap().entrySet()) {
						writer.printf("%d %d %d ", entry.getKey().getX(), entry.getKey().getY(), entry.getValue());
					}
					writer.print(paths.get(Vector.getInstance(i, j)).size() + " ");
					for (Vector vector : paths.get(Vector.getInstance(i, j))) {
						writer.printf("%d %d ", vector.getX(), vector.getY());
					}
					writer.print(scores.get(Vector.getInstance(i, j)) + " ");
					writer.println(plan.getCount());
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
