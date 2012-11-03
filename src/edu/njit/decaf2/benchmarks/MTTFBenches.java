package edu.njit.decaf2.benchmarks;

import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.solver.DoubleGMRES;
import cern.colt.matrix.tdouble.algo.solver.IterativeSolverDoubleNotConvergedException;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.Simulation;

public class MTTFBenches {
	private static double start = 0.1;
	private static double end = 0.55;
	private static double increment = 0.01;
	private static int length = (int) ((end - start)/increment) + 1;
	private static double[] times = new double[length];
	private static double[] mttfs = new double[length];

	public static void main(String[] args) {
		DECAF.biasType = "high";
		DECAF.completeTreeRate = false;
		DECAF.heightThreshold = 100;
		DECAF.rateThreshold = 0;
		DECAF.treeThreshold = 1000000;
		Simulation sim = new Simulation();
		sim.run("data/input.xml");
		double mttf = Simulation.meanTimeToFailure;
		double time = Simulation.mttfCalculationTime;

		/* Calculate PMatrix */
		Set<Integer> systemDownStates = new HashSet<Integer>();
		DoubleMatrix2D pmatrix = Simulation.qmatrix.copy();
		DoubleMatrix1D hvector = new DenseDoubleMatrix1D(Simulation.states.length);

		for (int i = 0; i < Simulation.states.length; i++) {
			here: for (int j = 0; j < Simulation.states.length; j++) {
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
		int o = 0;
		for ( double i = start; i < end; i += increment, o++ ) {
			long t = System.nanoTime();
			int l = (int) (Simulation.states.length * i);
			DoubleMatrix1D result = new DenseDoubleMatrix1D(l);
			DoubleGMRES solve = new DoubleGMRES(hvector.viewPart(0, l));
			try {
				solve.solve(pmatrix.viewPart(0, 0, l, l), hvector.viewPart(0, l), result);
			} catch (IterativeSolverDoubleNotConvergedException e) {
				e.printStackTrace();
			}
			times[o] = (System.nanoTime() - t) / 10e9f;
			mttfs[o] = result.get(0);
			System.out.println(l + "\t" + times[o] + "\t" + mttfs[o]);
		}
	}
}
