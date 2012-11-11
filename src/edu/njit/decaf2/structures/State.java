/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.structures;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.Simulation;

/**
 * 
 * DECAF - State
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public class State extends DECAF implements Cloneable {
	private Map<String, Integer> vector;
	private int demand = -1;

	/**
	 * 
	 */
	public State() {
		super();
		vector = new HashMap<String, Integer>();
	}

	/**
	 * 
	 * @param components
	 * @param states
	 * @param demand
	 */
	public State(final List<Integer> states, final int demand) {
		super();
		int index = 0;
		vector = new HashMap<String, Integer>();
		for (String k : Simulation.typeList) {
			vector.put(k, states.get(index));
			index++;
		}
		this.demand = demand;
	}

	/**
	 * 
	 * @param vector
	 * @param demand
	 */
	public State(final Map<String, Integer> vector, final int demand) {
		super();
		this.vector = vector;
		this.demand = demand;
	}

	/**
	 * 
	 * @param type
	 * @param state
	 */
	public void addComponent(final String type, final int state) {
		vector.put(type, state);
	}

	/**
	 * 
	 * @param obj
	 * @return
	 */
	public State diff(final State obj) {
		final State result = new State();
		final Map<String, Integer> temp = obj.vector;
		for (String type : temp.keySet()) {
			result.addComponent(type, diffType(temp, type));
		}
		if (demand == obj.demand) {
			result.demand = obj.demand;
		}
		return result;
	}

	/**
	 * 
	 * @param temp
	 * @param type
	 * @return
	 */
	public int diffType(final Map<String, Integer> temp, final String type) {
		return temp.get(type) - vector.get(type);
	}

	/**
	 * 
	 * @param obj
	 * @param type
	 * @return
	 */
	public int diffType(final State obj, final String type) {
		return obj.vector.get(type) - vector.get(type);
	}

	/**
	 * 
	 * @param obj
	 * @return
	 */
	public State add(State obj) {
		if (obj.demand != demand) {
			return null;
		}

		final Map<String, Integer> temp = new HashMap<String, Integer>();
		for (String k : vector.keySet()) {
			temp.put(k, getComponentCount(k) + obj.getComponentCount(k));
		}
		return new State(temp, demand);
	}

	/**
	 * 
	 * @return
	 */
	public int sum() {
		if (demand == -1) {
			return 0;
		}

		int result = 0;
		for (String k : vector.keySet()) {
			result += vector.get(k);
		}
		return result;
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	public int getComponentCount(String type) {
		return vector.get(type);
	}

	/**
	 * 
	 * @param type
	 */
	public void incrementComponentCount(String type) {
		vector.put(type, vector.get(type) + 1);
	}

	/**
	 * 
	 * @param node
	 */
	public void incrementComponentCount(FailureNode node) {
		int currentCount = vector.get(node.getType());
		if( currentCount <= node.getRedundancy() ){
			vector.put(node.getType(), currentCount + 1);
		}
	}

	/**
	 * 
	 * @param type
	 * @param count
	 */
	public void updateComponentCount(String type, int count) {
		vector.put(type, count);
	}

	/**
	 * @return
	 */
	public boolean isMaxedTransition() {
		for (String k : vector.keySet()) {
			if( Simulation.nodeMap.get(k).getRedundancy() >= vector.get(k) ){
				return false;
			}
		}
		return true;
	}

	/**
	 * @return the vector
	 */
	public Map<String, Integer> getVector() {
		return vector;
	}

	/**
	 * @param vector
	 *            the vector to set
	 */
	public void setVector(Map<String, Integer> vector) {
		this.vector = vector;
	}

	/**
	 * @return the demand
	 */
	public int getDemand() {
		return demand;
	}

	/**
	 * @param demand
	 *            the demand to set
	 */
	public void setDemand(int demand) {
		this.demand = demand;
	}

	@Override
	public String toString() {
		final StringBuffer result = new StringBuffer(4096);
		for (String k : Simulation.typeList) {
			result.append("--");
			result.append(k);
			result.append("\t=>\t");
			result.append(vector.get(k));
			result.append('\n');
		}
		result.append("--Env\t=>\t");
		result.append(demand);
		return error(result.toString());
	}

	public String toLine() {
		final StringBuffer result = new StringBuffer("(");
		for (String k : Simulation.typeList) {
			result.append(vector.get(k));
			result.append(", ");
		}
		result.append(demand);
		result.append(')');
		return result.toString();
	}

	@Override
	public State clone() {
		final State result = new State();
		result.demand = demand - 0;
		final Map<String, Integer> tempVector = new HashMap<String, Integer>();
		tempVector.putAll(vector);
		result.vector = tempVector;
		return result;
	}

	@Override
	public int hashCode() {
		int result = Integer.valueOf(demand).hashCode();
		for (String k : vector.keySet()) {
			result += k.hashCode() + Integer.valueOf(vector.get(k)).hashCode();
		}
		return Integer.valueOf(result).hashCode();
	}


	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof State)) {
			return false;
		}

		final State other = (State) obj;

		if (demand != other.demand) {
			return false;
		}

		for (String k : vector.keySet()) {
			if (!other.vector.containsKey(k) || vector.get(k) != other.vector.get(k)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param truncationTransition
	 * @return
	 */
	public int compareTo(State b) {
		if (b.demand != demand) {
			return -2;
		}

		boolean flag = false;
		for (Entry<String, Integer> k : vector.entrySet()) {
			if (b.vector.get(k.getKey()) < k.getValue()) {
				return -1;
			} else if (b.vector.get(k.getKey()) > k.getValue()) {
				flag = true;
			}
		}

		return flag ? 1 : 0;
	}
}
