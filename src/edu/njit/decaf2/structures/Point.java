/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.structures;

import edu.njit.decaf2.DECAF;

/**
 * DECAF - Point
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public class Point extends DECAF implements Comparable<Point> {
	private int xIndex;
	private int yIndex;

	/**
	 * 
	 * @param x
	 * @param y
	 */
	public Point(final int xIndex, final int yIndex) {
		super();
		this.xIndex = xIndex;
		this.yIndex = yIndex;
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return xIndex;
	}

	/**
	 * @param x
	 *            the x to set
	 */
	public void setX(final int xIndex) {
		this.xIndex = xIndex;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return yIndex;
	}

	/**
	 * @param y
	 *            the y to set
	 */
	public void setY(final int yIndex) {
		this.yIndex = yIndex;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public int hashCode() {
		return Integer.valueOf(Integer.valueOf(xIndex).hashCode() + Integer.valueOf(yIndex).hashCode()).hashCode();
	}

	/*
	 * 
	 */
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof Point)) {
			return false;
		}

		final Point test = (Point) obj;
		return xIndex == test.xIndex && yIndex == test.yIndex;
	}

	/**
	 * 
	 */
	@Override
	public int compareTo(final Point obj) {
		return xIndex == obj.xIndex ? yIndex - obj.yIndex : xIndex - obj.xIndex;
	}

	@Override
	public String toString() {
		return "(" + xIndex + "," + yIndex + ")";
	}
}
