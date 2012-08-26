/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.data;

import java.util.HashMap;
import java.util.Set;

import edu.njit.decaf2.DECAF;

/**
 * DECAF 2 - State
 * 
 * @author Sashank Tadepalli
 * @version 2.0
 * 
 */
public class State extends DECAF {
	private HashMap<String, Integer> vector;
	private int demand = -1;

	/**
	 * 
	 */
	public State() {
		vector = new HashMap<String, Integer>();
	}

	/**
	 * 
	 * @param components
	 * @param states
	 * @param demand
	 */
	public State(Set<String> components, String states, int demand) {
		int i = 0;
		vector = new HashMap<String, Integer>();
		for (String k : components) {
			vector.put(k, Integer.parseInt(states.substring(i, i + 1)));
			i++;
		}
		this.demand = demand;
	}

	/**
	 * 
	 * @param components
	 * @param states
	 */
	public State(Set<String> components, String states) {
		this(components, states, 0);
	}

	/**
	 * 
	 * @param vector
	 */
	public State(HashMap<String, Integer> vector) {
		this.vector = vector;
	}

	/**
	 * 
	 * @param type
	 * @param state
	 */
	public State(String type, int state) {
		vector = new HashMap<String, Integer>();
		vector.put(type, state);
	}

	/**
	 * 
	 * @param vector
	 * @param demand
	 */
	public State(HashMap<String, Integer> vector, int demand) {
		this.vector = vector;
		this.demand = demand;
	}

	/**
	 * 
	 * @param type
	 * @param state
	 */
	public void addComponent(String type, int state) {
		vector.put(type, state);
	}

	/**
	 * 
	 * @param b
	 * @return
	 */
	public State diff(State b) {
		State result = new State();
		HashMap<String, Integer> temp = b.vector;
		for (String type : temp.keySet()) {
			result.addComponent(type, diffType(temp, type));
		}
		if (demand == b.demand)
			result.demand = b.demand;
		return result;
	}

	/**
	 * 
	 * @param b
	 * @param type
	 * @return
	 */
	public int diffType(HashMap<String, Integer> b, String type) {
		return b.get(type) - vector.get(type);
	}

	/**
	 * 
	 * @param b
	 * @param type
	 * @return
	 */
	public int diffType(State b, String type) {
		return b.vector.get(type) - vector.get(type);
	}

	/**
	 * 
	 * @param b
	 * @return
	 */
	public State add(State b) {
		if (b.demand != demand)
			return null;

		HashMap<String, Integer> temp = new HashMap<String, Integer>();
		for (String k : vector.keySet()) {
			temp.put(k, getComponentCount(k) + b.getComponentCount(k));
		}
		return new State(temp, demand);
	}

	/**
	 * 
	 * @return
	 */
	public int sum() {
		if (demand == -1)
			return 0;

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
		vector.put(node.getType(), vector.get(node.getType()) + 1);
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
	 * @return the vector
	 */
	public HashMap<String, Integer> getVector() {
		return vector;
	}

	/**
	 * @param vector
	 *            the vector to set
	 */
	public void setVector(HashMap<String, Integer> vector) {
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
		String result = "";
		for (String k : vector.keySet()) {
			result += "--" + k + "\t=>\t" + vector.get(k) + "\n";
		}
		result += "--Env\t=>\t" + demand;
		return error(result);
	}

	public String toLine() {
		String result = "(";
		for (String k : vector.keySet()) {
			result += vector.get(k) + ", ";
		}
		return result + demand + ")";
	}

	@SuppressWarnings("unchecked")
	public State clone() {
		State result = new State();
		result.demand = demand - 0;
		result.vector = (HashMap<String, Integer>) vector.clone();
		return result;
	}

	@Override
	public int hashCode() {
		int result = ( DECAF.forceStateDemandValidate ) ? new Integer(demand).hashCode() : 0;
		for (String k : vector.keySet()) {
			result += k.hashCode() + new Integer(vector.get(k)).hashCode();
		}
		return new Integer(result).hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof State))
			return false;

		State other = (State) o;

		if (DECAF.forceStateDemandValidate && demand != other.demand)
			return false;

		for (String k : vector.keySet()) {
			if (!other.vector.containsKey(k) || vector.get(k) != other.vector.get(k)) {
				return false;
			}
		}
		return true;
	}
}
