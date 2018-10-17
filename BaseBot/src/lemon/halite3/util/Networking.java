package lemon.halite3.util;

public class Networking {
	public static String readLine() {
		try {
			StringBuilder builder = new StringBuilder();
			int buffer;
			while ((buffer = System.in.read()) >= 0) {
				if (buffer == '\n') {
					break;
				}
				if (buffer == '\r') {
					// Ignore carriage return if on windows for manual testing.
					continue;
				}
				builder.append((char) buffer);
			}
			return builder.toString();
		} catch (Exception ex) {
			DebugLog.log(ex);
			throw new IllegalStateException("Exception reading line!");
		}
	}
	public static void spawnShip() {
		System.out.print("g ");
	}
	public static void transformShipIntoDropoffSite(Ship ship) {
		Networking.transformShipIntoDropoffSite(ship.getShipId());
	}
	public static void transformShipIntoDropoffSite(int id) {
		System.out.print("c " + id + ' ');
	}
	public static void move(Ship ship, Direction direction) {
		Networking.move(ship.getShipId(), direction);
	}
	public static void move(int id, Direction direction) {
		System.out.print("m " + id + ' ' + direction.getCharValue() + ' ');
	}
	public static void endTurn() {
		System.out.println();
	}
}
