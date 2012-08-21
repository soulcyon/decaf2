/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.util.HashMap;
import java.util.HashSet;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.data.FailureNode;
import edu.njit.decaf2.data.State;
import edu.njit.decaf2.data.TreeNode;

/**
 * DECAF 2 - TreeGenerator
 *
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 *
 */
public class TreeGeneratorUnthreaded extends DECAF {
	private HashSet<TreeNode>				treeCache = new HashSet<TreeNode>();
	private HashMap<State, TreeNode> 		stateCache = new HashMap<State, TreeNode>();
	private HashMap<String, FailureNode>	nodeMap = new HashMap<String, FailureNode>();
	private State[]							states;
	private int								misses = 0;
	
	/**
	 * Sets {@link HashMap}<{@link String}, {@link FailureNode}> {@code nodeMap}
	 * @param decaf_nodeMap
	 */
	public TreeGeneratorUnthreaded(HashMap<String, FailureNode> nodeMap){
		this.nodeMap = nodeMap;
	}

	/**
	 * 
	 * @param from
	 * @param to
	 * @return rate
	 */
	public double getFailureRate(State[] ss, int from, int to){
		states = ss;
		State diffState = states[from].diff(states[to]);

		if( stateCache.containsKey(diffState) ){
			misses++;
			return stateCache.get(diffState).getRate();
		}

		double failureRate = 0.0;
		HashMap<String, Integer> diffVector = diffState.getVector();
		for( String k : diffVector.keySet() ){
			failureRate += buildTree(k, diffState);
		}
		//stateCache.put(diffState, new TreeNode(new FailureNode("NULL", new double[]{0.0}), 0));
		return failureRate;
	}
	
	/**
	 * 
	 * @param root
	 * @param transition
	 * @return
	 */
	private double buildTree(String root, State failureTransition) {
		TreeNode ft = new TreeNode(nodeMap.get(root), failureTransition.getDemand());
		ft.makeRoot();
		buildChildrenNodes(ft, failureTransition, 1.0);
		return ft.getRate();
	}
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	private boolean validState(State s){
		HashMap<String, Integer> temp = s.getVector();
		for( String k : temp.keySet() ){
			if( temp.get(k) > nodeMap.get(k).getRedundancy() )
				return false;
		}
		return true;
	}
	
	/**
	 * Builds a larger tree by attaching nodes.
	 * @param root
	 * @param transition
	 * @param rate
	 * @return
	 */
	private void buildChildrenNodes(TreeNode curr, State failureTransition, double rate) {
		
		for( int f = 0; f < states.length; f++ ){
			FailureNode currFailureNode = curr.getFailureNode();
			State from = states[f];
			State to = from.add(failureTransition);
			if( to == null || !validState(to) )
				continue;
			
			// r *= n * lambda 
			int n = currFailureNode.getRedundancy() - 
					  from.getComponentCount(currFailureNode.getType());
			rate *= currFailureNode.getFailureRates()[from.getDemand()] * n;
			
			// TODO Add rate to transition in Q
			System.out.println(curr);
			System.out.println("");
		}
		
		HashMap<String, Double> gamma = curr.getFailureNode().getCascadingFailures();
		int gammaLength = gamma.size();

		for( int g = (int)Math.pow(2, gammaLength) - 1; g > 0; g-- ){
			String gInBinary = Integer.toBinaryString(g);
			curr.clearChildren();
			for( int b = 0; b < gInBinary.length(); b++ ) {
				String[] entries = new String[gammaLength];
				entries = gamma.keySet().toArray(entries);
				 
				if( gInBinary.charAt(b) == '0' ) continue;
				
				FailureNode failedComponent = nodeMap.get(entries[b]);
				if( failureTransition.getComponentCount(entries[b]) < failedComponent.getRedundancy() ){
					curr.addChild(failedComponent);
					curr.setRate(curr.getFailureNode().getRate(entries[b]));
					failureTransition.incrementComponentCount(failedComponent);
				}
			}
		}
		for(TreeNode child : curr.getChildren()) {
			System.out.println(failureTransition + ":" + child);
			buildChildrenNodes(child, failureTransition, rate);
		}
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
