package lemon.halite3.visualizer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JFrame;

public class Visualizer {
	private static int index = 0;
	public static void main(String[] args) throws IOException {
		Renderer renderer = new Renderer();
		String directory = "/home/awesomelemonade/Data/HaliteIII/Halite3_Java_Linux-AMD64/gamestates/";
		
		File[] files = new File(directory).listFiles();
		
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File a, File b) {
				return a.getName().compareTo(b.getName());
			}
		});
		
		for(File file : files) {
			System.out.println(file.getName());
		}
		
		
		setInfo(directory + files[index].getName(), renderer);
		
		JFrame frame = new JFrame("Visualizer");
		
		
		frame.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				
			}
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_A) {
					index = ((index - 1) + files.length) % files.length;
					setInfo(directory + files[index].getName(), renderer);
				}
				if (e.getKeyCode() == KeyEvent.VK_D) {
					index = ((index + 1) + files.length) % files.length;
					setInfo(directory + files[index].getName(), renderer);
				}
				frame.setTitle("Visualizer - " + files[index].getName());
			}
			@Override
			public void keyTyped(KeyEvent e) {
				
			}
		});
		
		frame.add(renderer);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setVisible(true);
	}
	public static void setInfo(String file, Renderer renderer) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			Vector shipLocation = new Vector(new StringTokenizer(reader.readLine()));
			Vector bestPlan = new Vector(new StringTokenizer(reader.readLine()));
			
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
			Vector[][][] paths = new Vector[width][height][];
			int[][] scores = new int[width][height];
			for (int i = 0; i < halite.length; ++i) {
				for (int j = 0; j < halite[0].length; ++j) {
					StringTokenizer tokenizer = new StringTokenizer(reader.readLine());
					Quad quad = new Quad(tokenizer);
					int mapSize = Integer.parseInt(tokenizer.nextToken());
					Map<Vector, Integer> mineMap = new HashMap<Vector, Integer>();
					for (int k = 0 ; k < mapSize; ++k) {
						mineMap.put(new Vector(tokenizer), Integer.parseInt(tokenizer.nextToken()));
					}
					int pathSize = Integer.parseInt(tokenizer.nextToken());
					Vector[] path = new Vector[pathSize];
					for (int k = 0; k < pathSize; ++k) {
						path[k] = new Vector(tokenizer);
					}
					paths[i][j] = path;
					scores[i][j] = Integer.parseInt(tokenizer.nextToken());
					int count = Integer.parseInt(tokenizer.nextToken());
					minePlans[i][j] = new MinePlan(quad, mineMap, count);
				}
			}
			reader.close();
			renderer.setInfo(shipLocation, bestPlan, halite, minePlans, scores, paths);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
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
