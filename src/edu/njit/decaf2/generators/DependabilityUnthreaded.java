/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Map.Entry;

import org.ejml.data.DenseMatrix64F;
import org.ejml.data.Eigenpair;
import org.ejml.ops.EigenOps;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
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
		
		DoubleMatrix1D hvector = new DenseDoubleMatrix1D(statesLen);
		
		for (int i = 0; i < statesLen; i++) {
			here: for (int j = 0; j < statesLen; j++) {
				if (j == i) {
					continue;
				}
				for (Entry<String, Integer> entry : Simulation.states[j].getVector().entrySet()) {
					if (entry.getValue() > Simulation.nodeMap.get(entry.getKey()).getRequired()) {
						pmatrix.setQuick(i, j, 0);
						continue here;
					}
				}
				pmatrix.setQuick(i, j, pmatrix.getQuick(i, j) / pmatrix.getQuick(i, i));
			}
			hvector.setQuick(i, -pmatrix.getQuick(i, i));
			pmatrix.setQuick(i, i, 1);
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
		DoubleMatrix1D result = new DenseDoubleMatrix1D(new double[statesLen]);
		DenseDoubleAlgebra sa = new DenseDoubleAlgebra();
		pmatrix = sa.inverse(pmatrix);
		result = pmatrix.zMult(hvector, result);

		
		DenseMatrix64F pimatrix = new DenseMatrix64F(sa.transpose(pmatrix).toArray());
		
		double[] ep = EigenOps.computeEigenVector(pimatrix, 0).vector.data;
		
		DoubleMatrix2D result1 = new DenseDoubleMatrix2D(1, ep.length);
		result1.assign(ep);
		result1 = sa.transpose(result1);
		System.out.println(result);
		
		return result.get(0);
	}

	public static double calculateSSU() {
		DenseDoubleAlgebra da = new DenseDoubleAlgebra();
		DenseMatrix64F pimatrix = new DenseMatrix64F(da.transpose(Simulation.qmatrix).toArray());
		
		double[] ep = EigenOps.computeEigenVector(pimatrix, 0).vector.data;
		
		DoubleMatrix2D result = new DenseDoubleMatrix2D(1, ep.length);
		result.assign(ep);
		result = da.transpose(result);
		System.out.println(result);
		return result.get(0, 0);
	}
}