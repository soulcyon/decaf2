/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.data.State;

/**
 * DECAF 2 - QMatrixGenerator
 *
 * @author Sashank Tadepalli
 * @version 2.0
 *
 */
public class QMatrixGenerator extends DECAF {
	private TreeGenerator					tg;
	private State[] 						transitionStates;
	private String[] 						vectorKeys;
	private double[][]						qMatrix;
	private double[][] 						demandMatrix;

	/**
	 * 
	 * @param transitionStates
	 * @param vectorKeys
	 */
	public QMatrixGenerator(State[] transitionStates, String[] vectorKeys, double[][] demandMatrix){
		setTransitionStates(transitionStates);
		setVectorKeys(vectorKeys);
		setDemandLevels(demandMatrix);
	}
	
	/**
	 * 
	 * @return
	 */
	public double[][] generateQMatrix(){
		int statesLen = transitionStates.length;
		//int keysLen = vectorKeys.length;
		
		setqMatrix(new double[statesLen][statesLen]);
		
		// Step 1: Iterate over matrix, ignore diagonal
		for( int i = 0; i < statesLen; i++ ){
			for( int j = i == 0 ? 1 : 0; j < statesLen; j = j == i - 1 ? j + 2 : j + 1 ){
				qMatrix[i][j] = fillQMatrix(transitionStates[i], transitionStates[j]);
			}
		}
		
		// Step 2: Sum each row to fill in diagonal
		for( int i = 0; i < statesLen; i++ ){
			int sum = 0;
			for( int j = i == 0 ? 1 : 0; j < statesLen; j = j == i - 1 ? j + 2 : j + 1 ){
				sum += qMatrix[i][j];
			}
			qMatrix[i][i] = sum;
		}
		return qMatrix;
	}
	
	/**
	 * 
	 * @param from
	 * @param to
	 * @return rate
	 */
	private double fillQMatrix(State from, State to){
		boolean equality = true;
		boolean gentrees = true;
		String repair = null;
		
		if( from.getDemand() != to.getDemand() /*&& from.diff(to).sum() == 0*/ ){
			for( int i = 0; i < vectorKeys.length; i++ )
				if( from.getVector().get(vectorKeys[i]) != to.getVector().get(vectorKeys[i]) )
					return 0.0;
			return demandMatrix[from.getDemand()][to.getDemand()];
		}
		for( int i = 0; i < vectorKeys.length; i++ ){
			int iFrom = from.getVector().get(vectorKeys[i]),
				iTo = to.getVector().get(vectorKeys[i]);

			if( repair == null && equality && iTo == iFrom - 1 ){
				repair = vectorKeys[i];
			} else if( iFrom != iTo ){
				equality = false;
			}
			if( iTo < iFrom ){
				gentrees = false;
			}
		}
		if( gentrees ){
			return tg.getFailureRate(from, to);
		}
		if( equality && repair != null ){
			return (double)from.getVector().get(repair) / (double)from.sum();
		}
		return 0.0;
	}
	
	/**
	 * @return the transitionStates
	 */
	public State[] getTransitionStates() {
		return transitionStates;
	}
	
	/**
	 * @param transitionStates the transitionStates to set
	 */
	public void setTransitionStates(State[] transitionStates) {
		this.transitionStates = transitionStates;
	}
	
	/**
	 * @return the vectorKeys
	 */
	public String[] getVectorKeys() {
		return vectorKeys;
	}
	
	/**
	 * @param vectorKeys the vectorKeys to set
	 */
	public void setVectorKeys(String[] vectorKeys) {
		this.vectorKeys = vectorKeys;
	}
	
	/**
	 * @return the qMatrix
	 */
	public double[][] getqMatrix() {
		return qMatrix;
	}

	/**
	 * @param qMatrix the qMatrix to set
	 */
	public void setqMatrix(double[][] qMatrix) {
		this.qMatrix = qMatrix;
	}

	/**
	 * @return the demandLevels
	 */
	public double[][] getDemandLevels() {
		return demandMatrix;
	}

	/**
	 * @param demandLevels the demandLevels to set
	 */
	public void setDemandLevels(double[][] demandLevels) {
		this.demandMatrix = demandLevels;
	}
	
	/**
	 * 
	 * @param tg
	 */
	public void setTreeGenerator(TreeGenerator tg){
		this.tg = tg;
	}
	
	@Override
	public String toString(){
		int statesLen = transitionStates.length;
		String result = "";
		for( int i = 0; i < statesLen; i++ ){
			for( int j = 0; j < statesLen; j++ ){
				result += "(" + qMatrix[i][j] + ")" + "\t";
			}
			result += "\n";
		}
		return error(result);
	}
}
