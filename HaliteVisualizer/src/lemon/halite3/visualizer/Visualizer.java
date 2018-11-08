package lemon.halite3.visualizer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JFrame;

public class Visualizer {
	public static void main(String[] args) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader("lol1-2-0"));
		
		// Info
		StringTokenizer infoTokenizer = new StringTokenizer(reader.readLine());
		int width = Integer.parseInt(infoTokenizer.nextToken());
		int height = Integer.parseInt(infoTokenizer.nextToken());
		// Halite Values
		int[][] halite = new int[width][height];
		for (int i = 0; i < halite.length; ++i) {
			StringTokenizer tokenizer = new StringTokenizer(reader.readLine());
			for (int j = 0; j < halite[0].length; ++j) {
				halite[i][j] = Integer.parseInt(tokenizer.nextToken());
			}
		}
		// Mine Plans
		MinePlan[][] minePlans = new MinePlan[width][height];
		for (int i = 0; i < halite.length; ++i) {
			for (int j = 0; j < halite[0].length; ++j) {
				StringTokenizer tokenizer = new StringTokenizer(reader.readLine());
				Quad quad = new Quad(tokenizer);
				int mapSize = Integer.parseInt(tokenizer.nextToken());
				Map<Vector, Integer> mineMap = new HashMap<Vector, Integer>();
				for (int k = 0 ; k < mapSize; ++k) {
					mineMap.put(new Vector(tokenizer), Integer.parseInt(tokenizer.nextToken()));
				}
				int count = Integer.parseInt(tokenizer.nextToken());
				minePlans[i][j] = new MinePlan(quad, mineMap, count);
			}
		}
		reader.close();
		
		JFrame frame = new JFrame("Visualizer");
		frame.add(new Renderer(halite, minePlans));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setVisible(true);
	}
	static class MinePlan {
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
		public Map<Vector, Integer> getMineMap(){
			return mineMap;
		}
		public int getCount() {
			return count;
		}
	}
}
