/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.threads;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.data.State;
import edu.njit.decaf2.generators.QMatrixGenerator;

/**
 * DECAF 2 - QMatrixRunnable
 * 
 * @author Sashank Tadepalli
 *
 */
public class QMatrixRunnable extends DECAF implements Runnable {
	int i, j;
	
	public QMatrixRunnable(int i, int j){
		this.i = i;
		this.j = j;
	}
	
	public void run(double[][] qMatrix, State[] transitionStates, QMatrixGenerator qg){
		qMatrix[i][j] = qg.fillQMatrix(transitionStates[i], transitionStates[j]);
	}
	
	@Override
	public void run(){
	}
}
