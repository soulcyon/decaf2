/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.util.ArrayList;
import java.util.List;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.Simulation;
import edu.njit.decaf2.structures.State;

/**
 * 
 * DECAF - StateGenerator
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public final class StateGenerator extends DECAF {
	/**
	 * 
	 */
	private StateGenerator() {
		super();
	}

	/**
	 * 
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/**
	 * 
	 * @return transitionStates
	 */
	public static void generateStates() {
		// comb will push resulting combinations into this list
		final List<List<Integer>> list = new ArrayList<List<Integer>>();
		// Go!
		combStates(0, new ArrayList<Integer>(), list);

		// Performance enhancement - use toArray rather than creating a new
		// variable for toArray(<T>)
		Simulation.states = new State[list.size() * Simulation.demandMatrix.length];

		// Iterate over combinations and add demand changes
		for (int i = 0, m = 0; i < list.size(); i++) {
			for (int j = 0; j < Simulation.demandMatrix.length; j++, m++) {
				Simulation.states[m] = new State(list.get(i), j);
			}
		}
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
	private static void combStates(final int index, final List<Integer> current, final List<List<Integer>> list) {

		if (current.size() == Simulation.typeList.size()) {
			list.add(current);
		}
		if (index >= Simulation.typeList.size()) {
			return;
		}

		for (int i = 0; i <= Simulation.nodeMap.get(Simulation.typeList.get(index)).getRedundancy(); i++) {
			final List<Integer> currentCopy = new ArrayList<Integer>(current);
			currentCopy.add(i);
			combStates(index + 1, currentCopy, list);
		}
	}

	public static String stateString() {
		final StringBuffer sgString = new StringBuffer();
		for (int i = 0; i < Simulation.states.length; i++) {
			sgString.append(Simulation.states[i].toLine());
			sgString.append('\t');
		}
		return error(sgString.toString());
	}
}
