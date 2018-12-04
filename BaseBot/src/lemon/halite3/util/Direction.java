package lemon.halite3.util;

public enum Direction {
	NORTH('n', new Vector(0, -1)),
	SOUTH('s', new Vector(0, 1)),
	WEST('w', new Vector(-1, 0)),
	EAST('e', new Vector(1, 0)),
	STILL('o', new Vector(0, 0));
	
	public static final Direction[] CARDINAL_DIRECTIONS = new Direction[] {NORTH, SOUTH, WEST, EAST};
	public static final Direction[][] RANDOM_CARDINAL_DIRECTIONS = new Direction[][] {
		{NORTH, SOUTH, WEST, EAST},
		{NORTH, SOUTH, EAST, WEST},
		{NORTH, WEST, SOUTH, EAST},
		{NORTH, WEST, EAST, SOUTH},
		{NORTH, EAST, WEST, SOUTH},
		{NORTH, EAST, SOUTH, WEST},
		{SOUTH, NORTH, WEST, EAST},
		{SOUTH, NORTH, EAST, WEST},
		{SOUTH, WEST, NORTH, EAST},
		{SOUTH, WEST, EAST, NORTH},
		{SOUTH, EAST, WEST, NORTH},
		{SOUTH, EAST, NORTH, WEST},
		{WEST, NORTH, SOUTH, EAST},
		{WEST, NORTH, EAST, SOUTH},
		{WEST, SOUTH, NORTH, EAST},
		{WEST, SOUTH, EAST, NORTH},
		{WEST, EAST, SOUTH, NORTH},
		{WEST, EAST, NORTH, SOUTH},
		{EAST, WEST, NORTH, SOUTH},
		{EAST, WEST, SOUTH, NORTH},
		{EAST, NORTH, WEST, SOUTH},
		{EAST, NORTH, SOUTH, WEST},
		{EAST, SOUTH, NORTH, WEST},
		{EAST, SOUTH, WEST, NORTH}
	};
	
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
	public static Direction[] getRandomCardinalPermutation() {
		return RANDOM_CARDINAL_DIRECTIONS[(int) (Math.random() * RANDOM_CARDINAL_DIRECTIONS.length)];
	}
}
