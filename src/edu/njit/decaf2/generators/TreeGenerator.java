/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

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
		
		buildLargerTree(ft, failureTransition);
		
		return 0.0;
	}
	
	/**
	 * 
	 * @param root
	 * @param transition
	 * @return
	 */
	
	private double buildLargerTree(TreeNode curr, State failureTransition) {		
		
		if(curr.isLeaf()) {
			
			 HashMap<String, Double> gamma = curr.getFailureNode().getCascadingFailures();
			 
			 // build power set of gamma components
			 for (int g = (int) Math.pow(2, gamma.size()) - 1; g > 0; g--) {
				 
				 String gInBinary = Integer.toBinaryString(g);
				 curr.clearChildren();
				 
				 // binary enumeration will toggle inclusion of a component in a subset 
				 for (int b = 0; b < gInBinary.length(); b++) {
					 
					 String[] entries = (String[]) gamma.keySet().toArray();
					 
						if (gInBinary.charAt(b) == '1') {
							TreeNode child = new TreeNode(new FailureNode(entries[b]));
							curr.addChild(child.getFailureNode());
							// TODO update failure transition by adding 1 in place of the component type just added 
							buildLargerTree(child, failureTransition);
						}
				 }
			 }
		}
		
		else {
			for(TreeNode child : curr.getChildren()) {
				buildLargerTree(child, failureTransition);
			}
		}
			
		return 0.0;
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
