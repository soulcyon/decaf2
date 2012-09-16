/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.benchmarks;

import java.util.HashMap;

import edu.njit.decaf2.structures.FailureNode;

/**
 * 
 * DECAF - Test$HashMaps
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public class Test$HashMaps {
	public static void main(String[] args) {
		/****************************************************************************/
		/* FailureNode-Double HashMap Benchmark */
		/****************************************************************************/

		HashMap<FailureNode, double[]> test = new HashMap<FailureNode, double[]>();
		int i = 0;

		// Java heap space issues > 3,500,000 elements
		int max = 1000000;

		double t = System.nanoTime();
		while (i++ < max) {
			double[] temp = new double[] { Math.random() };
			test.put(new FailureNode("A", temp), temp);
		}
		System.out.println("Pushing " + max + " (" + test.size() + ") Components to HashMap.");
		System.out.println((System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0);

		/****************************************************************************/
		/* Double-Double HashMap Benchmark */
		/****************************************************************************/

		HashMap<Double, Double> test2 = new HashMap<Double, Double>();
		i = 0;

		// Java heap space issues > 20,000,000 elements
		max = 500000;

		t = System.nanoTime();
		while (i++ < max) {
			double temp = Math.random();
			test2.put(temp, temp);
		}
		System.out.println("Pushing " + max + " (" + test2.size() + ") Doubles to HashMap.");
		System.out.println((System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0);
	}
}