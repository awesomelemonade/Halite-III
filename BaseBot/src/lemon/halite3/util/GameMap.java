package lemon.halite3.util;

import java.util.StringTokenizer;

public class GameMap {
	private int numPlayers;
	private int playerId;
	private int width;
	private int height;
	private GamePlayer[] players;
	private int currentTurn;
	private int[][] halite;
	private Ship[][] ships;
	private int[][] lastUpdated; // Tracked for ships
	private Dropoff[][] dropoffs;
	private int totalHalite;
	public void parseStart() {
		StringTokenizer infoTokenizer = new StringTokenizer(Networking.readLine());
		numPlayers = Integer.parseInt(infoTokenizer.nextToken());
		playerId = Integer.parseInt(infoTokenizer.nextToken());
		players = new GamePlayer[numPlayers];
		for (int i = 0; i < numPlayers; ++i) {
			GamePlayer player = GamePlayer.parsePlayer();
			players[player.getPlayerId()] = player;
		}
		StringTokenizer mapSizeTokenizer = new StringTokenizer(Networking.readLine());
		width = Integer.parseInt(mapSizeTokenizer.nextToken());
		height = Integer.parseInt(mapSizeTokenizer.nextToken());
		this.halite = new int[width][height];
		this.dropoffs = new Dropoff[width][height];
		this.ships = new Ship[width][height];
		this.lastUpdated = new int[width][height];
		this.currentTurn = -1;
		this.totalHalite = 0;
		for (int i = 0; i < width; ++i) {
			StringTokenizer tokenizer = new StringTokenizer(Networking.readLine());
			for (int j = 0; j < height; ++j) {
				this.halite[j][i] = Integer.parseInt(tokenizer.nextToken());
				this.totalHalite += this.halite[j][i];
			}
		}
	}
	public void ready(String name) {
		System.out.println(name);
	}
	public void update() {
		currentTurn = Integer.parseInt(Networking.readLine());
		for (int i = 0; i < numPlayers; ++i) {
			StringTokenizer tokenizer = new StringTokenizer(Networking.readLine());
			players[Integer.parseInt(tokenizer.nextToken())].update(Integer.parseInt(tokenizer.nextToken()),
					Integer.parseInt(tokenizer.nextToken()), Integer.parseInt(tokenizer.nextToken()));
		}
		int updateCount = Integer.parseInt(Networking.readLine());
		for (int i = 0; i < updateCount; ++i) {
			StringTokenizer tokenizer = new StringTokenizer(Networking.readLine());
			int x = Integer.parseInt(tokenizer.nextToken());
			int y = Integer.parseInt(tokenizer.nextToken());
			int newHalite = Integer.parseInt(tokenizer.nextToken());
			this.totalHalite += (newHalite - halite[x][y]);
			halite[x][y] = newHalite;
		}
		for (GamePlayer player : players) {
			for (Ship ship : player.getShips().values()) {
				this.updateShip(ship);
			}
			for (Dropoff dropoff : player.getDropoffs().values()) {
				dropoffs[dropoff.getLocation().getX()][dropoff.getLocation().getY()] = dropoff;
			}
		}
	}
	public int getHalite(Vector location) {
		return this.getHalite(location.getX(), location.getY());
	}
	public int getHalite(int x, int y) {
		return halite[x][y];
	}
	public void updateShip(Ship ship) {
		ships[ship.getLocation().getX()][ship.getLocation().getY()] = ship;
		lastUpdated[ship.getLocation().getX()][ship.getLocation().getY()] = currentTurn;
	}
	public Ship getShip(Vector location) {
		return this.getShip(location.getX(), location.getY());
	}
	public Ship getShip(int x, int y) {
		return lastUpdated[x][y] == currentTurn ? ships[x][y] : null;
	}
	public GamePlayer[] getPlayers() {
		return players;
	}
	public GamePlayer getMyPlayer() {
		return players[playerId];
	}
	public int getMyPlayerId() {
		return playerId;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public int getCurrentTurn() {
		return currentTurn;
	}
	public int getTotalHalite() {
		return totalHalite;
	}
}
