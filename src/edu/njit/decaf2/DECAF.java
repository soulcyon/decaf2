/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2;

import java.util.concurrent.ForkJoinPool;

/**
 * DECAF 2 - DECAF
 * 
 * @author Sashank Tadepalli
 * @version 2.0
 * 
 */
public class DECAF {
	public static boolean forceStateDemandValidate = false;
	public static boolean verboseDebug = false;
	public static int debugX = 0;
	public static int debugY = 8;
	public static boolean enableThreading = false;
	public static ForkJoinPool threadPool = new ForkJoinPool();

	/**
	 * Utility function to pretty-print error messages thrown by sub-classes.
	 * 
	 * @param message
	 * @return {@link String}
	 */
	public static String error(String message) {
		return "\n[DECAF" + "::\n" + message + "]\n";
	}
}
