/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.algo.decomposition.DenseDoubleEigenvalueDecomposition;
import cern.colt.matrix.tdouble.algo.decomposition.DenseDoubleLUDecomposition;
import cern.colt.matrix.tdouble.algo.solver.DoubleBiCG;
import cern.colt.matrix.tdouble.algo.solver.DoubleCGS;
import cern.colt.matrix.tdouble.algo.solver.DoubleGMRES;
import cern.colt.matrix.tdouble.algo.solver.IterativeSolverDoubleNotConvergedException;
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
	public static Set<Integer> systemDownStates = new HashSet<Integer>();

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
					if (Simulation.nodeMap.get(entry.getKey()).getRedundancy() - entry.getValue() < 
							Simulation.nodeMap.get(entry.getKey()).getRequired()) {
						pmatrix.setQuick(i, j, 0);
						systemDownStates.add(j);
						continue here;
					}
				}
				pmatrix.setQuick(i, j, pmatrix.getQuick(i, j) / pmatrix.getQuick(i, i));
			}
			hvector.setQuick(i, -pmatrix.getQuick(i, i));
			pmatrix.setQuick(i, i, 1);
		}

		/*if (DECAF.sriniOutput) {
			try {
				FileWriter fstream = new FileWriter("matrix." + statesLen + ".txt");
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(pmatrix.toString());
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}*/

		DoubleMatrix1D result = new DenseDoubleMatrix1D(statesLen);
		DenseDoubleAlgebra sa = new DenseDoubleAlgebra();
		pmatrix = sa.inverse(pmatrix);
		/*DoubleGMRES t = new DoubleGMRES(hvector);
		try {
			t.solve(pmatrix, hvector, result);
		} catch (IterativeSolverDoubleNotConvergedException e) {
			e.printStackTrace();
		}*/
		result = pmatrix.zMult(hvector, result);

		return result.get(0);
	}
	
	/**
	 * 
	 * @return
	 */
	public static double calculateSSU() {
		if( systemDownStates.size() < 1 ){
			System.out.println(DECAF.error("SSU might be innacurate.  Please compute MTTF before SSU."));
		}
		
		int statesLen = Simulation.qmatrix.columns();
		double result = 0.0;
		DoubleMatrix2D dd = Simulation.qmatrix.copy();
		
		for (int i = 0; i < statesLen; i++) {
			dd.setQuick(i, 0, 1);
		}
		DenseDoubleMatrix1D temp = new DenseDoubleMatrix1D(statesLen);
		DenseDoubleMatrix1D e = new DenseDoubleMatrix1D(statesLen);
		for(int i = 0; i < statesLen; i++ ){
			e.setQuick(i, 1);
		}
		DoubleGMRES t = new DoubleGMRES(temp);
		try {
			System.out.println(t.solve(dd, e, temp));
			double r = 0;
			for( int i = 0; i < statesLen; i++ ){
				r += temp.getQuick(i);
			}
			System.out.println(r/statesLen);
		} catch (IterativeSolverDoubleNotConvergedException err) {
			err.printStackTrace();
		}

		dd = new DenseDoubleAlgebra().inverse(dd);
		
		for (Integer k : systemDownStates) {
			result += dd.getQuick(0, k);
		}
		return result;
	}
}
