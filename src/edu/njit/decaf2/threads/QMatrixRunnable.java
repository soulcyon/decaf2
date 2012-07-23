/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.threads;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.data.State;
import edu.njit.decaf2.generators.QMatrixGenerator;

/**
 * DECAF 2 - QMatrixRunnable<br /><br />
 * 
 * This is just a helper class for multi-threaded calculations of the QMatrix.   This class will implement Runnable.
 * 
 * @author Sashank Tadepalli
 *
 */
public class QMatrixRunnable extends DECAF implements Runnable {
	private State row;
	private int i;
	private int len;
	private QMatrixGenerator qg;
	
	/**
	 * Sets {@link State} {@code row}, {@link Integer} {@code i}, {@link Integer} {@code len} and
	 * {@link QMatrixGenerator} {@code qg}.
	 * 
	 * @param row
	 * @param i
	 * @param len
	 * @param qg
	 */
	public QMatrixRunnable(State row, int i, int len, QMatrixGenerator qg){
		this.qg = qg;
		this.row = row;
		this.i = i;
		this.len = len;
	}
	
	@Override
	public void run(){
		for( int j = i == 0 ? 1 : 0; j < len; j = j == i - 1 ? j + 2 : j + 1 ){
			double fillV = qg.fillQMatrix(row, qg.getTransitionStates()[j]);
			if( Double.isNaN(fillV) ){
				qg.getTodoFill().add(new int[]{i, j});
			} else {
				qg.getqMatrix()[i][j] = fillV;
			}
		}
	}
}
