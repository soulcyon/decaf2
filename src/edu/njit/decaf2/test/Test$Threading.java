/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.test;

import java.util.ArrayList;
import java.util.List;

/**
 * DECAF 2 - Test$Threading
 * 
 * @author Sashank Tadepalli
 *
 */
public class Test$Threading {
	public static void main(String[] args) {
		double t = System.nanoTime();
		/****************************************************************************/
		/* Threaded Arithmetic */
		/****************************************************************************/
		for (int i = 0; i < 500; i++) {
			Runnable task = new Test$Runnable(10000000L + i);
			Thread worker = new Thread(task);
			worker.setName(i + "");
			worker.start();
		}
		System.out.println("Threaded: " + (System.nanoTime() - t)/1000.0/1000.0/1000.0);

		/****************************************************************************/
		/* UnThreaded Arithmetic */
		/****************************************************************************/
		t = System.nanoTime();
		for (int i = 0; i < 500; i++) {
			Runnable task = new Test$Runnable(10000000L + i);
			task.run();
		}
		System.out.println("Unthreaded: " + (System.nanoTime() - t)/1000.0/1000.0/1000.0);
	}
}