package lemon.halite3.util;

import java.util.StringTokenizer;

public class Ship {
	private int playerId;
	private int shipId;
	private Vector location;
	private int halite;
	public Ship(int playerId, int shipId, Vector location, int halite) {
		this.playerId = playerId;
		this.shipId = shipId;
		this.location = location;
		this.halite = halite;
	}
	public int getPlayerId() {
		return playerId;
	}
	public int getShipId() {
		return shipId;
	}
	public Vector getLocation() {
		return location;
	}
	public int getHalite() {
		return halite;
	}
	public static Ship parseShip(int playerId) {
		StringTokenizer tokenizer = new StringTokenizer(Networking.readLine());
		return new Ship(playerId, Integer.parseInt(tokenizer.nextToken()),
				new Vector(tokenizer), Integer.parseInt(tokenizer.nextToken()));
	}
}
