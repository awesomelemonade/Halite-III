package lemon.halite3.strategy;

import java.util.Arrays;

public class HungarianAlgorithm {
	private int[][] array;
	public HungarianAlgorithm(int[][] array) {
		this.array = array;
	}
	public void subtractMinimums() {
		int[] rowMinimums = new int[array.length];
		int[] columnMinimums = new int[array.length];
		Arrays.fill(rowMinimums, Integer.MAX_VALUE);
		Arrays.fill(columnMinimums, Integer.MAX_VALUE);
		for (int i = 0; i < array.length; ++i) {
			for (int j = 0; j < array[0].length; ++j) {
				rowMinimums[i] = Math.min(rowMinimums[i], array[i][j]);
				columnMinimums[j] = Math.min(columnMinimums[j], array[i][j]);
			}
		}
		for (int i = 0; i < array.length; ++i) {
			for (int j = 0; j < array[0].length; ++j) {
				array[i][j] = array[i][j] - rowMinimums[i] - columnMinimums[j];
			}
		}
	}
	public void cross() {
		int[] assignmentsByRow = new int[array.length];
		int[] assignmentsByColumn = new int[array[0].length];
		Arrays.fill(assignmentsByRow, -1);
		Arrays.fill(assignmentsByColumn, -1);
		boolean[] markedRows = new boolean[array.length];
		boolean[] markedColumns = new boolean[array[0].length];
		boolean[] newlyMarkedRows = new boolean[array.length];
		boolean[] newlyMarkedColumns = new boolean[array[0].length];
		boolean[] hasZeros = new boolean[array[0].length];
		boolean changed = false;
		for (int i = 0; i < array.length; ++i) {
			for (int j = 0; j < array[0].length; ++j) {
				if (array[i][j] == 0) {
					hasZeros[j] = true;
					break;
				}
			}
			// Assign as many tasks as possible
			int index = -1;
			// Search for a zero that's unassigned
			for (int j = 0; j < array[0].length; ++j) {
				if (array[i][j] == 0 && assignmentsByColumn[j] == -1) { //TODO
					index = j;
					break;
				}
			}
			assignmentsByRow[i] = index;
			assignmentsByColumn[index] = i;
			// Mark all rows having no assignment
			if (index == -1) {
				markedRows[i] = true;
				newlyMarkedRows[i] = true;
				changed = true;
			}
		}
		// https://math.stackexchange.com/questions/590305/finding-the-minimum-number-of-lines-to-cover-all-zeros-in-an-assignment-problem
		// Drawing
		Arrays.fill(newlyMarkedColumns, false);
		for (int j = 0; j < array[0].length; ++j) {
			if (!markedColumns[j] && hasZeros[j]) {
				newlyMarkedColumns[j] = true;
				markedColumns[j] = true;
			}
		}
		Arrays.fill(newlyMarkedRows, false);
		for (int j = 0; j < array[0].length; ++j) {
			if (assignmentsByColumn[j] != -1 && !markedRows[assignmentsByColumn[j]]) {
				newlyMarkedRows[assignmentsByColumn[j]] = true;
				markedRows[assignmentsByColumn[j]] = true;
			}
		}
		
		for (int i = 0; i < array.length; ++i) {
			if (!markedRows[i]) {
				for (int j = 0; j < array[0].length; ++j) {
					newlyMarkedRows[i] |
				}
				newlyMarkedRows[i] = 
			}
		}
	}
}
