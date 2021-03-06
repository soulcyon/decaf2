/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javolution.util.FastMap;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.Simulation;
import edu.njit.decaf2.structures.DelegateStore;
import edu.njit.decaf2.structures.FailureNode;
import edu.njit.decaf2.structures.Point;
import edu.njit.decaf2.structures.State;
import edu.njit.decaf2.structures.PowerSetComparator;

/**
 * 
 * DECAF - TreeGeneratorUnthreaded
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public final class TreeGeneratorUnthreaded extends DECAF {
	public static Map<String, List<String>> binaryEnumCache;
	public static Map<String, ArrayList<DelegateStore>> delegateCallStores;
	private static int nextRoot = 0;
	
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
		binaryEnumCache = new FastMap<String, List<String>>();
		delegateCallStores = new FastMap<String, ArrayList<DelegateStore>>();
		
		for (String type : Simulation.nodeMap.keySet()) {
			final Map<String, Double> gamma = Simulation.nodeMap.get(type).getCascadingFailures();
			if (!gamma.isEmpty()) {
				binaryEnumCache.put(type, powerSet(gamma.keySet(), type));
			}
			delegateCallStores.put(type, new ArrayList<DelegateStore>());
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
		triggerDelegation();
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
		ArrayList<ArrayList<Integer>> productSet = new ArrayList<ArrayList<Integer>>();
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

			// ---Approximation conditions---
			if (levels.size() > DECAF.heightThreshold) {
				//if (DECAF.biasType.equals("high") || DECAF.biasType.equals("hybrid"))
				//	forceRates(levels, failureTransition, bfhCopy, subTreeRate);
				continue nextLevel;
			}

			if (subTreeRate < DECAF.rateThreshold) {
				//if (DECAF.biasType.equals("high") || DECAF.biasType.equals("hybrid"))
				//	forceRates(levels, failureTransition, bfhCopy, subTreeRate);
				continue nextLevel;
			}

			if (failureTransition.sum() > DECAF.nodeThreshold) {
				//if (DECAF.biasType.equals("high") || DECAF.biasType.equals("hybrid"))
				//	forceRates(levels, failureTransition, bfhCopy, subTreeRate);
				continue nextLevel;
			}
			
			if( Simulation.numberOfUniqueTrees > DECAF.treeThreshold ){
				return;
			}

			// -------------------------------
			if (c > 0) {
				final ArrayList<String> levelsCopy = new ArrayList<String>(levels);
				levelsCopy.add(newLevel.substring(0, newLevel.length() - 1));
				
				//(levelsCopy, ftCopy, subTreeRateCopy, bfhCopy);
				delegate(new DelegateStore(levelsCopy, ftCopy, subTreeRateCopy, bfhCopy));
			} else {
				processRates(levels, failureTransition, bfhCopy, subTreeRate);
			}
		}
	}

	/**
	 * 
	 * @param delegateStore
	 */
	private static void delegate(DelegateStore delegateStore) {
		delegateCallStores.get(delegateStore.root).add(delegateStore);
	}
	
	private static void triggerDelegation(){
		while(true){
			double max = 0;
			int t = 0;
			String o = "";
			DelegateStore next = null;
	
			for( Entry<String, ArrayList<DelegateStore>> k : delegateCallStores.entrySet() ){
				int i = 0;
				for( DelegateStore value : k.getValue()){
					if( value.rate > max ){
						max = value.rate;
						next = value;
						t = i;
						o = k.getKey();
					}
					i++;
				}
			}
			if( next != null ){
				delegateCallStores.get(o).remove(t);
				growSubTree(next.levels, next.ft, next.rate, next.bfhMap);
			} else {
				break;
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
		for( int i = 0; i < Simulation.demandMatrix.length; i++ ){
			failureTransition.setDemand(i);
			final List<Point> likeTransitions = QMatrixGeneratorUnthreaded.likeTransitionMap.get(failureTransition);
			Simulation.numberOfUniqueTrees++;
			for (Point transition : likeTransitions) {
				final int fIndex = transition.getX();
				final int tIndex = transition.getY();
				final State from = Simulation.states[fIndex];
				final double currentRate = Simulation.qmatrix.getQuick(fIndex, tIndex);
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
				Simulation.qmatrix.setQuick(fIndex, tIndex, currentRate + (rootRate * subTreeRate * complementRate));
				Simulation.numberOfTrees++;
			}
		}
	}

	private static void forceRates(final List<String> truncatedLevels, final State truncationTransition,
			final Map<String, ArrayList<String>> truncationBfh, final double partialSubTreeRate) {

		for (State diff : QMatrixGeneratorUnthreaded.likeTransitionMap.keySet()) {

			if (diff.compareTo(truncationTransition) <= 0)
				continue;

			final List<Point> likeTransitions = QMatrixGeneratorUnthreaded.likeTransitionMap.get(diff);

			for (Point transition : likeTransitions) {
				final int fIndex = transition.getX();
				final int tIndex = transition.getY();
				final State from = Simulation.states[fIndex];
				final double currentRate = Simulation.qmatrix.getQuick(fIndex, tIndex);

				double rootRate = 1.0;
				double complementRate = 1.0;

				if (DECAF.completeTreeRate) {
					final String rootType = truncatedLevels.get(0).substring(truncatedLevels.get(0).indexOf(":") + 1);
					final FailureNode root = Simulation.nodeMap.get(rootType);
					final int nAlgo = root.getRedundancy() - from.getComponentCount(rootType);
					final double lambda = root.getFailureRates()[from.getDemand()];
					rootRate = nAlgo * lambda;

					for (Entry<String, FailureNode> entry : Simulation.nodeMap.entrySet()) {
						int compsAvailable = entry.getValue().getRedundancy() - from.getComponentCount(entry.getKey());
						for (String s : truncationBfh.get(entry.getKey())) {
							if ("|".equals(s)) {
								--compsAvailable;
							} else if (compsAvailable > 0) {
								complementRate *= 1 - Simulation.nodeMap.get(s).getRate(entry.getKey());
							} else {
								break;
							}
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
				Simulation.qmatrix.setQuick(fIndex, tIndex, currentRate
						+ (rootRate * partialSubTreeRate * complementRate));
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	private static Map<String, ArrayList<String>> buildBFH() {
		final Map<String, ArrayList<String>> result = new FastMap<String, ArrayList<String>>();
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
		final Map<String, ArrayList<String>> result = new FastMap<String, ArrayList<String>>();
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
	public static void cartesianProduct(final List<Integer> limits, final int index, final List<Integer> current,
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
	private static List<String> powerSet(final Set<String> set, final String parentType) {

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
		String temp = binaryEnum.remove(0);
		Collections.sort(binaryEnum, new PowerSetComparator(parentType));
		ArrayList<String> result = new ArrayList<String>();
		result.add(temp);
		result.addAll(binaryEnum);
		return result;
	}
}
