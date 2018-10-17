package lemon.halite3.util;

import java.util.StringTokenizer;

public class Dropoff {
	private int playerId;
	private int dropoffId;
	private Vector location;
	public Dropoff(int playerId, int dropoffId, Vector location) {
		this.playerId = playerId;
		this.dropoffId = dropoffId;
		this.location = location;
	}
	public int getPlayerId() {
		return playerId;
	}
	public int getDropoffId() {
		return dropoffId;
	}
	public Vector getLocation() {
		return location;
	}
	public static Dropoff parseDropoff(int playerId) {
		StringTokenizer tokenizer = new StringTokenizer(Networking.readLine());
		return new Dropoff(playerId, Integer.parseInt(tokenizer.nextToken()), new Vector(tokenizer));
	}
}
