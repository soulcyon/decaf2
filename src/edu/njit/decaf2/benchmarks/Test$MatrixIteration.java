/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.benchmarks;

/**
 * DECAF - Test$MatrixIteration
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public class Test$MatrixIteration {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int l = 4500;
		double[][] matrix = new double[l][l];
		double t = 0;
		/****************************************************************************/
		/* No continue Bench */
		/****************************************************************************/
		t = System.nanoTime();
		for (int i = 0; i < l; i++) {
			for (int j = i == 0 ? 1 : 0; j < l; j = j == i - 1 ? j + 2 : j + 1) {
				matrix[i][j] = 1.0;
			}
		}
		System.out.println("NoContinue:   " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0);
		
		/****************************************************************************/
		/* Continue Bench */
		/****************************************************************************/
		t = System.nanoTime();
		for (int i = 0; i < l; i++) {
			for (int j = 0; j < l; j++) {
				if( i == j )
					continue;
				matrix[i][j] = 1.0;
			}
		}
		System.out.println("Continue:     " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0);
	}
}
