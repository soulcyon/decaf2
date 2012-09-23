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

import org.ojalgo.machine.Hardware;
import org.ojalgo.machine.VirtualMachine;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.MatrixBuilder;
import org.ojalgo.matrix.MatrixFactory;
import org.ojalgo.matrix.PrimitiveMatrix;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
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
	private static Set<Integer> systemDownStates = new HashSet<Integer>();

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
						systemDownStates.add(j);
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

		return result.get(0);
	}

	public static double calculateSSU() {
		double result = 0.0;
		DoubleMatrix2D dd = Simulation.qmatrix.copy();
		// DenseDoubleAlgebra da = new DenseDoubleAlgebra();
		for (int i = 0; i < dd.columns(); i++) {
			dd.setQuick(i, 0, 1);
		}

		/* Comment the block below and uncomment other lines for P-Colt SSU */
		String tmpArchitecture = VirtualMachine.getArchitecture();
		long tmpMemory = VirtualMachine.getMemory();
		int tmpThreads = VirtualMachine.getThreads();
		
		org.ojalgo.OjAlgoUtils.ENVIRONMENT = Hardware.makeSimple(tmpArchitecture, tmpMemory, tmpThreads).virtualise();
		
		MatrixFactory<?> tmpFactory = PrimitiveMatrix.FACTORY;
		MatrixBuilder<?> tmpBuilder = tmpFactory.getBuilder(dd.columns(), dd.columns());
		for (int j = 0; j < tmpBuilder.getColDim(); j++) {
			for (int i = 0; i < tmpBuilder.getRowDim(); i++) {
				tmpBuilder.set(i, j, dd.getQuick(i, j));
			}
		}
		BasicMatrix tmpI = tmpBuilder.build();
		tmpI = tmpI.invert();
		/* block end */

		//dd = da.inverse(dd);
		for (Integer k : systemDownStates) {
			result += tmpI.doubleValue(0, k);
		}
		return result;
	}
}
