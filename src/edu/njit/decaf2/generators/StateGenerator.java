/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.util.ArrayList;
import java.util.HashMap;

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
	private ArrayList<FailureNode> componentList = new ArrayList<FailureNode>();
	private int compLen;
	private HashMap<String, FailureNode> componentMap;
	private double[][] demandMatrix;
	private State[] transitionStates;

	public StateGenerator(HashMap<String, FailureNode> componentMap, double[][] demandMatrix) {
		this.componentMap = componentMap;
		this.demandMatrix = demandMatrix;
	}

	/**
	 * 
	 * @return transitionStates
	 */
	public State[] generateStates() {
		// comb will push resulting combinations into this list
		ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();

		// Convert HashMap componentMap to ArrayList for comb
		for (String k : componentMap.keySet()) {
			componentList.add(componentMap.get(k));
		}
		compLen = componentList.size();

		// Go!
		
		combStates(0, new ArrayList<Integer>(), list);

		// Performance enhancement - use toArray rather than creating a new
		// variable for toArray(<T>)
		transitionStates = new State[list.size() * demandMatrix.length];

		// Iterate over combinations and add demand changes
		for (int i = 0, m = 0; i < list.size(); i++) {
			for (int j = 0; j < demandMatrix.length; j++, m++) {
				transitionStates[m] = new State(componentMap.keySet(), list.get(i), j);
			}
		}
		return transitionStates;
	}

	/**
	 * Generates all possible combinations of Transition-States Minimal
	 * overhead, for every change between components there will be one extra
	 * String generated.
	 * 
	 * Complexity - O( Maximum Redundancy ^ Number of components )
	 * 
	 * Because of the high complexity at this stage in the algorithm, the whole
	 * Simulation has a practical limit of about 10 - 20 components with less
	 * than 3 redundancy. Matrices created this large will takes hours or days
	 * to iterate. This is before even thinking about the N^3 complexity of
	 * Matrix Inversion and Multiplication.
	 * 
	 * @param x
	 * @param str
	 * @param list
	 */
	private void combStates(int x, ArrayList<Integer> current, ArrayList<ArrayList<Integer>> list) {
		if (current.size() == compLen) {
			list.add(current);
		}
		if (x >= componentList.size())
			return;

		for (int i = 0; i <= componentList.get(x).getRedundancy(); i++) {
			current.add(i);
			combStates(x + 1, current, list);
		}
	}

	/**
	 * @return the componentMap
	 */
	public HashMap<String, FailureNode> getComponentMap() {
		return componentMap;
	}

	/**
	 * @param componentMap
	 *            the componentMap to set
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
	 * @param demandLevels
	 *            the demandLevels to set
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
	 * @param transitionStates
	 *            the transitionStates to set
	 */
	public void setTransitionStates(State[] transitionStates) {
		this.transitionStates = transitionStates;
	}

	@Override
	public String toString() {
		String sgString = "";
		for (int i = 0; i < transitionStates.length; i++) {
			sgString += transitionStates[i].toLine() + "\t";
		}
		return error(sgString);
	}
}
