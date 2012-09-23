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

/**
 * 
 * DECAF - TreeGenerator
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public final class TreeGenerator extends DECAF {
	protected static Map<String, List<String>> binaryEnumCache;
	protected static Map<Integer, List<List<Integer>>> productCache;

	/**
	 * 
	 */
	private TreeGenerator() {
		super();
	}

	/**
	 * 
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/**
	 * 
	 */
	public static void initSubTrees() {
		productCache = new HashMap<Integer, List<List<Integer>>>();
		// Build binary enumeration cache once
		binaryEnumCache = new HashMap<String, List<String>>();

		for (Entry<String, FailureNode> entry : Simulation.nodeMap.entrySet()) {
			final Map<String, Double> gamma = Simulation.nodeMap.get(entry.getKey()).getCascadingFailures();
			if (!gamma.isEmpty()) {
				binaryEnumCache.put(entry.getKey(), powerSet(gamma.keySet()));
			}
		}

		DECAF.threadPool.invoke(new TreeAction());
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
