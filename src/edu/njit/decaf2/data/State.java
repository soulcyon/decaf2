/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.data;

import java.util.ArrayList;
import java.util.HashMap;

import edu.njit.decaf2.DECAF;

/**
 * DECAF 2 - State
 * 
 * @author Sashank Tadepalli
 * @version 2.0
 * 
 */
public class State extends DECAF {
	private ArrayList<String> typeList;
	private HashMap<String, Integer> vector;
	private int demand = -1;

	/**
	 * 
	 */
	public State() {
		vector = new HashMap<String, Integer>();
		typeList = new ArrayList<String>();
	}

	/**
	 * 
	 * @param components
	 * @param states
	 * @param demand
	 */
	public State(ArrayList<String> typeList, ArrayList<Integer> states, int demand) {
		this.typeList = typeList;
		int i = 0;
		vector = new HashMap<String, Integer>();
		for (String k : typeList) {
			vector.put(k, states.get(i));
			i++;
		}
		this.demand = demand;
	}

	/**
	 * 
	 * @param components
	 * @param states
	 */
	public State(ArrayList<String> typeList, ArrayList<Integer> states) {
		this(typeList, states, 0);
	}

	/**
	 * 
	 * @param type
	 * @param state
	 */
	public State(String type, int state) {
		vector = new HashMap<String, Integer>();
		typeList = new ArrayList<String>();
		vector.put(type, state);
		typeList.add(type);
	}

	/**
	 * 
	 * @param vector
	 * @param demand
	 */
	public State(ArrayList<String> typeList, HashMap<String, Integer> vector, int demand) {
		this.typeList = typeList;
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

		if (!typeList.equals(b.typeList))
			return null;

		HashMap<String, Integer> temp = new HashMap<String, Integer>();
		for (String k : vector.keySet()) {
			temp.put(k, getComponentCount(k) + b.getComponentCount(k));
		}
		return new State(typeList, temp, demand);
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
		System.out.println(typeList);
		for (String k : typeList) {
			result += "--" + k + "\t=>\t" + vector.get(k) + "\n";
		}
		result += "--Env\t=>\t" + demand;
		return error(result);
	}

	public String toLine() {
		String result = "(";
		for (String k : typeList) {
			result += vector.get(k) + ", ";
		}
		return result + demand + ")";
	}

	public State clone() {
		State result = new State();
		result.demand = demand - 0;
		HashMap<String, Integer> tempVector = new HashMap<String, Integer>();
		tempVector.putAll(vector);
		result.vector = tempVector;
		result.typeList = typeList;
		return result;
	}

	@Override
	public int hashCode() {
		int result = (DECAF.forceStateDemandValidate) ? new Integer(demand).hashCode() : 0;
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
