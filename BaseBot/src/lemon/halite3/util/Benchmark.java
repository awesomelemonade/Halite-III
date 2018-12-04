package lemon.halite3.util;

import java.util.ArrayDeque;
import java.util.Deque;

public class Benchmark implements AutoCloseable {
	private Deque<Long> deque;
	private String closeString;

	public Benchmark() {
		this(null);
	}

	public Benchmark(String closeString) {
		this.deque = new ArrayDeque<Long>();
		this.push();
		this.closeString = closeString;
	}

	public void push() {
		deque.push(System.nanoTime());
	}
	
	public long peek() {
		return System.nanoTime() - deque.peek();
	}
	
	public void peek(String formatString) {
		printFormattedString(formatString, System.nanoTime() - deque.peek());
	}
	
	public long pop () {
		return System.nanoTime() - deque.pop();
	}

	public void pop(String formatString) {
		printFormattedString(formatString, System.nanoTime() - deque.pop());
	}

	@Override
	public void close() {
		if (closeString != null) {
			printFormattedString(closeString, System.nanoTime() - deque.pop());
		}
	}

	private void printFormattedString(String formatString, long time) {
		DebugLog.log(String.format(formatString, Double.toString(((double) time) / 1000000000.0)));
	}
}
