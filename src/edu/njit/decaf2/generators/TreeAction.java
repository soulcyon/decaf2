/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.concurrent.RecursiveAction;

import edu.njit.decaf2.Simulation;
import edu.njit.decaf2.structures.FailureNode;
import edu.njit.decaf2.structures.Point;
import edu.njit.decaf2.structures.State;

/**
 * DECAF - TreeCallable
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public class TreeAction extends RecursiveAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1898255913879356515L;
	Entry<String, FailureNode> entry;
	protected static List<StringBuffer> levels;

	public TreeAction(Entry<String, FailureNode> e) {
		entry = e;
	}

	/**
	 * 
	 */
	public TreeAction() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.RecursiveAction#compute()
	 */
	@Override
	protected void compute() {
		if (entry == null) {
			for (Entry<String, FailureNode> e : Simulation.nodeMap.entrySet()) {
				invokeAll(new TreeAction(e));
			}
			return;
		}

		levels = new ArrayList<StringBuffer>();
		levels.add(new StringBuffer("1:").append(entry.getKey()));

		final State initialFT = (State) Simulation.states[0].clone();
		initialFT.incrementComponentCount(entry.getValue());

		final Map<String, List<String>> bfhMap = buildBFH();
		bfhMap.get(entry.getKey()).add("|");

		growSubTree(levels, initialFT, 1.0, bfhMap);
	}

	/**
	 * 
	 * @param levels
	 * @param failureTransition
	 * @param subRate
	 * @param bfhMap
	 */
	protected static void growSubTree(final List<StringBuffer> levels, final State failureTransition,
			final double subTreeRate, final Map<String, List<String>> bfhMap) {
		final StringTokenizer terminalNodes = new StringTokenizer(levels.get(levels.size() - 1).toString(), ",");
		final ArrayList<Integer> gammaPermutations = new ArrayList<Integer>();
		final ArrayList<String> terminalTypes = new ArrayList<String>();

		while (terminalNodes.hasMoreElements()) {
			final String terminalNode = terminalNodes.nextToken();
			final String type = terminalNode.substring(terminalNode.indexOf(':') + 1);
			if (terminalNode.charAt(0) == '1' && TreeGenerator.binaryEnumCache.containsKey(type)) {
				gammaPermutations.add(TreeGenerator.binaryEnumCache.get(type).size());
				terminalTypes.add(type);
			}
		}

		List<List<Integer>> productSet = new ArrayList<List<Integer>>();
		if( TreeGenerator.productCache.containsKey(gammaPermutations.hashCode()) ){
			productSet = TreeGenerator.productCache.get(gammaPermutations.hashCode());
		} else {
			cartesianProduct(gammaPermutations, 0, new ArrayList<Integer>(gammaPermutations.size()), productSet);
		}
		TreeGenerator.productCache.put(gammaPermutations.hashCode(), productSet);
		
		nextLevel: for (int c = 0; c < productSet.size(); c++) {
			final State ftCopy = failureTransition.clone();
			final Map<String, List<String>> bfhCopy = buildBFH(bfhMap);
			final List<Integer> breadthEncoding = productSet.get(c);

			double subTreeRateCopy = subTreeRate;
			final StringBuffer newLevel = new StringBuffer();

			for (int b = 0; b < breadthEncoding.size(); b++) {
				final String parentType = terminalTypes.get(b);
				final FailureNode parentFailureNode = Simulation.nodeMap.get(parentType);
				final int binEnumId = breadthEncoding.get(b);
				final String block = TreeGenerator.binaryEnumCache.get(parentType).get(binEnumId) + ",";

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
				final List<StringBuffer> levelsCopy = new ArrayList<StringBuffer>(levels);
				levelsCopy.add(new StringBuffer(newLevel.substring(0, newLevel.length() - 1)));
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
	private static void processRates(final List<StringBuffer> levels, final State failureTransition,
			final Map<String, List<String>> bfhCopy, final double subTreeRate) {
		
		final List<Point> likeTransitions = QMatrixGenerator.likeTransitionMap.get(failureTransition);
		
		for (Point transition : likeTransitions) {
			final int fIndex = transition.getX();
			final int tIndex = transition.getY();
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
			Simulation.qmatrix.setQuick(fIndex, tIndex, Simulation.qmatrix.getQuick(fIndex, tIndex)
					+ (rootRate * subTreeRate * complementRate));
			Simulation.numberOfTrees++;
		}
	}

	/**
	 * 
	 * @return
	 */
	protected static Map<String, List<String>> buildBFH() {
		final Map<String, List<String>> result = new HashMap<String, List<String>>();
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
	private static Map<String, List<String>> buildBFH(final Map<String, List<String>> clone) {
		final Map<String, List<String>> result = new HashMap<String, List<String>>();
		for (Entry<String, List<String>> entry : clone.entrySet()) {
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
			final List<List<Integer>> list) {

		if (current.size() == limits.size()) {
			list.add(current);
		}

		if (index >= limits.size()) {
			return;
		}

		for (int i = 0; i < limits.get(index); i++) {
			final List<Integer> currentCopy = new ArrayList<Integer>(current);
			currentCopy.add(i);
			cartesianProduct(limits, index + 1, currentCopy, list);
		}
	}
}
