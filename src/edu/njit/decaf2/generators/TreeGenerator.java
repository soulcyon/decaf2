package edu.njit.decaf2.generators;

import java.util.HashMap;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.data.FailureNode;
import edu.njit.decaf2.data.FailureTree;
import edu.njit.decaf2.data.State;

/**
 * ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |_____  |     | |            __|__ __|__
 *
 * @author Sashank Tadepalli
 *
 */
public class TreeGenerator extends DECAF {
	private HashMap<String, FailureTree> 	cache = new HashMap<String, FailureTree>();
	private HashMap<String, FailureNode>	nodeM = new HashMap<String, FailureNode>();
	private int								misses = 0;
	
	/**
	 * @param decaf_nodeMap
	 */
	public TreeGenerator(HashMap<String, FailureNode> nodeM) {
		this.nodeM = nodeM;
	}

	/**
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public double getFailureRate(State from, State to){
		if( cache.containsKey(from.diff(to) + "") ){
			misses++;
		} else {
			cache.put(from.diff(to) + "", new FailureTree(nodeM.get(0)));
		}
		return -1.0;
	}

	/**
	 * @return the nodeM
	 */
	public HashMap<String, FailureNode> getNodeM() {
		return nodeM;
	}
	
	/**
	 * @return the cache
	 */
	public HashMap<String, FailureTree> getCache() {
		return cache;
	}

	/**
	 * @return the misses
	 */
	public int getMisses() {
		return misses;
	}
}
