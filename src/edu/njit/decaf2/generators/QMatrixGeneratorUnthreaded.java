/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.util.ArrayList;
import java.util.HashMap;
//import java.util.Iterator;

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
	
	private TreeGeneratorUnthreaded			  	tg;
	private ArrayList<int[]>				  	todoFill = new ArrayList<int[]>();
	private State[] 						  	states;
	private String[] 						  	vectorKeys;
	private double[][] 						  	demandMatrix;
	
	static HashMap<State, ArrayList<String>> 	likeTransitionMap;
	static double[][]						  	qMatrix;
	
	/**
	 * Sets {@link State} {@code transitionStates}, {@link String}[] {@code vectorKeys}, {@link Double}[][] 
	 * {@code demandMatrix}
	 * 
	 * @param states
	 * @param vectorKeys
	 */
	public QMatrixGeneratorUnthreaded(State[] ts, String[] vk, double[][] dm, TreeGeneratorUnthreaded t){
		states = ts;
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
		int statesLen = states.length;
		
		// Initialize brand new qMatrix[][] array
		QMatrixGeneratorUnthreaded.qMatrix = new double[statesLen][statesLen];
		
		likeTransitionMap = new HashMap<State, ArrayList<String>>();

		// Iterate over matrix, ignore diagonal
		for( int i = 0; i < statesLen; i++ ){
			//for( int j = i == 0 ? 1 : 0; j < statesLen; j = j == i - 1 ? j + 2 : j + 1 ){
			for( int j = 0; j < statesLen; j++){
				
				if(j == i) continue;
				double fillV = fillQMatrix(states[i], states[j]);
				
				if( Double.isNaN(fillV) ){
					State differenceState = states[i].diff(states[j]);
					String str = i + "," + j;
					if( likeTransitionMap.containsKey(differenceState) ){
						likeTransitionMap.get(differenceState).add(str);
					} else {
						ArrayList<String> temp = new ArrayList<String>();
						temp.add(str);
						likeTransitionMap.put(differenceState, temp);
					}
					todoFill.add(new int[]{i, j});
				} else {
					qMatrix[i][j] = fillV;
				}
			}
		}
		
		// Generate trees as required
		tg.buildTree(states);
		
		/*Iterator<int[]> tfd = todoFill.iterator();
		while( tfd.hasNext() ){
			int[] next = tfd.next();
			
			if( next == null )
				continue;
			
			// TODO Calculate state differences and populate LikeTransitionMapper
			// Run TreeGenerator
			qMatrix[next[0]][next[1]] = tg.getFailureRate(states, next[0], next[1]);
		}
		
		// Add up diagonals
		for( int i = 0; i < statesLen; i++ ){
			int sum = 0;
			for( int j = i == 0 ? 1 : 0; j < statesLen; j = j == i - 1 ? j + 2 : j + 1 ){
				sum += qMatrix[i][j];
			}
			qMatrix[i][i] = sum;
		}*/
		
		//diagonal entries = negative of the sum of row entries
		for( int i = 0; i < statesLen; i++ ){
			int sum = 0;
			for( int j = 0 ; j < statesLen; j++ ){
				sum += qMatrix[i][j];
			}
			qMatrix[i][i] = -sum;
		}
		
		return qMatrix;
	}
	
	/**
	 * 
	 * @return
	 */
	
	public HashMap<State, ArrayList<String>> getLikeTransitionMap() {
		return likeTransitionMap;
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
	public State[] getStates(){
		return states;
	}
	
	/**
	 * @param transitionStates the transitionStates to set
	 */
	public void setStates(State[] transitionStates){
		this.states = transitionStates;
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
		QMatrixGeneratorUnthreaded.qMatrix = qMatrix;
	}
	
	/**
	 * 
	 * @param value
	 * @param i
	 * @param j
	 */
	static void setValue(double value, int i, int j) {
		qMatrix[i][j] = value;
	}
	
	/**
	 *  
	 * @param i
	 * @param j
	 * @return
	 */

	static double getValue(int i, int j) {
		return qMatrix[i][j];
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
		int statesLen = states.length;
		String result = "";
		for( int i = 0; i < statesLen; i++ ){
			for( int j = 0; j < statesLen; j++ ){
				result += qMatrix[i][j] + "@(" + i + "," + j + "); ";
			}
			result += "\n";
		}
		return error(result);
	}
}
