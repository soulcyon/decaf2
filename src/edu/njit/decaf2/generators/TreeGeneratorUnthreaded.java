/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.util.ArrayList;
import java.util.HashMap;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.Simulation;
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
	/**
	 * 
	 * @param statesCopy
	 */
	
	public static void buildTree() {
		
		// calculations for nodes that could have failed but did not fail
		HashMap<String, ArrayList<String>> complementMap = new HashMap<String, ArrayList<String>>();
		
		for(String root : Simulation.nodeMap.keySet()) {
			
			ArrayList<String> couldHaveCausedToFail = new ArrayList<String>();
			
			for(String k : Simulation.nodeMap.keySet()) {
				complementMap.put(k, new ArrayList<String>());
			}
			
			couldHaveCausedToFail.add("|");
			complementMap.put(root, couldHaveCausedToFail);
			
			TreeNode ft = new TreeNode(Simulation.nodeMap.get(root));
			ft.makeRoot();
			
			State initFailureTransition = (State)Simulation.states[0].clone();
			initFailureTransition.incrementComponentCount(Simulation.nodeMap.get(root));
			
			buildChildrenNodes(ft, initFailureTransition, 1.0, complementMap);
		}
	}
	
	/**
	 * Builds a larger tree by attaching nodes.
	 * @param root
	 * @param transition
	 * @param rate
	 * @return
	 */
	private static void buildChildrenNodes(TreeNode curr, State failureTransition, double subTreeRateExcludingRoot, 
			HashMap<String, ArrayList<String>> complementMap) {

		HashMap<String, ArrayList<String>> complementMapCopy = new HashMap<String, ArrayList<String>>();
		complementMapCopy.putAll(complementMap);
		
		ArrayList<String> likeTransitions = QMatrixGeneratorUnthreaded.likeTransitionMap.get(failureTransition);
		
		if(verboseDebug) {
			System.out.println(curr);
			System.out.println(complementMapCopy);
			System.out.println("\n\n");
		}

		HashMap<String, Double> gamma = curr.getFailureNode().getCascadingFailures();
		int gammaLength = gamma.size();
		
		// Tree cannot be grown any further
		if( gammaLength == 0 )
			return;
		
		for( int g = 1; g < (int)Math.pow(2, gammaLength); g++ ){
			
			String gInBinary = String.format("%" + gammaLength + "s", Integer.toBinaryString(g)).replace(' ', '0');
			
			curr.clearChildren();
			State tempFailureTransition = failureTransition; 
			
			for( int b = 0; b < gInBinary.length(); b++ ) {
				
				String[] entriesInGamma = new String[gammaLength];
				entriesInGamma = gamma.keySet().toArray(entriesInGamma);
				FailureNode triggerComponent = Simulation.nodeMap.get(entriesInGamma[b]);
				
				if(!(failureTransition.getComponentCount(entriesInGamma[b]) < triggerComponent.getRedundancy()))
					continue;
				
				if (gInBinary.charAt(b) == '0') {
					complementMapCopy.get(entriesInGamma[b]).add(curr.getFailureNode().getType());
				} else {
					curr.addChild(triggerComponent);
					tempFailureTransition.incrementComponentCount(triggerComponent);
					complementMapCopy.get(entriesInGamma[b]).add("|");
					subTreeRateExcludingRoot *= curr.getFailureNode().getRate(entriesInGamma[b]);
				}
			}
			
			for( String transition : likeTransitions ) {
				
				String[] fromAndTo = transition.split(",");
				int f = Integer.parseInt(fromAndTo[0]);
				int t = Integer.parseInt(fromAndTo[1]);
				State from = Simulation.states[f];
				
				FailureNode root = curr.getRoot();
				
				// n * lambda for root of tree
				int n = root.getRedundancy() - from.getComponentCount(root.getType());
				double lambda = root.getFailureRates()[from.getDemand()];
				double rootRate = n * lambda;
				
				double complementRate = 1.0;
				for(String k : Simulation.nodeMap.keySet()) {
					
					int compsAvailable = Simulation.nodeMap.get(k).getRedundancy() - from.getComponentCount(k);
					ArrayList<String> couldHaveCausedToFail = complementMapCopy.get(k);
					
					for(String s : couldHaveCausedToFail) {
						if(s.equals("|")) {
							--compsAvailable;
						}
						else if(compsAvailable > 0) {
							FailureNode parent = Simulation.nodeMap.get(s);
							complementRate *= 1 - parent.getRate(k);
						}
						else
							break;
					}
				}
				
				Simulation.qMatrix[f][t] += rootRate * subTreeRateExcludingRoot * complementRate;
				
			}
			
			for(TreeNode child : curr.getChildren()) {
				buildChildrenNodes(child, tempFailureTransition, subTreeRateExcludingRoot, complementMapCopy);
			}
		}
	}
}
