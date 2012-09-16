/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.structures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.njit.decaf2.Simulation;

/**
 * DECAF - QMatrix
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public final class QMatrix {
	private static Map<Point, Double> matrix = new HashMap<Point, Double>();

	/**
	 * 
	 */
	private QMatrix() {
		super();
	}

	/**
	 * 
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public static void put(final int xIndex, final int yIndex, final double value) {
		if (value == 0.0) {
			return;
		}
		matrix.put(new Point(xIndex, yIndex), value);
	}

	public static void update(final int xIndex, final int yIndex, final double value) {
		if (value == 0.0) {
			return;
		}

		final Point point = new Point(xIndex, yIndex);
		if (matrix.containsKey(point)) {
			matrix.put(point, (matrix.get(point) + 0.0) + value);
		} else {
			put(xIndex, yIndex, value);
		}
	}

	public static double get(final int xIndex, final int yIndex) {
		if (!matrix.containsKey(new Point(xIndex, yIndex))) {
			return 0.0;
		}
		return matrix.get(new Point(xIndex, yIndex));
	}

	public static String generateCCM() {
		final StringBuffer result = new StringBuffer();
		final List<Point> pointList = new ArrayList<Point>(matrix.keySet());

		// Sorted by from-transition, then to-transition
		Collections.sort(pointList);

		for (Point p : pointList) {
			result.append(matrix.get(p));
			result.append('@');
			result.append(p);
			result.append("; ");
		}
		return result.toString();
	}

	/**
	 * 
	 * @return
	 */
	public static double[][] toDoubleArray() {
		double[][] result = new double[Simulation.states.length][Simulation.states.length];
		for (Entry<Point, Double> entry : matrix.entrySet()) {
			result[entry.getKey().getX()][entry.getKey().getY()] = entry.getValue();
		}
		return result;
	}

	/**
	 * @param qmatrix
	 * @return
	 */
	public static String matrixToString(double[][] qmatrix) {
		StringBuffer result = new StringBuffer();
		for( int i = 0; i < qmatrix.length; i++ ){
			StringBuffer temp = new StringBuffer();
			for( int j = 0; j < qmatrix.length; j++ ){
				temp.append(qmatrix[i][j]);
				temp.append(',');
			}
			result.append(temp);
			result.append('\n');
		}
		return result.toString();
	}
}
