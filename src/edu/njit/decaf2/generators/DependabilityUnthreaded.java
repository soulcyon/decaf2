/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Map.Entry;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.Simulation;

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
		int statesLen = Simulation.qmatrix.columns();
		DoubleMatrix2D pmatrix = Simulation.qmatrix.copy();
		
		DenseDoubleMatrix1D hvector = new DenseDoubleMatrix1D(statesLen);
		
		for (int i = 0; i < statesLen; i++) {
			here: for (int j = 0; j < statesLen; j++) {
				if (j == i) {
					continue;
				}
				for (Entry<String, Integer> entry : Simulation.states[j].getVector().entrySet()) {
					if (entry.getValue() > Simulation.nodeMap.get(entry.getKey()).getRequired()) {
						pmatrix.setQuick(i, j, 0);

						// Custom QMatrix Deprecation in progress
						//qmatrix[i][j] = 0;
						continue here;
					}
				}
				pmatrix.setQuick(i, j, pmatrix.getQuick(i, j) / pmatrix.getQuick(i, i));
				
				// Custom QMatrix Deprecation in progress
				//qmatrix[i][j] = (qmatrix[i][j] / qmatrix[i][i]);
			}
			hvector.setQuick(i, -pmatrix.getQuick(i, i));
			pmatrix.setQuick(i, i, 1);
			
			// Custom QMatrix Deprecation in progress
			//hvectorArray[i] = -qmatrix[i][i];
			//qmatrix[i][i] = 1;
		}

		if (DECAF.sriniOutput) {
			try {
				FileWriter fstream = new FileWriter("matrix." + statesLen + ".txt");
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(pmatrix.toString());
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		DenseDoubleMatrix1D result = new DenseDoubleMatrix1D(new double[statesLen]);
		DenseDoubleAlgebra sa = new DenseDoubleAlgebra();
		pmatrix = sa.inverse(pmatrix);
		result = (DenseDoubleMatrix1D) pmatrix.zMult(hvector, result);
		
		return result.get(0);
	}

	public static double calculateSSU() {
		return 0.0;
	}
}
