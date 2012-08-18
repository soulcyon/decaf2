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
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 *
 */
public class TreeGenerator extends DECAF {
	private HashSet<TreeNode>				treeCache = new HashSet<TreeNode>();
	private HashMap<State, TreeNode> 		stateCache = new HashMap<State, TreeNode>();
	private HashMap<String, FailureNode>	nodeMap = new HashMap<String, FailureNode>();
	private int								misses = 0;
	
	/**
	 * Sets {@link HashMap}<{@link String}, {@link FailureNode}> {@code nodeMap}
	 * @param decaf_nodeMap
	 */
	public TreeGenerator(HashMap<String, FailureNode> nodeMap){
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
			return stateCache.get(diffState).getRate();
		}

		double failureRate = 0.0;
		HashMap<String, Integer> diffVector = diffState.getVector();
		for( String k : diffVector.keySet() ){
			failureRate += buildTree(k, diffState);
		}
		stateCache.put(diffState, new TreeNode(new FailureNode("NULL", new double[]{0.0}), 0));
		return failureRate;
	}
	
	/**
	 * 
	 * @param root
	 * @param transition
	 * @return
	 */
	private double buildTree(String root, State failureTransition) {		
		
		TreeNode ft = new TreeNode(nodeMap.get(root));
		
		// TODO n lambda calculations for all demand levels
		
		buildLargerTree(ft, failureTransition, 1.0);
		
		return 0.0;
	}
	
	/**
	 * 
	 * @param root
	 * @param transition
	 * @param rate
	 * @return
	 */
	
	private double buildLargerTree(TreeNode curr, State failureTransition, double rate) {		
		
		if(curr.isLeaf()) {
			
			// TODO populate Q with current Tree
			
			 HashMap<String, Double> gamma = curr.getFailureNode().getCascadingFailures();
			 
			 // build power set of gamma components
			 for (int g = (int) Math.pow(2, gamma.size()) - 1; g > 0; g--) {
				 
				 String gInBinary = Integer.toBinaryString(g);
				 curr.clearChildren();
	
				 // binary enumeration will toggle inclusion of a component in a subset 
				 for (int b = 0; b < gInBinary.length(); b++) {
					 
					 String[] entries = (String[]) gamma.keySet().toArray();
					 
						if (gInBinary.charAt(b) == '1' && failureTransition.getComponentCount(entries[b]) < nodeMap.get(entries[b]).getRedundancy()) {
							TreeNode child = new TreeNode(new FailureNode(entries[b]));
							curr.addChild(child.getFailureNode());
							rate *= curr.getFailureNode().getRate(child.getFailureNode().getType());
							
							// builds a new failureTransition that applies to tree
							failureTransition.incrementComponentCount(child.getFailureNode());
						}
				 }
			 }
		}
		for(TreeNode child : curr.getChildren()) {
			buildLargerTree(child, failureTransition, rate);
		}
			
		return rate;
	}

	/**
	 * @return the nodeM
	 */
	public HashMap<String, FailureNode> getNodeMap(){
		return nodeMap;
	}
	
	/**
	 * @return the cache
	 */
	public HashMap<State, TreeNode> getCache(){
		return stateCache;
	}

	/**
	 * @return the misses
	 */
	public int getMisses(){
		return misses;
	}

	/**
	 * @return the treeCache
	 */
	public HashSet<TreeNode> getTreeCache(){
		return treeCache;
	}
}
