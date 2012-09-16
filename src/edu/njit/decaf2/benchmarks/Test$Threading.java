/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.benchmarks;

import java.util.concurrent.RecursiveTask;

import edu.njit.decaf2.DECAF;

/**
 * DECAF - Test$Threading
 * 
 * Findings: Minimal 1,000,000 computations required before Threads become
 * relevant. Threading is useless when trying to calculate combinations.
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public class Test$Threading extends DECAF {
	public static void main(String[] args) {
		double t = System.nanoTime();
		/****************************************************************************/
		/* Threaded Arithmetic */
		/****************************************************************************/
		for (int i = 0; i < 500; i++) {
			Runnable task = new Test$Runnable(1_000_000 + i);
			Thread worker = new Thread(task);
			worker.setName(i + "");
			worker.start();
		}
		System.out.println("Threaded: " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0);

		/****************************************************************************/
		/* UnThreaded Arithmetic */
		/****************************************************************************/
		t = System.nanoTime();
		for (int i = 0; i < 500; i++) {
			Runnable task = new Test$Runnable(1_000_000 + i);
			task.run();
		}
		System.out.println("Unthreaded: " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0);

		/****************************************************************************/
		/* Threaded Recursion */
		/****************************************************************************/
		t = System.nanoTime();
		RecursiveTask<String[]> task = new Test$CombinationTask("", "abcdefghijk");
		threadPool.invoke(task);
		System.out.println("Threaded: " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0);

		/****************************************************************************/
		/* Unthreaded Recursion */
		/****************************************************************************/
		t = System.nanoTime();
		comb2("abcdefghijk");
		System.out.println("Unthreaded: " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0);
	}

	public static void comb2(String s) {
		comb2("", s);
	}

	private static void comb2(String prefix, String s) {
		for (int i = 0; i < s.length(); i++)
			comb2(prefix + s.charAt(i), s.substring(i + 1));
	}
}