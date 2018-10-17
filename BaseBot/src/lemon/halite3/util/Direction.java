package lemon.halite3.util;

public enum Direction {
	NORTH('n', new Vector(0, -1)),
	SOUTH('s', new Vector(0, 1)),
	WEST('w', new Vector(-1, 0)),
	EAST('e', new Vector(1, 0)),
	STILL('o', new Vector(0, 0));
	
	public static final Direction[] CARDINAL_DIRECTIONS = new Direction[] {NORTH, SOUTH, WEST, EAST};
	
	private char charValue;
	private Vector offset;
	private Direction(char charValue, Vector offset) {
		this.charValue = charValue;
		this.offset = offset;
	}
	public Direction invert() {
		switch (this) {
			case NORTH: return SOUTH;
			case SOUTH: return NORTH;
			case WEST: return EAST;
			case EAST: return WEST;
			case STILL: return STILL;
			default: throw new IllegalStateException("Unknown Direction: " + this);
		}
	}
	public char getCharValue() {
		return charValue;
	}
	public Vector getOffsetVector() {
		return offset;
	}
}
