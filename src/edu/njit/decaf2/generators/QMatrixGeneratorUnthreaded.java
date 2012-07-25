/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.util.ArrayList;
import java.util.Iterator;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.data.State;

/**
 * DECAF 2 - QMatrixGenerator
 *
 * @author Sashank Tadepalli
 * @version 2.0
 *
 */
public class QMatrixGeneratorUnthreaded extends DECAF {
	private TreeGenerator					tg;
	private ArrayList<Thread>				threads = new ArrayList<Thread>();
	private ArrayList<int[]>				todoFill = new ArrayList<int[]>();

	private State[] 						transitionStates;
	private String[] 						vectorKeys;
	private double[][]						qMatrix;
	private double[][] 						demandMatrix;

	/**
	 * Sets {@link State} {@code transitionStates}, {@link String}[] {@code vectorKeys}, {@link Double}[][] 
	 * {@code demandMatrix}
	 * 
	 * @param transitionStates
	 * @param vectorKeys
	 */
	public QMatrixGeneratorUnthreaded(State[] ts, String[] vk, double[][] dm, TreeGenerator t){
		transitionStates = ts;
		vectorKeys = vk;
		demandMatrix = dm;
		tg = t;
	}
	
	/**
	 * Algorithm 2.0 - Generating QMatrix via {@link QMatrixRunnable}
	 * Refer to annotated source for details
	 * 
	 * @return qMatrix
	 */
	public double[][] generateQMatrix(){
		// Cache the length of valid transition states
		int statesLen = transitionStates.length;
		
		// Initialize brand new qMatrix[][] array
		this.qMatrix = new double[statesLen][statesLen];

		// Iterate over matrix, ignore diagonal
		// This takes advantage of multi-threading to speed up calculations.  We could multi-thread the calculation of
		// every cell, however to lower overhead and increase performance, we will only multi-thread the row 
		// calculations.  See QMatrixRunnable for implementation details.
		for( int i = 0; i < statesLen; i++ ){
			for( int j = i == 0 ? 1 : 0; j < statesLen; j = j == i - 1 ? j + 2 : j + 1 ){
				double fillV = fillQMatrix(transitionStates[i], transitionStates[j]);
				if( Double.isNaN(fillV) ){
					todoFill.add(new int[]{i, j});
				} else {
					qMatrix[i][j] = fillV;
				}
			}
		}
		
		// Make sure all threads are complete before proceeding
		int running = 0;
		do {
			running = 0;
			for (Thread thread : threads)
				if (thread.isAlive())
					running++;
		} while (running > 0);
		
		// Generate trees as required
		Iterator<int[]> tfd = todoFill.iterator();
		while( tfd.hasNext() ){
			int[] next = tfd.next();
			
			if( next == null )
				continue;
			
			// Run TreeGenerator
			tg.getFailureRate(transitionStates[next[0]], transitionStates[next[1]]);
		}
		
		// Add up diagonals
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
	 * === NOTE ===
	 * QMatrixRunnable will call this method, please use the refactoring tool in Eclipse to make any modifications.
	 * 
	 * @param from
	 * @param to
	 * @return rate
	 */
	public double fillQMatrix(State from, State to){
		boolean repairTransition = true;
		boolean failedTransition = true;
		boolean enviroTransition = false;		
		String repair = null;
		int fromDemand = from.getDemand();
		int toDemand = to.getDemand();
		
		if( fromDemand != toDemand ){
			enviroTransition = true;
		}
		for( int i = 0; i < vectorKeys.length; i++ ){
			int iFrom = from.getVector().get(vectorKeys[i]),
				iTo = to.getVector().get(vectorKeys[i]);
			
			if( enviroTransition && iFrom != iTo ){
				return 0.0;
			} else if( repair == null && repairTransition && iTo == iFrom - 1 ){
				repair = vectorKeys[i];
			} else if( iFrom != iTo ){
				repairTransition = false;
			}
			if( iTo < iFrom ){
				failedTransition = false;
			}
		}
		
		if( enviroTransition )
			return demandMatrix[fromDemand][toDemand];
		if( failedTransition )
			return Double.NaN;
			
		if( repairTransition && repair != null )
			return (double)from.getVector().get(repair) / (double)from.sum();
		return 0.0;
	}
	
	/**
	 * @return the transitionStates
	 */
	public State[] getTransitionStates(){
		return transitionStates;
	}
	
	/**
	 * @param transitionStates the transitionStates to set
	 */
	public void setTransitionStates(State[] transitionStates){
		this.transitionStates = transitionStates;
	}
	
	/**
	 * @return the vectorKeys
	 */
	public String[] getVectorKeys(){
		return vectorKeys;
	}
	
	/**
	 * @param vectorKeys the vectorKeys to set
	 */
	public void setVectorKeys(String[] vectorKeys){
		this.vectorKeys = vectorKeys;
	}
	
	/**
	 * @return the qMatrix
	 */
	public double[][] getqMatrix(){
		return qMatrix;
	}

	/**
	 * @param qMatrix the qMatrix to set
	 */
	public void setqMatrix(double[][] qMatrix){
		this.qMatrix = qMatrix;
	}

	/**
	 * @return the demandLevels
	 */
	public double[][] getDemandLevels(){
		return demandMatrix;
	}

	/**
	 * @param demandLevels the demandLevels to set
	 */
	public void setDemandLevels(double[][] demandLevels){
		this.demandMatrix = demandLevels;
	}
	
	/**
	 * @return the todoFill
	 */
	public ArrayList<int[]> getTodoFill(){
		return todoFill;
	}

	/**
	 * @param todoFill the todoFill to set
	 */
	public void setTodoFill(ArrayList<int[]> todoFill){
		this.todoFill = todoFill;
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
