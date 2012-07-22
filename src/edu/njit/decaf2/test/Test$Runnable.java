/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.test;

/**
 * DECAF 2 - Test$Runnable
 * 
 * @author Sashank Tadepalli
 *
 */
public class Test$Runnable implements Runnable {
	private final long countUntil;

	Test$Runnable(long countUntil) {
		this.countUntil = countUntil;
	}

	@Override
	public void run() {
		long sum = 0;
		for (long i = 1; i < countUntil; i++) {
			sum += i;
		}
	}
}
