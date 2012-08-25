/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.util.ArrayList;
import java.util.HashMap;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.Simulation;
import edu.njit.decaf2.data.ComplementPhi;
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
	public static HashMap<String, ComplementPhi> complementPhiMap = new HashMap<String, ComplementPhi>();
	/**
	 * 
	 * @param statesCopy
	 */
	public static void buildTree() {
		// Populate phiMap
		for( String root : Simulation.nodeMap.keySet() ){
			complementPhiMap.put(root, new ComplementPhi(root));
		}
		
		// 
		for(String root : Simulation.nodeMap.keySet()) {
			TreeNode ft = new TreeNode(Simulation.nodeMap.get(root));
			ft.makeRoot();
			State initFailureTransition = (State)Simulation.states[0].clone();
			initFailureTransition.incrementComponentCount(Simulation.nodeMap.get(root));
			buildChildrenNodes(ft, initFailureTransition, 1.0);
		}
	}
	
	/**
	 * Builds a larger tree by attaching nodes.
	 * @param root
	 * @param transition
	 * @param rate
	 * @return
	 */
	private static void buildChildrenNodes(TreeNode curr, State failureTransition, double subTreeRate) {

		ArrayList<String> likeTransitions = QMatrixGeneratorUnthreaded.likeTransitionMap.get(failureTransition);
		System.out.println(curr + "\n\n");

		HashMap<String, Double> gamma = curr.getFailureNode().getCascadingFailures();
		int gammaLength = gamma.size();
		
		// Tree cannot be grown
		if( gammaLength == 0 )
			return;
		
		for( int g = 0; g < (int)Math.pow(2, gammaLength); g++ ){
			
			String gInBinary = String.format("%" + gammaLength + "s", Integer.toBinaryString(g)).replace(' ', '0');
			
			curr.clearChildren();
			State tempFailureTransition = failureTransition; 
			
			for( int b = 0; b < gInBinary.length(); b++ ) {
				
				String[] entriesInGamma = new String[gammaLength];
				entriesInGamma = gamma.keySet().toArray(entriesInGamma);
				FailureNode triggerComponent = Simulation.nodeMap.get(entriesInGamma[b]);
				
				if (gInBinary.charAt(b) == '0') {
					complementPhiMap.get(entriesInGamma[b]).addParent(curr.getFailureNode().getType());
					//System.out.println("count:" + curr.getPhiCount(entriesInGamma[b]));
				} else if( failureTransition.getComponentCount(entriesInGamma[b]) < triggerComponent.getRedundancy() ) {
					complementPhiMap.get(entriesInGamma[b]).removeTopParent();
					curr.addChild(triggerComponent);
					tempFailureTransition.incrementComponentCount(triggerComponent);
					subTreeRate *= curr.getFailureNode().getRate(entriesInGamma[b]);
				}
			}
			
			for( String transition : likeTransitions ){
				for( String k : complementPhiMap.keySet() ){
					complementPhiMap.get(k).reset();
				}
				double superRate = 1.0;
				
				String[] fromAndTo = transition.split(",");
				int f = Integer.parseInt(fromAndTo[0]);
				int t = Integer.parseInt(fromAndTo[1]);
				
				State from = Simulation.states[f];
				State to = Simulation.states[t];
				
				FailureNode root = curr.getRoot();
				
				// n * lambda for root of tree
				int n = root.getRedundancy() - from.getComponentCount(root.getType());
				double lambda = root.getFailureRates()[from.getDemand()];
				superRate = n * lambda;
				System.out.println("n * lambda = " + (n * lambda));
				
				for( String k : Simulation.nodeMap.keySet() ){
					// The same type can never be added as a child so 1 - does not apply because there was no choice made 
					if(curr.getFailureNode().getType() == k)
						continue;
					
					int maximumComplementPhi = Simulation.nodeMap.get(k).getRedundancy() - to.getComponentCount(k),
						neededComplementPhi = Math.min(maximumComplementPhi, complementPhiMap.get(k).size());
					
					for( int i = 0; i < neededComplementPhi; i++ ){
						double temp = 0.0;
						System.out.println("1 - phi: " +  (1 - (temp = Simulation.nodeMap.get(complementPhiMap.get(k).getTopParent()).getRate(k))));
						superRate *= 1 - temp;
					}
				}
				System.out.println(f + ", " + t + " => " + subTreeRate + " _ " + superRate + "\n");
				Simulation.qMatrix[f][t] += subTreeRate * superRate;
				
				
			}
			
			for(TreeNode child : curr.getChildren()) {
				buildChildrenNodes(child, tempFailureTransition, subTreeRate);
			}
		}
	}
}
