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
		for (String root : Simulation.nodeMap.keySet()) {
			TreeNode ft = new TreeNode(Simulation.nodeMap.get(root));
			ft.makeRoot();

			HashMap<String, ArrayList<String>> breadthFirstHistory = new HashMap<String, ArrayList<String>>();

			for (String k : Simulation.nodeMap.keySet())
				breadthFirstHistory.put(k, new ArrayList<String>());

			breadthFirstHistory.get(root).add("|");
			State failureTransition = (State) Simulation.states[0].clone();
			failureTransition.incrementComponentCount(Simulation.nodeMap.get(root));
			buildSubTree(ft, 1.0, breadthFirstHistory, failureTransition);
		}
	}

	/**
	 * Builds a larger tree by attaching nodes.
	 * 
	 * @param root
	 * @param transition
	 * @param rate
	 * @return
	 */
	private static void buildSubTree(TreeNode curr, double subTreeRate,
			HashMap<String, ArrayList<String>> breadthFirstHistory, State failureTransition) {
		HashMap<String, Double> gamma = curr.getFailureNode().getCascadingFailures();
		int gammaLength = gamma.size();
		for (int g = 0; g < (int) Math.pow(2, gammaLength); g++) {

			// Properly clones HashMap by cloning internal ArrayLists, putAll
			// Fails because vales are a non-primitive type
			HashMap<String, ArrayList<String>> tempHistory = new HashMap<String, ArrayList<String>>();
			for (String key : breadthFirstHistory.keySet()) {
				ArrayList<String> compHistory = new ArrayList<String>(breadthFirstHistory.get(key));
				tempHistory.put(key, compHistory);
			}
			
			// If curr has no gamma, then do not assign gInBinary
			String gInBinary = "";
			if (gammaLength > 0) {
				gInBinary = String.format("%" + gammaLength + "s", Integer.toBinaryString(g)).replace(' ', '0');
			}

			for (int b = 0; b < gInBinary.length(); b++) {
				String[] entriesInGamma = new String[gammaLength];
				entriesInGamma = gamma.keySet().toArray(entriesInGamma);
				FailureNode triggerComponent = Simulation.nodeMap.get(entriesInGamma[b]);

				if (!(failureTransition.getComponentCount(entriesInGamma[b]) < triggerComponent.getRedundancy()))
					continue;

				if (gInBinary.charAt(b) == '1') {
					curr.addChild(triggerComponent);
					failureTransition.incrementComponentCount(triggerComponent);
					subTreeRate *= curr.getFailureNode().getRate(entriesInGamma[b]);
					tempHistory.get(entriesInGamma[b]).add("|");
				} else {
					tempHistory.get(entriesInGamma[b]).add(curr.getFailureNode().getType());
				}
			}

			for (TreeNode child : curr.getChildren()) {
				System.out.println(curr);
				buildSubTree(child, subTreeRate, tempHistory, failureTransition.clone());
			}
			
			// We still need gInBinary to be "0" for qMatrix calculation
			// even if curr has no gamma
			if (gammaLength == 0) {
				gInBinary = "0";
			}

			ArrayList<String> likeTransitions = QMatrixGeneratorUnthreaded.likeTransitionMap.get(failureTransition);

			// Iterate through all likeTransitions
			for (String transition : likeTransitions) {
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

				// Iterate through all nodes, calculate complementRate
				for (String k : Simulation.nodeMap.keySet()) {
					int compsAvailable = Simulation.nodeMap.get(k).getRedundancy() - from.getComponentCount(k);
					ArrayList<String> couldHaveFailed = tempHistory.get(k);

					for (int i = 0; i < couldHaveFailed.size(); i++) {
						String s = couldHaveFailed.get(i);

						if (s.equals("|"))
							--compsAvailable;

						else if (compsAvailable > 0)
							complementRate *= 1 - Simulation.nodeMap.get(s).getRate(k);

						else
							break;
					}
				}

				if (verboseDebug/* && f == debugX && t == debugY */) {
					/*System.out.println("========================\nDifference Transition " + failureTransition.toLine());
					System.out.println("\nTree:\n" + curr + "\n");
					System.out.println("Root Rate:\t" + rootRate);
					System.out.println("Subtree Rate:\t" + subTreeRate);
					System.out.println("Supertree Rate:\t" + complementRate);
					System.out.println("BFHistory:\t" + tempHistory);
					System.out.println("Rate: \t" + Simulation.qMatrix[f][t] + " + "
							+ (rootRate * subTreeRate * complementRate) + "\n\n");*/
				}
				if (gInBinary.equals("0")) {
					Simulation.qMatrix[f][t] += rootRate * subTreeRate * complementRate;
				}
			}
		}
	}
}