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
		for(String root : Simulation.nodeMap.keySet()) {
			TreeNode ft = new TreeNode(Simulation.nodeMap.get(root));
			ft.makeRoot();
			State initFailureTransition = (State)Simulation.states[0].clone();
			initFailureTransition.incrementComponentCount(Simulation.nodeMap.get(root));
			buildChildrenNodes(ft, initFailureTransition, 1.0);
		}
	}
	
	/**
	 * 
	 * @param s
	 * @return
	private boolean validState(State s){
		HashMap<String, Integer> temp = s.getVector();
		for( String k : temp.keySet() ){
			if( temp.get(k) > nodeMap.get(k).getRedundancy() )
				return false;
		}
		return true;
	}*/
	
	/**
	 * Builds a larger tree by attaching nodes.
	 * @param root
	 * @param transition
	 * @param rate
	 * @return
	 */
	private static void buildChildrenNodes(TreeNode curr, State failureTransition, double subTreeRate) {
		
		ArrayList<String> likeTransitions = QMatrixGeneratorUnthreaded.likeTransitionMap.get(failureTransition);
		
		// build a larger tree
		HashMap<String, Double> gamma = curr.getFailureNode().getCascadingFailures();
		int gammaLength = gamma.size();
		
		// tree cannot be grown
		if( gammaLength == 0 )
			return;
		
		for( int g = 0; g < (int)Math.pow(2, gammaLength); g++ ){
			
			String gInBinary = String.format("%" + gammaLength + "s", Integer.toBinaryString(g)).replace(' ', '0');
			
			curr.clearChildren();
			State tempFailureTransition = failureTransition; 
			
			//System.out.println("GIB:" + gInBinary);
			for( int b = 0; b < gInBinary.length(); b++ ) {
				
				String[] entriesInGamma = new String[gammaLength];
				entriesInGamma = gamma.keySet().toArray(entriesInGamma);
				FailureNode triggerComponent = Simulation.nodeMap.get(entriesInGamma[b]);
				
				if (gInBinary.charAt(b) == '0') {
					curr.putComplementPhi(entriesInGamma[b], curr.getFailureNode().getType());
					//System.out.println("count:" + curr.getPhiCount(entriesInGamma[b]));
				} else if( failureTransition.getComponentCount(entriesInGamma[b]) < triggerComponent.getRedundancy() ) {
					curr.addChild(triggerComponent);
					tempFailureTransition.incrementComponentCount(triggerComponent);
					subTreeRate *= curr.getFailureNode().getRate(entriesInGamma[b]);
				}
			}
			
			for( String transition : likeTransitions ){
				
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
				
				for( String k : Simulation.nodeMap.keySet() ){
					
					//the same type can never be added as a child so 1 - does not apply because there was no choice made 
					if(curr.getFailureNode().getType() == k)
						continue;
					
					int maximumComplementPhi = Simulation.nodeMap.get(k).getRedundancy() - to.getComponentCount(k),
						supportedComplementPhi = maximumComplementPhi - curr.getPhiCount(k);
					for( int i = 0; i < supportedComplementPhi; i++ ){
						superRate *= 1 - Simulation.nodeMap.get(curr.getFIFOComplementPhi(k)).getRate(k);
						//System.out.println("Parent " + curr.getFIFOComplementPhi(k) + ", Child " + k + " => " + subRate);
					}
				}
				Simulation.qMatrix[f][t] += subTreeRate * superRate;
				
				if(verboseDebug) {
					System.out.println("Tree: \n" + curr + "\n");
					System.out.println(from.toLine() + " => " + to.toLine());
					System.out.println("n = " + n);
					System.out.println("lambda = " + lambda);
					System.out.println("complement-rate = " + superRate);
				}
				
			}
			
			for(TreeNode child : curr.getChildren()) {
				buildChildrenNodes(child, tempFailureTransition, subTreeRate);
			}
		}
	}
}
