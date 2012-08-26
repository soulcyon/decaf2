/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.threads;

import java.util.concurrent.RecursiveTask;

/**
 * DECAF 2 - QMatrixSumDiagonalsAction
 * 
 * @author Sashank Tadepalli
 * 
 */
@SuppressWarnings("serial")
public class QMatrixSumDiagonalsAction extends RecursiveTask<double[][]> {
	private double[][] arr;

	/**
	 * 
	 * @param a
	 */
	public QMatrixSumDiagonalsAction(double[][] a) {
		arr = a;
	}

	@Override
	protected double[][] compute() {
		if (arr.length < 5) {
			for (int i = 0; i < arr.length; i++) {
				double sum = 0;
				for (int j = 0; j < arr.length; j++) {
					sum += arr[i][j];
				}
				arr[i][i] = sum;
			}
			return arr;
		}

		for (int i = 0; i < arr.length; i++) {
			QMatrixSumDiagonalsAction row = new QMatrixSumDiagonalsAction(new double[][] { arr[i] });
			invokeAll(row);
		}
		return arr;
	}

}
