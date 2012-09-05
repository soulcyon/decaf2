/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.Simulation;
import edu.njit.decaf2.data.FailureNode;
import edu.njit.decaf2.data.State;

/**
 * DECAF 2 - TreeGenerator
 * 
 * @author Mihir Sanghavi
 * @version 2.0
 * 
 */

public class TreeGeneratorUnthreaded extends DECAF {

	private static HashMap<String, ArrayList<String>> binaryEnumCache;

	public static void initSubTrees() {

		// build binaryEnumCache
		binaryEnumCache = new HashMap<String, ArrayList<String>>();

		for (String type : Simulation.nodeMap.keySet()) {

			// build binaryEnumCache
			FailureNode fn = Simulation.nodeMap.get(type);
			HashMap<String, Double> gamma = fn.getCascadingFailures();

			if (gamma.size() > 0) {
				binaryEnumCache.put(type, powerSet(gamma.keySet()));
			}
		}

		printBinaryEnumCache();

		// plant trees using all types
		for (String rootType : Simulation.nodeMap.keySet()) {

			// build binaryEnumCache
			FailureNode fn = Simulation.nodeMap.get(rootType);
			HashMap<String, Double> gamma = fn.getCascadingFailures();

			if (gamma.size() > 0) {
				binaryEnumCache.put(rootType, powerSet(gamma.keySet()));
			}

			// initialize associated parameters
			ArrayList<String> levels = new ArrayList<String>();
			levels.add("1:" + rootType);

			State initFailureTransition = (State) Simulation.states[0].clone();
			initFailureTransition.incrementComponentCount(Simulation.nodeMap.get(rootType));

			HashMap<String, ArrayList<String>> breadthFirstHistory = new HashMap<String, ArrayList<String>>();
			for (String k : Simulation.nodeMap.keySet())
				breadthFirstHistory.put(k, new ArrayList<String>());
			breadthFirstHistory.get(rootType).add("|");

			GrowSubTree(levels, initFailureTransition, 1.0, breadthFirstHistory);
		}
	}

	/**
	 * 
	 * @param set
	 * @return
	 */

	private static ArrayList<String> powerSet(Set<String> set) {

		ArrayList<String> members = new ArrayList<String>(set);

		if (members.size() == 0)
			return new ArrayList<String>();

		int permutations = (int) Math.pow(2, members.size());
		ArrayList<String> binaryEnum = new ArrayList<String>(permutations);

		for (int p = 0; p < permutations; p++) {

			String binary = Integer.toBinaryString(p);
			while (binary.length() < members.size())
				binary = "0" + binary;
			
			String block = "";
			for (int b = 0; b < binary.length(); b++) {
				block += binary.charAt(b) + ":" + members.get(b) + ",";
			}
			block = block.substring(0, block.length() - 1);
			binaryEnum.add(p, block);
		}

		return binaryEnum;
	}

	/**
	 * 
	 * @param levels
	 * @param failureTransition
	 * @param subRate
	 * @param breadthFirstHistory
	 */

