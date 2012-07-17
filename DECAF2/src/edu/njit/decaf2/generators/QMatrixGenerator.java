package edu.njit.decaf2.generators;
import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.data.State;


/**
 * 
 * @author Sashank Tadepalli
 *
 */
public class QMatrixGenerator extends DECAF {
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
	public double[][] generateTransitionMatrix(){
		int statesLen = transitionStates.length;
		//int keysLen = vectorKeys.length;
		
		setqMatrix(new double[statesLen][statesLen]);
		
		// Step 1: Iterate over matrix, ignore diagonal
		for( int i = 0; i < statesLen; i++ ){
			for( int j = i == 0 ? 1 : 0; j < statesLen; j = j == i - 1 ? j + 2 : j + 1 ){
				State currentState = transitionStates[i].diff(transitionStates[j]);
				int sum = currentState.sum();
				
				switch( sum ){
					// In case of negatives, fill in proper Repair probability
					case -1: 
						qMatrix[i][j] = 1.0;
					break;
					default:
						qMatrix[i][j] = 0;
				}
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
}
