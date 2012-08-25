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
	
	public static void initSubTrees() {
		
		for(String root : Simulation.nodeMap.keySet()) {	
			
			TreeNode ft = new TreeNode(Simulation.nodeMap.get(root));
			ft.makeRoot();
			
			HashMap <String, ArrayList<String>> breadthFirstHistory = new HashMap<String, ArrayList<String>>();
			
			for(String k : Simulation.nodeMap.keySet())
				breadthFirstHistory.put(k, new ArrayList<String>());
			
			breadthFirstHistory.get(root).add("|");
			
			State initFailureTransition = (State)Simulation.states[0].clone();
			initFailureTransition.incrementComponentCount(Simulation.nodeMap.get(root));
			
			buildSubTree(ft, initFailureTransition, 1.0, breadthFirstHistory);
		}
	}
	
	/**
	 * Builds a larger tree by attaching nodes.
	 * @param root
	 * @param transition
	 * @param rate
	 * @return
	 */
	private static void buildSubTree(TreeNode curr, State failureTransition, double subTreeRateExcludingRoot, 
		HashMap<String, ArrayList<String>> breadthfirstHistory) {

		HashMap<String, Double> gamma = curr.getFailureNode().getCascadingFailures();
		int gammaLength = gamma.size();
		
		for( int g = 0; g < (int)Math.pow(2, gammaLength); g++ ) {
			
			// properly clones HashMap by cloning internal ArrayLists, putAll fails because vales are a non-primitive type 
			HashMap<String, ArrayList<String>> tempHistory = new HashMap<String, ArrayList<String>>();
			for(String key : breadthfirstHistory.keySet()) {
				ArrayList<String> compHistory = new ArrayList<String>(breadthfirstHistory.get(key)); 
				tempHistory.put(key, compHistory);
			}
			
			// clones failureTransition
			State tempFailureTransition = failureTransition;
			
			String gInBinary = String.format("%" + gammaLength + "s", Integer.toBinaryString(g)).replace(' ', '0');
			
			for( int b = 0; b < gInBinary.length(); b++ ) {
				
				String[] entriesInGamma = new String[gammaLength];
				entriesInGamma = gamma.keySet().toArray(entriesInGamma);
				FailureNode triggerComponent = Simulation.nodeMap.get(entriesInGamma[b]);
				
				if(!(failureTransition.getComponentCount(entriesInGamma[b]) < triggerComponent.getRedundancy()))
					continue;
			
				if (gInBinary.charAt(b) == '1'){
					curr.addChild(triggerComponent);
					tempFailureTransition.incrementComponentCount(triggerComponent);
					subTreeRateExcludingRoot *= curr.getFailureNode().getRate(entriesInGamma[b]);
					tempHistory.get(entriesInGamma[b]).add("|");
				}
				else {
					tempHistory.get(entriesInGamma[b]).add(curr.getFailureNode().getType());
				}
					
			}
			
			ArrayList<String> likeTransitions = QMatrixGeneratorUnthreaded.likeTransitionMap.get(failureTransition);
			
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
					ArrayList<String> couldHaveFailed = tempHistory.get(k);
				 
					for(String s : couldHaveFailed)
						
						if(s.equals("|")) 
							--compsAvailable;
					
						else if(compsAvailable > 0) {
							FailureNode parent = Simulation.nodeMap.get(s);
							complementRate *= 1 - parent.getRate(k);
						}
					
						else break;
				}

				Simulation.qMatrix[f][t] += rootRate * subTreeRateExcludingRoot * complementRate;	
			}
			
			if(verboseDebug) {
				System.out.println("Binary:" + gInBinary);
				System.out.println(curr);
				System.out.println(tempHistory);
				System.out.println("\n\n");
			}
			
			for(TreeNode child : curr.getChildren()) {
				buildSubTree(child, tempFailureTransition, subTreeRateExcludingRoot, tempHistory);
			}
		}
	}
}
