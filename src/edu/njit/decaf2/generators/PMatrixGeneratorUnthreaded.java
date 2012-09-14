/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.Simulation;
import edu.njit.decaf2.data.QMatrix;

/**
 * DECAF - PMatrixGeneratorUnthreaded
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public final class PMatrixGeneratorUnthreaded extends DECAF {
	/**
	 * 
	 */
	private PMatrixGeneratorUnthreaded() {
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
	 * 
	 */
	public static void generatePMatrix() {
		double[][] qmatrix = QMatrix.toDoubleArray();

		for (int i = 0; i < qmatrix.length; i++) {
			here: for (int j = i == 0 ? 1 : 0; j < qmatrix.length; j = j == i - 1 ? j + 2 : j + 1) {
				for (Integer k : Simulation.states[j].getVector().values()) {
					if (k > 1) {
						qmatrix[i][j] = 0;
						continue here;
					}
				}
				qmatrix[i][j] = (qmatrix[i][j] / qmatrix[i][i]);
			}
		}
		for (int i = 0; i < qmatrix.length; i++) {
			qmatrix[i][i] = 1;
		}

		for (int i = 0; i < qmatrix.length; i++) {
			for (int j = 0; j < qmatrix[i].length; j++) {
				System.out.print((qmatrix[i][j] + "           ").substring(0, 12) + ",");
			}
			System.out.println("");
		}

		System.out.println("\n\nTODO INVERSION HERE!");
	}
}
