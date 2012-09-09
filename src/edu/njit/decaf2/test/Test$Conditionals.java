/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.test;

/**
 * 
 * DECAF - Test$Conditionals
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public class Test$Conditionals {
	public static void main(String[] args) {
		int comp1 = 1;
		int comp2 = 2;
		int max = 100000;
		double t = System.nanoTime();

		/****************************************************************************/
		/* Equal-Equal Bench */
		/****************************************************************************/
		comp1 = 1;
		comp2 = 2;
		while (max-- > 0) {
			if (comp1 == comp2)
				max--;
		}

		System.out.println("Bench1: " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0);
		t = System.nanoTime();
		max = 100000;
		/****************************************************************************/
		/* Equal-Inequal Bench */
		/****************************************************************************/
		comp1 = 1;
		comp2 = 1;
		while (max-- > 0) {
			if (comp1 != comp2)
				max--;
		}

		System.out.println("Bench2: " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0);
		t = System.nanoTime();
		max = 100000;
		/****************************************************************************/
		/* Math-Inequal Bench */
		/****************************************************************************/
		comp1 = 1;
		comp2 = 2;
		while (max-- > 0) {
			if (comp1 - comp2 == 0)
				max--;
		}

		System.out.println("Bench3: " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0);
		t = System.nanoTime();
		max = 100000;
		/****************************************************************************/
		/* Math-Inequal Bench */
		/****************************************************************************/
		comp1 = 1;
		comp2 = 1;
		while (max-- > 0) {
			if (comp1 - comp2 != 0)
				max--;
		}

		System.out.println("Bench4: " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0);
	}
}
