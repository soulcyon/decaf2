/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.data.FailureNode;
import edu.njit.decaf2.data.State;

/**
 * DECAF 2 - StateGenerator
 *
 * @author Sashank Tadepalli
 * @version 2.0
 *
 */
public class StateGenerator extends DECAF {
	private Vector<FailureNode> 			componentList = new Vector<FailureNode>();
	private HashMap<String, FailureNode> 	componentMap = new HashMap<String, FailureNode>();
	private double[][]						demandMatrix;
	private State[]							transitionStates;
	
	public StateGenerator(HashMap<String, FailureNode> componentMap, double[][] demandMatrix){
		setComponentMap(componentMap);
		setDemandLevels(demandMatrix);
	}
	
	/**
	 * Pushes combinations to the {@code states} array
	 */
	public State[] generateStates(){
		HashSet<String> list = new HashSet<String>();
		for( String k : componentMap.keySet() ){
			componentList.add(componentMap.get(k));
		}
		
		findCombinations(componentMap.size(), list);
		
		String[] result = new String[list.size()];
		transitionStates = new State[result.length * demandMatrix.length];
		Arrays.sort((result = list.toArray(result)));
		
		for( int i = 0, m = 0; i < result.length; i++ ){
			for( int j = 0; j < demandMatrix.length; j++, m++ ){
				transitionStates[m] = new State(componentMap.keySet(), result[i], j);
			}
		}
		return transitionStates;
	}
	
	private void findCombinations(int len, HashSet<String> list){
		findCombinations(len, componentList.get(0).getRedundancy(), 0, "", list);
	}
	
	private void findCombinations(int len, int max, int curr, String pref, HashSet<String> list){
		if( pref.length() == len ){
			list.add(pref);
		}
		if( curr >= len ){
			return;
		}
		for( int i = 0; i < componentList.get(curr).getRedundancy() + 1; i++ ){
			findCombinations(len, i, curr + 1, pref + i, list);
		}
	}

	/**
	 * @return the componentMap
	 */
	public HashMap<String, FailureNode> getComponentMap() {
		return componentMap;
	}
	
	/**
	 * @param componentMap the componentMap to set
	 */
	public void setComponentMap(HashMap<String, FailureNode> componentMap) {
		this.componentMap = componentMap;
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
	
	@Override
	public String toString(){
		String sgString = "";
		for( int i = 0; i < transitionStates.length; i++ ){
			sgString += transitionStates[i].toLine() + "\t";
		}
		return error(sgString + "\n");
	}
}
