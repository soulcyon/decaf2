/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.data;

import java.util.HashMap;

import edu.njit.decaf2.DECAF;

/**
 * DECAF 2 - FailureNode
 * 
 * @author Sashank Tadepalli
 * @version 2.0
 * 
 */
public class FailureNode extends DECAF {
	private String type;
	private int required;
	private int redundancy;
	private double[] failureRates;
	private double[] repairRates;

	private HashMap<String, Double> cascadingFailures = new HashMap<String, Double>();

	/**
	 * Sets {@link String} {@code type} and {@link Double}[]
	 * {@code failureRates} array.
	 * 
	 * @param type
	 * @param failureRates
	 */
	public FailureNode(String type, double[] failureRates) {
		setType(type);
		setFailureRates(failureRates);
	}

	/**
	 * Sets {@link Integer} {@code required}, {@link String} {@code type},
	 * {@link Integer} {@code redundancy} and {@link Double}[]
	 * {@code failureRates} array.
	 * 
	 * @param required
	 * @param type
	 * @param redundancy
	 * @param failureRate
	 */
	public FailureNode(int required, String type, int redundancy, double[] failureRates, double[] repairRates) {
		setRequired(required);
		setType(type);
		setRedundancy(redundancy);
		setFailureRates(failureRates);
		setRepairRates(repairRates);
	}

	/**
	 * Sets {@link String} {@code type}.
	 * 
	 * @param type
	 */
	public FailureNode(String type) {
		setType(type);
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the required
	 */
	public int getRequired() {
		return required;
	}

	/**
	 * @param required
	 *            the required to set
	 */
	public void setRequired(int required) {
		this.required = required;
	}

	/**
	 * @return the redundancy
	 */
	public int getRedundancy() {
		return redundancy;
	}

	/**
	 * @param redundancy
	 *            the redundancy to set
	 */
	public void setRedundancy(int redundancy) {
		this.redundancy = redundancy;
	}

	/**
	 * @return the failureRate
	 */
	public double[] getFailureRates() {
		return failureRates;
	}

	/**
	 * @param failureRate
	 *            the failureRate to set
	 */
	public void setFailureRates(double[] failureRates) {
		this.failureRates = failureRates;
	}

	/**
	 * @return the repairRates
	 */
	public double[] getRepairRates() {
		return repairRates;
	}

	/**
	 * @param repairRates the repairRates to set
	 */
	public void setRepairRates(double[] repairRates) {
		this.repairRates = repairRates;
	}
	
	/**
	 * @return the cascadingFailures
	 */
	public HashMap<String, Double> getCascadingFailures() {
		return cascadingFailures;
	}

	/**
	 * @param cascadingFailures
	 *            the cascadingFailures to set
	 */
	public void setCascadingFailures(HashMap<String, Double> cascadingFailures) {
		this.cascadingFailures = cascadingFailures;
	}

	/**
	 * Adds a cascading {@link FailureNode} {@code failure} with {@link Double}
	 * {@code rate}.
	 * 
	 * @param type
	 * @param rate
	 */
	public void addCascadingFailure(FailureNode node, double rate) {
		cascadingFailures.put(node.getType(), rate);
	}

	/**
	 * Removes cascading {@link FailureNode} {@code failure}.
	 * 
	 * @param type
	 */
	public void removeCascadingFailure(FailureNode type) {
		cascadingFailures.remove(type);
	}

	public double getRate(String type2) {
		return cascadingFailures.get(type2);
	}

	@Override
	public String toString() {
		String cascadePrint = "--Causes to fail (" + cascadingFailures.size() + ") ";
		for (String k : cascadingFailures.keySet()) {
			cascadePrint += "\n\t\t\t" + k + " @ " + cascadingFailures.get(k);
		}
		String failurePrint = "";
		for (int i = 0; i < failureRates.length; i++) {
			failurePrint += ", " + failureRates[i];
		}
		return error("--FailureNode \t\t" + type + " @ " + failurePrint.substring(2) + "\n--Required \t\t" + required
				+ "\n--Redundancy \t\t" + redundancy + "\n" + cascadePrint);
	}

	@Override
	public int hashCode() {
		int result = type.hashCode() + failureRates.hashCode() + new Integer(redundancy).hashCode()
				+ new Integer(required).hashCode();

		for (String k : cascadingFailures.keySet()) {
			result += k.hashCode() + new Double(cascadingFailures.get(k)).hashCode();
		}
		return new Integer(result).hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FailureNode))
			return false;

		FailureNode other = (FailureNode) o;
		if (!type.equals(other.type) || redundancy != other.redundancy || required != other.required
				|| failureRates != other.failureRates || cascadingFailures.size() != other.cascadingFailures.size())
			return false;
		for (String k : cascadingFailures.keySet()) {
			if (!other.cascadingFailures.containsKey(k) || cascadingFailures.get(k) != other.cascadingFailures.get(k)) {
				return false;
			}
		}
		return true;
	}
}
