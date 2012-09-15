/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import org.ejml.alg.dense.linsol.svd.SolvePseudoInverseSvd;
import org.ejml.alg.dense.mult.MatrixVectorMult;
import org.ejml.data.DenseMatrix64F;

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
public final class DependabilityUnthreaded extends DECAF {
	/**
	 * 
	 */
	private DependabilityUnthreaded() {
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
	public static double calculateMTTF() {
		double[][] qmatrix = QMatrix.toDoubleArray();
		double[][] hvectorArray = new double[1][qmatrix.length];
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
			hvectorArray[0][i] = -qmatrix[i][i];
			qmatrix[i][i] = 1;
		}

		DenseMatrix64F pmatrix = new DenseMatrix64F(qmatrix);
		DenseMatrix64F hvector = new DenseMatrix64F(hvectorArray);
		DenseMatrix64F result = new DenseMatrix64F(new double[qmatrix.length][1]);

		SolvePseudoInverseSvd spSVD = new SolvePseudoInverseSvd(qmatrix.length, qmatrix.length);
		spSVD.setA(pmatrix);
		spSVD.invert(pmatrix);

		MatrixVectorMult.mult(pmatrix, hvector, result);

		return result.get(0);
	}

	public static double calculateSSU() {
		double[][] qmatrix = QMatrix.toDoubleArray();
		double[][] eArray = new double[1][qmatrix.length];
		for (int i = 0; i < qmatrix.length; i++) {
			eArray[0][i] = 1;
		}

		DenseMatrix64F pmatrix = new DenseMatrix64F(qmatrix);
		DenseMatrix64F hvector = new DenseMatrix64F(eArray);
		DenseMatrix64F result = new DenseMatrix64F(new double[qmatrix.length][1]);
		MatrixVectorMult.mult(pmatrix, hvector, result);

		return 0;
	}
}
