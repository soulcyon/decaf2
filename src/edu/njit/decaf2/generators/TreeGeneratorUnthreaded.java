/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.Simulation;
import edu.njit.decaf2.structures.FailureNode;
import edu.njit.decaf2.structures.QMatrix;
import edu.njit.decaf2.structures.State;

/**
 * 
 * DECAF - TreeGeneratorUnthreaded
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public final class TreeGeneratorUnthreaded extends DECAF {
	private static Map<String, List<String>> binaryEnumCache;

	/**
	 * 
	 */
	private TreeGeneratorUnthreaded() {
		super();
	}

	/**
	 * 
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public static void initSubTrees() {
		binaryEnumCache = new HashMap<String, List<String>>();

		for (String type : Simulation.nodeMap.keySet()) {
			final Map<String, Double> gamma = Simulation.nodeMap.get(type).getCascadingFailures();
			if (!gamma.isEmpty()) {
				binaryEnumCache.put(type, powerSet(gamma.keySet()));
			}
		}

		ArrayList<String> levels;
		for (Entry<String, FailureNode> entry : Simulation.nodeMap.entrySet()) {
			levels = new ArrayList<String>();
			levels.add("1:" + entry.getKey());

			final State initialFT = (State) Simulation.states[0].clone();
			initialFT.incrementComponentCount(entry.getValue());

			final Map<String, ArrayList<String>> bfhMap = buildBFH();
			bfhMap.get(entry.getKey()).add("|");

			growSubTree(levels, initialFT, 1.0, bfhMap);
		}
	}

	/**
	 * 
	 * @param levels
	 * @param failureTransition
	 * @param subRate
	 * @param bfhMap
	 */
	private static void growSubTree(final List<String> levels, final State failureTransition, final double subTreeRate,
			final Map<String, ArrayList<String>> bfhMap) {

		final String[] terminalNodes = levels.get(levels.size() - 1).split(",");
		final ArrayList<Integer> gammaPermutations = new ArrayList<Integer>();
		final ArrayList<String> terminalTypes = new ArrayList<String>();

		for (int t = 0; t < terminalNodes.length; t++) {
			final String terminalNode = terminalNodes[t];
			final String type = terminalNode.substring(terminalNode.indexOf(':') + 1);
			if (terminalNode.charAt(0) == '1' && binaryEnumCache.containsKey(type)) {
				gammaPermutations.add(binaryEnumCache.get(type).size());
				terminalTypes.add(type);
			}
		}

		final ArrayList<ArrayList<Integer>> productSet = new ArrayList<ArrayList<Integer>>();
		cartesianProduct(gammaPermutations, 0, new ArrayList<Integer>(gammaPermutations.size()), productSet);

		nextLevel: for (int c = 0; c < productSet.size(); c++) {
			final State ftCopy = failureTransition.clone();
			final Map<String, ArrayList<String>> bfhCopy = buildBFH(bfhMap);
			final ArrayList<Integer> breadthEncoding = productSet.get(c);

			double subTreeRateCopy = subTreeRate;
			final StringBuffer newLevel = new StringBuffer();

			for (int b = 0; b < breadthEncoding.size(); b++) {
				final String parentType = terminalTypes.get(b);
				final FailureNode parentFailureNode = Simulation.nodeMap.get(parentType);
				final int binEnumId = breadthEncoding.get(b);
				final String block = binaryEnumCache.get(parentType).get(binEnumId) + ",";

				newLevel.append(block);
				final String[] gammaStatus = block.split(",");

				for (int g = 0; g < gammaStatus.length; g++) {
					final String childInfo = gammaStatus[g];
					final String childType = childInfo.substring(childInfo.indexOf(':') + 1);

					// update failureTransition and subRate, breadthFirstHistory
					// for tree
					if (childInfo.charAt(0) == '1') {
						ftCopy.incrementComponentCount(childType);
						bfhCopy.get(childType).add("|");
						subTreeRateCopy *= parentFailureNode.getRate(childType);
					} else {
						bfhCopy.get(childType).add(parentType);
					}
				}
			}

			for (String type : Simulation.nodeMap.keySet()) {
				if (ftCopy.getComponentCount(type) > Simulation.nodeMap.get(type).getRedundancy()) {
					continue nextLevel;
				}
			}

			if (c > 0) {
				final ArrayList<String> levelsCopy = new ArrayList<String>(levels);
				levelsCopy.add(newLevel.substring(0, newLevel.length() - 1));
				growSubTree(levelsCopy, ftCopy, subTreeRateCopy, bfhCopy);
			} else {
				processRates(levels, failureTransition, bfhCopy, subTreeRate);
			}
		}
	}

	/**
	 * 
	 * @param levels
	 * @param failureTransition
	 * @param bfhCopy
	 * @param subTreeRate
	 */
	private static void processRates(final List<String> levels, final State failureTransition,
			final Map<String, ArrayList<String>> bfhCopy, final double subTreeRate) {
		final ArrayList<String> likeTransitions = QMatrixGeneratorUnthreaded.likeTransitionMap.get(failureTransition);
		for (String transition : likeTransitions) {
			final String[] fromAndTo = transition.split(",");
			final int fIndex = Integer.parseInt(fromAndTo[0]);
			final int tIndex = Integer.parseInt(fromAndTo[1]);
			final State from = Simulation.states[fIndex];

			final String rootType = levels.get(0).substring(levels.get(0).indexOf(":") + 1);
			final FailureNode root = Simulation.nodeMap.get(rootType);

			final int nAlgo = root.getRedundancy() - from.getComponentCount(rootType);
			final double lambda = root.getFailureRates()[from.getDemand()];
			final double rootRate = nAlgo * lambda;
			double complementRate = 1.0;

			for (Entry<String, FailureNode> entry : Simulation.nodeMap.entrySet()) {
				int compsAvailable = entry.getValue().getRedundancy() - from.getComponentCount(entry.getKey());
				for (String s : bfhCopy.get(entry.getKey())) {
					if ("|".equals(s)) {
						--compsAvailable;
					} else if (compsAvailable > 0) {
						complementRate *= 1 - Simulation.nodeMap.get(s).getRate(entry.getKey());
					} else {
						break;
					}
				}
			}

			/*
			 * if (verboseDebug) { printAllLevels(levels);
			 * System.out.println("From:\t" + f + " => " + from.toLine());
			 * System.out.println("To:\t" + t + " => " +
			 * Simulation.states[t].toLine());
			 * System.out.println("Failure Transition:" +
			 * failureTransition.toLine()); System.out.println("n:\t" + n);
			 * System.out.println("Lambda:\t" + lambda);
			 * System.out.println("Root Rate:\t" + rootRate);
			 * System.out.println("Subtree Rate:\t" + subTreeRate);
			 * System.out.println("Supertree Rate:\t" + complementRate);
			 * System.out.println("BFHistory:\t" + breadthFirstHistoryCopy);
			 * System.out.println("Rate: \t" + (rootRate * subTreeRate *
			 * complementRate) + "\n\n"); }
			 */

			QMatrix.update(fIndex, tIndex, rootRate * subTreeRate * complementRate);
			Simulation.numberOfTrees++;
		}
	}

	/**
	 * 
	 * @return
	 */
	private static Map<String, ArrayList<String>> buildBFH() {
		final HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
		for (String k : Simulation.nodeMap.keySet()) {
			result.put(k, new ArrayList<String>());
		}
		return result;
	}

	/**
	 * 
	 * @param clone
	 * @return
	 */
	private static Map<String, ArrayList<String>> buildBFH(final Map<String, ArrayList<String>> clone) {
		final HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
		for (Entry<String, ArrayList<String>> entry : clone.entrySet()) {
			result.put(entry.getKey(), new ArrayList<String>(entry.getValue()));
		}
		return result;
	}

	/**
	 * 
	 * @param limits
	 * @param x
	 * @param current
	 * @param list
	 */
	private static void cartesianProduct(final List<Integer> limits, final int index, final List<Integer> current,
			final List<ArrayList<Integer>> list) {

		if (current.size() == limits.size()) {
			list.add((ArrayList<Integer>) current);
		}

		if (index >= limits.size()) {
			return;
		}

		for (int i = 0; i < limits.get(index); i++) {
			final ArrayList<Integer> currentCopy = new ArrayList<Integer>(current);
			currentCopy.add(i);
			cartesianProduct(limits, index + 1, currentCopy, list);
		}
	}

	/**
	 * 
	 * @param set
	 * @return
	 */
	private static List<String> powerSet(final Set<String> set) {

		final ArrayList<String> members = new ArrayList<String>(set);

		if (members.isEmpty()) {
			return new ArrayList<String>();
		}

		final int permutations = (int) Math.pow(2, members.size());
		final ArrayList<String> binaryEnum = new ArrayList<String>(permutations);

		for (int p = 0; p < permutations; p++) {

			final StringBuffer binary = new StringBuffer(String.format("%" + members.size() + "s",
					Integer.toBinaryString(p)).replace(' ', '0'));
			final StringBuffer block = new StringBuffer();
			for (int b = 0; b < binary.length(); b++) {
				block.append(binary.charAt(b) + ":" + members.get(b) + ",");
			}
			binaryEnum.add(p, block.substring(0, block.length() - 1));
		}

		return binaryEnum;
	}
}
