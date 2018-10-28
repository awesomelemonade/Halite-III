package lemon.halite3.util;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class GamePlayer {
	private int playerId;
	private Vector shipyardLocation;
	private int halite;
	private Map<Integer, Ship> ships;
	private Map<Integer, Dropoff> dropoffs;
	public GamePlayer(int playerId, Vector shipyardLocation) {
		this.playerId = playerId;
		this.shipyardLocation = shipyardLocation;
		this.ships = new HashMap<Integer, Ship>();
		this.dropoffs = new HashMap<Integer, Dropoff>();
	}
	public void update(int numShips, int numDropoffs, int halite) {
		this.halite = halite;
		ships.clear();
		for (int i = 0; i < numShips; ++i) {
			Ship ship = Ship.parseShip(playerId);
			ships.put(ship.getShipId(), ship);
		}
		dropoffs.clear();
		for (int i = 0; i < numDropoffs; ++i) {
			Dropoff dropoff = Dropoff.parseDropoff(playerId);
			dropoffs.put(dropoff.getDropoffId(), dropoff);
		}
	}
	public int getPlayerId() {
		return playerId;
	}
	public Vector getShipyardLocation() {
		return shipyardLocation;
	}
	public int getHalite() {
		return halite;
	}
	public Map<Integer, Ship> getShips(){
		return ships;
	}
	public Map<Integer, Dropoff> getDropoffs(){
		return dropoffs;
	}
	public static GamePlayer parsePlayer() {
		StringTokenizer tokenizer = new StringTokenizer(Networking.readLine());
		return new GamePlayer(Integer.parseInt(tokenizer.nextToken()), Vector.getInstance(tokenizer));
	}
}