	private static void GrowSubTree(ArrayList<String> levels, State failureTransition, double subTreeRate,
			HashMap<String, ArrayList<String>> breadthFirstHistory) {

		// identify growth locations
		String[] terminalNodes = levels.get(levels.size() - 1).split(",");
		ArrayList<Integer> gammaPermutations = new ArrayList<Integer>();
		ArrayList<String> terminalTypes = new ArrayList<String>();

		// determine how many growth possibilities exist
		for (int t = 0; t < terminalNodes.length; t++) {
			String terminalNode = terminalNodes[t];
			String type = terminalNode.substring(terminalNode.indexOf(":") + 1);
			if (terminalNode.charAt(0) == '1' && binaryEnumCache.containsKey(type)) {
				gammaPermutations.add(binaryEnumCache.get(type).size());
				terminalTypes.add(type);
			}
		}

		// fork by different growth possibilities
		ArrayList<ArrayList<Integer>> cartesianProductEnum = new ArrayList<ArrayList<Integer>>();
		cartesianProduct(gammaPermutations, 0, new ArrayList<Integer>(gammaPermutations.size()), cartesianProductEnum);
		
		// nextLevel label is a $500 solution - copyright Mihir Sanghavi
		nextLevel:
		for (int c = 0; c < cartesianProductEnum.size(); c++) {
			
			// make copies of reference types to prevent data persistence over
			// mutually exclusive recursive calls
			ArrayList<String> levelsCopy = new ArrayList<String>(levels);

			State failureTransitionCopy = failureTransition.clone();

			HashMap<String, ArrayList<String>> breadthFirstHistoryCopy = new HashMap<String, ArrayList<String>>();
			for (String key : breadthFirstHistory.keySet()) {
				ArrayList<String> compHistory = new ArrayList<String>(breadthFirstHistory.get(key));
				breadthFirstHistoryCopy.put(key, compHistory);
			}
			
			double subTreeRateCopy = subTreeRate;

			// add one possible new level
			ArrayList<Integer> breadthEncoding = cartesianProductEnum.get(c);
			String newLevel = "";

			// go through all added nodes, denoted by 1:type
			for (int b = 0; b < breadthEncoding.size(); b++) {

				String parentType = terminalTypes.get(b);
				FailureNode parentFailureNode = Simulation.nodeMap.get(parentType);
				int binEnumId = breadthEncoding.get(b);
				String block = binaryEnumCache.get(parentType).get(binEnumId);
				newLevel += block + ","; 

				// go through each of the added nodes' children
				String[] gammaStatus = block.split(",");

				for (int g = 0; g < gammaStatus.length; g++) {

					String childInfo = gammaStatus[g];
					String childType = childInfo.substring(childInfo.indexOf(":") + 1);

					// update failureTransition and subRate, breadthFirstHistory
					// for tree
					if (childInfo.charAt(0) == '1') {
						failureTransitionCopy.incrementComponentCount(childType);
						breadthFirstHistoryCopy.get(childType).add("|");
						subTreeRateCopy *= parentFailureNode.getRate(childType);
					} else {
						breadthFirstHistoryCopy.get(childType).add(parentType);
					}
				}
			}

			// base case - break out if tree is invalid i.e. if it has more
			// component types than redundancy
			for (String type : Simulation.nodeMap.keySet()) {
				FailureNode fn = Simulation.nodeMap.get(type);
				if (failureTransitionCopy.getComponentCount(type) > fn.getRedundancy()) {
					continue nextLevel;
				}
			}

			if (c > 0) {
				newLevel = newLevel.substring(0, newLevel.length() - 1);
				levelsCopy.add(newLevel);
				GrowSubTree(levelsCopy, failureTransitionCopy, subTreeRateCopy, breadthFirstHistoryCopy);
			} else {
				// Iterate through all likeTransitions to which this tree applies
				ArrayList<String> likeTransitions = QMatrixGeneratorUnthreaded.likeTransitionMap.get(failureTransition);

				for (String transition : likeTransitions) {

					String[] fromAndTo = transition.split(",");
					int f = Integer.parseInt(fromAndTo[0]);
					int t = Integer.parseInt(fromAndTo[1]);
					State from = Simulation.states[f];

					String rootType = levels.get(0).substring(levels.get(0).indexOf(":") + 1);
					FailureNode root = Simulation.nodeMap.get(rootType);

					// n * lambda for root of tree
					int n = root.getRedundancy() - from.getComponentCount(rootType);
					double lambda = root.getFailureRates()[from.getDemand()];
					double rootRate = n * lambda;
					double complementRate = 1.0;

					// Iterate through BreadthFirstHistory to calculate
					// complementRate
					for (String k : Simulation.nodeMap.keySet()) {
						int compsAvailable = Simulation.nodeMap.get(k).getRedundancy() - from.getComponentCount(k);
						ArrayList<String> couldHaveFailed = breadthFirstHistoryCopy.get(k);

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

					if (verboseDebug /*&& f == 0 && t == 47*/) {
						printAllLevels(levels);
						System.out.println("From:\t" + f + " => " + from.toLine());
						System.out.println("To:\t" + t + " => " + Simulation.states[t].toLine());
						System.out.println("Failure Transition:" + failureTransition.toLine());
						System.out.println("n:\t" + n);
						System.out.println("Lambda:\t" + lambda);
						System.out.println("Root Rate:\t" + rootRate);
						System.out.println("Subtree Rate:\t" + subTreeRate);
						System.out.println("Supertree Rate:\t" + complementRate);
						System.out.println("BFHistory:\t" + breadthFirstHistoryCopy);
						System.out.println("Rate: \t" + Simulation.qMatrix[f][t] + " + "
								+ (rootRate * subTreeRate * complementRate) + "\n\n");
					}

					// populate Q
					Simulation.qMatrix[f][t] += rootRate * subTreeRate * complementRate;
				}
			}
		}
	}

	/**
	 * 
	 * @param levels
	 */
	private static void printAllLevels(ArrayList<String> levels) {

		System.out.println("______________________________________________________");
		for (String level : levels)
			System.out.println(level);
	}

	/**
	 * 
	 * @param levels
	 * @param indent
	 * @param nodeType
	 */

	private static void printBinaryEnumCache() {

		System.out.println("Cache:");
		for (String key : binaryEnumCache.keySet()) {
			System.out.print(key + " => ");

			ArrayList<String> values = binaryEnumCache.get(key);
			for (int i = 0; i < values.size(); i++) {
				System.out.print(values.get(i) + " | ");
			}

			System.out.println();
		}
		System.out.println();
	}

	/**
	 * 
	 * @param limits
	 * @param x
	 * @param current
	 * @param list
	 */
	private static void cartesianProduct(ArrayList<Integer> limits, int x, ArrayList<Integer> current,
			ArrayList<ArrayList<Integer>> list) {

		if (current.size() == limits.size()) {
			list.add(current);
		}

		if (x >= limits.size())
			return;

		for (int i = 0; i < limits.get(x); i++) {
			ArrayList<Integer> currentCopy = new ArrayList<Integer>(current);
			currentCopy.add(i);
			cartesianProduct(limits, x + 1, currentCopy, list);
		}
	}
}
