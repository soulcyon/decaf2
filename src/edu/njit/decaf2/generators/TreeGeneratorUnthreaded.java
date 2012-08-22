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
	 *
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
	}*/
	
	/**
	 * 
	 * @param root
	 * @param transition
	 * @return
	 */
	/**
	 * @param root
	 * @param failureTransition
	 * @return
	 */
	public void buildTree(State[] ss) {
		states = ss;
		for(String root : nodeMap.keySet()) {
			TreeNode ft = new TreeNode(nodeMap.get(root));
			ft.makeRoot();
			State initFailureTransition = (State) states[0].clone();
			System.out.println(initFailureTransition);
			initFailureTransition.incrementComponentCount(nodeMap.get(root));
			buildChildrenNodes(ft, initFailureTransition, 1.0);
		}
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
		
		// print trees
		System.out.println("curr:\n" + curr + "\n\n");
		
		for( int f = 0; f < states.length; f++ ){
			
			State from = states[f];
			State to = from.add(failureTransition);
			if( to == null || !validState(to) )
				continue;
			
			FailureNode root = curr.getRoot();
			
			// n * lambda for root of tree
			int n = root.getRedundancy() - 
					  from.getComponentCount(root.getType());
			rate *= root.getFailureRates()[from.getDemand()] * n;
			
			// TODO Add rate to transition in Q
		}
		
		HashMap<String, Double> gamma = curr.getFailureNode().getCascadingFailures();
		int gammaLength = gamma.size();
		for( int g = (int)Math.pow(2, gammaLength) - 1; g > 0; g-- ){
			
			String gInBinary = String.format("%" + gammaLength + "s", Integer.toBinaryString(g)).replace(' ', '0'); // padding 0's
			
			curr.clearChildren();
			State tempFailureTransition = failureTransition; 
			
			for( int b = 0; b < gInBinary.length(); b++ ) {
				
				String[] entriesInGamma = new String[gammaLength];
				entriesInGamma = gamma.keySet().toArray(entriesInGamma);
				FailureNode triggerComponent = nodeMap.get(entriesInGamma[b]);
				
				if( failureTransition.getComponentCount(entriesInGamma[b]) < triggerComponent.getRedundancy() ) {
				
					// adding probability i.e. "Φ" of a node failure because it failed due to parent
					if( gInBinary.charAt(b) == '1' )  {
						//System.out.println("Adding child " + triggerComponent.getType() + " to parent " + curr.getFailureNode().getType());
						curr.addChild(triggerComponent);
						tempFailureTransition.incrementComponentCount(triggerComponent);
						// phi
						rate*= curr.getFailureNode().getRate(entriesInGamma[b]);
					}
					
					// adding complement probability i.e. "1-Φ' of a node because it could have failed but did not fail
					else if (gInBinary.charAt(b) == '0') {
						// 1 - phi
						rate*= (1 - curr.getFailureNode().getRate(entriesInGamma[b]));
					}
				}
			}
			
			for(TreeNode child : curr.getChildren()) {
				buildChildrenNodes(child, tempFailureTransition, rate);
			}
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
