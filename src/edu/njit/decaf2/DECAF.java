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
	public static final boolean forceStateDemandValidate = true;
	public static final boolean verboseDebug = false;
	public static final int debugX = 0;
	public static final int debugY = 8;
	public static final boolean enableThreading = false;
	public static final int threadCount = 4;
	public static final ForkJoinPool threadPool = new ForkJoinPool(256);
	public static final int heightThreshold = 100;
	public static final double rateThreshold = 0;
	public static final int nodeThreshold = 100; 
	public static final String biasType = "high";
	public static final boolean completeTreeRate = true;

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
