/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.util.HashMap;
import java.util.HashSet;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.data.FailureNode;
import edu.njit.decaf2.data.FailureTree;
import edu.njit.decaf2.data.State;

/**
 * DECAF 2 - TreeGenerator
 *
 * @author Sashank Tadepalli
 * @version 2.0
 *
 */
public class TreeGenerator extends DECAF {
	private HashSet<FailureTree>			treeCache = new HashSet<FailureTree>();
	private HashMap<State, Double> 			stateCache = new HashMap<State, Double>();
	private HashMap<String, FailureNode>	nodeMap = new HashMap<String, FailureNode>();
	private int								misses = 0;
	
	/**
	 * @param decaf_nodeMap
	 */
	public TreeGenerator(HashMap<String, FailureNode> nodeMap) {
		this.nodeMap = nodeMap;
	}

	/**
	 * 
	 * @param from
	 * @param to
	 * @return rate
	 */
	public double getFailureRate(State from, State to){
		State diffState = from.diff(to);
		if( stateCache.containsKey(diffState) ){
			misses++;
			return stateCache.get(diffState);
		}

		double failureRate = 0.0;
		HashMap<String, Integer> diffVector = diffState.getVector();
		for( String k : diffVector.keySet() ){
			FailureTree curr = buildTree(k, diffState);
		}
		stateCache.put(diffState, failureRate);
		return failureRate;
	}
	
	private FailureTree buildTree(String root, State transition){
		FailureTree ft = new FailureTree(nodeMap.get(root));
		return ft;
	}

	/**
	 * @return the nodeM
	 */
	public HashMap<String, FailureNode> getNodeMap() {
		return nodeMap;
	}

	/**
	 * @return the misses
	 */
	public int getMisses() {
		return misses;
	}
	
	/**
	 * @return the stateCache
	 */
	public HashMap<State, Double> getStateCache() {
		return stateCache;
	}
}
