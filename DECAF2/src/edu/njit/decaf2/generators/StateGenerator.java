package edu.njit.decaf2.generators;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.data.FailureNode;
import edu.njit.decaf2.data.State;

/**
 * @author soulcyon
 *
 */
public class StateGenerator extends DECAF {
	private HashMap<String, FailureNode> 	componentMap;
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
		// HashSet to avoid combination duplicates
		HashSet<String> list = new HashSet<String>();
		String s = "";
		int m = 0;
		
		// Create generator string with appropriate number of redundancies
		for( String k : getComponentMap().keySet() ){
			for( int i = 0; i <= getComponentMap().get(k).getRedundancy(); i++ ){
				s += i;
			}
		}
		
		// Run algorithm over generator string
		list = findCombinations(s, getComponentMap().size());
		
		// HashSet -> sorted Array
		String[] sortedCombinations = new String[list.size()];
		list.toArray(sortedCombinations);
		
		// Sort by total # of failures, then lexicographically
		Arrays.sort(sortedCombinations/*, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
            	int aV = 0, bV = 0, result = 0;
            	for( int i = 0; i < a.length(); i++ ){
            		aV += Integer.parseInt(a.substring(i, i + 1));
            	}
            	for( int i = 0; i < b.length(); i++ ){
            		bV += Integer.parseInt(b.substring(i, i + 1));
            	}
            	result = aV - bV;
            	return result == 0 ? a.compareTo(b) : result;
            }
        }*/);
		
		// Push to states array
		transitionStates = new State[list.size() * getDemandLevels().length];

		for( String k : sortedCombinations ){
			for( int i = 0; i < getDemandLevels().length; i++ ){
				transitionStates[m++] = new State(getComponentMap().keySet(), k, i);
			}
		}
		return transitionStates;
	}
	
	/**
	 * Pushes combinations found over a generator string into a {@link HashSet}
	 * 
	 * @param prefix 
	 * @param elements Generator String
	 * @param k Maximum length of combination string
	 * @param list HashSet to put combinations inside
	 */
	private HashSet<String> findCombinations(String input, int k) {
		if (k == 0){
			HashSet<String> result = new HashSet<String>();
			result.add("");
			return result;
		}
		
		if (input.isEmpty())
			return new HashSet<String>();
		
		HashSet<String> finalResult = new HashSet<String>();
		String prefix = new StringBuilder().append((input.charAt(0))).toString();
		String restOfString = input.substring(1);
		HashSet<String> intermediate = findCombinations(restOfString,k-1);
		for(String s : intermediate){
			s = (prefix + s);
			finalResult.add(s);
		}
		HashSet<String> tailResult = findCombinations(restOfString, k);
		finalResult.addAll(tailResult);
		
		return finalResult;
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
