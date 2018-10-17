package lemon.halite3.util;

import java.util.StringTokenizer;

public class GameMap {
	private int numPlayers;
	private int playerId;
	private GamePlayer[] players;
	private int currentTurn;
	private int[][] halite;
	private Ship[][] ships;
	private int[][] lastUpdated; // Tracked for ships
	private Dropoff[][] dropoffs;
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
		int mapWidth = Integer.parseInt(mapSizeTokenizer.nextToken());
		int mapHeight = Integer.parseInt(mapSizeTokenizer.nextToken());
		this.halite = new int[mapWidth][mapHeight];
		this.ships = new Ship[mapWidth][mapHeight];
		this.lastUpdated = new int[mapWidth][mapHeight];
		this.currentTurn = -1;
		for (int i = 0; i < mapWidth; ++i) {
			StringTokenizer tokenizer = new StringTokenizer(Networking.readLine());
			for (int j = 0; j < mapHeight; ++j) {
				this.halite[i][j] = Integer.parseInt(tokenizer.nextToken());
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
			halite[Integer.parseInt(tokenizer.nextToken())][Integer.parseInt(tokenizer.nextToken())] = Integer.parseInt(tokenizer.nextToken());
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
	public void updateShip(Ship ship) {
		ships[ship.getLocation().getX()][ship.getLocation().getY()] = ship;
		lastUpdated[ship.getLocation().getX()][ship.getLocation().getY()] = currentTurn;
	}
	public Ship getShip(int x, int y) {
		return lastUpdated[x][y] == currentTurn ? ships[x][y] : null;
	}
	public Ship getShip(Vector location) {
		return this.getShip(location.getX(), location.getY());
	}
	public GamePlayer getMyPlayer() {
		return players[playerId];
	}
	public int getMyPlayerId() {
		return playerId;
	}
}
