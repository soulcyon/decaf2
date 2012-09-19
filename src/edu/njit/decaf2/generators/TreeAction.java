/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.RecursiveAction;

import edu.njit.decaf2.Simulation;
import edu.njit.decaf2.structures.FailureNode;
import edu.njit.decaf2.structures.State;

/**
 * DECAF - TreeCallable
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public class TreeAction extends RecursiveAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1898255913879356515L;
	Entry<String, FailureNode> entry;
	protected static ArrayList<String> levels;

	public TreeAction(Entry<String, FailureNode> e) {
		entry = e;
	}

	/**
	 * 
	 */
	public TreeAction() {
		for( Entry<String, FailureNode> e : Simulation.nodeMap.entrySet() ){
			invokeAll(new TreeAction(e));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.RecursiveAction#compute()
	 */
	@Override
	protected void compute() {
		if (entry == null) {
			return;
		}

		levels = new ArrayList<String>();
		levels.add("1:" + entry.getKey());

		final State initialFT = (State) Simulation.states[0].clone();
		initialFT.incrementComponentCount(entry.getValue());

		final Map<String, ArrayList<String>> bfhMap = TreeGenerator.buildBFH();
		bfhMap.get(entry.getKey()).add("|");

		TreeGenerator.growSubTree(levels, initialFT, 1.0, bfhMap);
	}

}
