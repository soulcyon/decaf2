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
	protected static final boolean sriniOutput = false;
	protected static final boolean forceStateDemandValidate = true;
	protected static final boolean verboseDebug = false;
	protected static final int debugX = 0;
	protected static final int debugY = 8;
	protected static final boolean enableThreading = false;
	public static final int threadCount = 4;
	public static final ForkJoinPool threadPool = new ForkJoinPool(256);
	public static final int heightThreshold = 100;
	public static final double rateThreshold = 0;
	public static final int nodeThreshold = 100; 
	public static final String biasType = "high";

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
