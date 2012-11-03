/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2;

import java.util.concurrent.ForkJoinPool;

/**
 * 
 * DECAF
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public class DECAF {
	public static final boolean sriniOutput = false;
	public static final boolean verboseDebug = false;
	public static final int debugX = 0;
	public static final int debugY = 8;
	public static final boolean enableThreading = false;
	public static final int threadCount = 4;
	public static ForkJoinPool threadPool = new ForkJoinPool(256);
	public static int heightThreshold = 1000000;
	public static double rateThreshold = 0;
	public static int nodeThreshold = 10000000;
	public static int treeThreshold = 10000000;
	public static String biasType = "high";
	public static boolean completeTreeRate = true;

	/**
	 * Utility function to pretty-print error messages thrown by sub-classes.
	 * 
	 * @param message
	 * @return {@link String}
	 */
	public static String error(final String message) {
		return "\n[DECAF" + "::\n" + message + "]\n";
	}

	/**
	 * 
	 */
	protected DECAF() {
		super();
	}

	/**
	 * 
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
