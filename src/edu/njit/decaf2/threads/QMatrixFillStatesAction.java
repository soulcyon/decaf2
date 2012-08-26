/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.threads;

import java.util.concurrent.RecursiveTask;
import edu.njit.decaf2.data.State;
import edu.njit.decaf2.generators.QMatrixGenerator;

/**
 * DECAF 2 - QMatrixFillStatesAction
 * 
 * @author Sashank Tadepalli
 * 
 */
@SuppressWarnings("serial")
public class QMatrixFillStatesAction extends RecursiveTask<double[][]> {
	private State[] ts;
	private double[][] matrix;
	private QMatrixGenerator qg;
	private int i;

	/**
	 * 
	 * @param transitionStates
	 * @param a
	 * @param qMatrixGenerator
	 */
	public QMatrixFillStatesAction(State[] transitionStates, double[][] a, QMatrixGenerator qMatrixGenerator) {
		ts = transitionStates;
		matrix = a;
		qg = qMatrixGenerator;
	}

	/**
	 * @param ts2
	 * @param matrix2
	 * @param qg2
	 * @param i
	 */
	public QMatrixFillStatesAction(State[] transitionStates, double[][] a, QMatrixGenerator qMatrixGenerator, int i) {
		ts = transitionStates;
		matrix = a;
		qg = qMatrixGenerator;
		this.i = i;
	}

	@Override
	protected double[][] compute() {
		if (matrix.length < 5) {
			for (int j = i == 0 ? 1 : 0; j < matrix[0].length; j = j == i - 1 ? j + 2 : j + 1) {
				double fillV = qg.fillQMatrix(ts[i], ts[j]);
				if (Double.isNaN(fillV)) {
					qg.getTodoFill().add(new int[] { i, j });
				} else {
					matrix[0][j] = fillV;
				}
			}
			return matrix;
		}

		for (int i = 0; i < matrix.length; i++) {
			QMatrixFillStatesAction r = new QMatrixFillStatesAction(ts, new double[][] { matrix[i] }, qg, i);
			invokeAll(r);
		}
		return matrix;
	}

}
