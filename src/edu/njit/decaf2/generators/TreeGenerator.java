/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.util.HashMap;
import java.util.HashSet;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.data.FailureNode;
import edu.njit.decaf2.data.TreeNode;
import edu.njit.decaf2.data.State;

/**
 * DECAF 2 - TreeGenerator
 *
 * @author Sashank Tadepalli
 * @version 2.0
 *
 */
public class TreeGenerator extends DECAF {
	private HashSet<TreeNode>				treeCache = new HashSet<TreeNode>();
	private HashMap<String, TreeNode> 		stateCache = new HashMap<String, TreeNode>();
	private HashMap<String, FailureNode>	nodeMap = new HashMap<String, FailureNode>();
	private int								misses = 0;
	
	/**
	 * Sets {@link HashMap}<{@link String}, {@link FailureNode}> {@code nodeMap}
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
		if( stateCache.containsKey(diffState.toString()) ){
			misses++;
			return stateCache.get(diffState.toString()).getRate();
		}

		double failureRate = 0.0;
		HashMap<String, Integer> diffVector = diffState.getVector();
		for( String k : diffVector.keySet() ){
			failureRate += buildTree(k, diffState);
		}
		return failureRate;
	}
	
	private double buildTree(String root, State transition){
		TreeNode ft = new TreeNode(nodeMap.get(root), transition.getDemand());
		stateCache.put(transition.toString(), ft);
		return ft.getRate();
	}

	/**
	 * @return the nodeM
	 */
	public HashMap<String, FailureNode> getNodeM() {
		return nodeMap;
	}
	
	/**
	 * @return the cache
	 */
	public HashMap<String, TreeNode> getCache() {
		return stateCache;
	}
	
	/**
	 * @return the stateCache
	 */
	public HashMap<String, TreeNode> getStateCache() {
		return stateCache;
	}

	/**
	 * @return the misses
	 */
	public int getMisses() {
		return misses;
	}
}
