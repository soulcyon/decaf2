/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import edu.njit.decaf2.DECAF;

/**
 * DECAF - PMatrixGenerator
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public class Dependability extends DECAF {
	/**
	 * 
	 */
	private Dependability() {
		super();
	}

	/**
	 * 
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/**
	 * @return
	 * 
	 */
	public static double calculateMTTF() {
		return 0.0;
	}

	/**
	 * @return
	 */
	public static double calculateSSU() {
		// TODO Auto-generated method stub
		return 0;
	}
}
