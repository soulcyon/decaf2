/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.structures;

import java.util.HashMap;
import java.util.Map;

import edu.njit.decaf2.DECAF;

/**
 * 
 * DECAF - FailureNode
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public class FailureNode extends DECAF {
	private String type;
	private int required;
	private int redundancy;
	private double[] failureRates;
	private double[] repairRates;

	private Map<String, Double> cascadingFailures = new HashMap<String, Double>();

	/**
	 * Sets {@link String} {@code type} and {@link Double}[]
	 * {@code failureRates} array.
	 * 
	 * @param type
	 * @param failureRates
	 */
	public FailureNode(final String type, final double[] failureRates) {
		this.type = type;
		this.failureRates = failureRates;
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
	public FailureNode(final int required, final String type, final int redundancy, final double[] failureRates,
			final double[] repairRates) {
		super();
		this.required = required;
		this.type = type;
		this.redundancy = redundancy;
		this.failureRates = failureRates;
		this.repairRates = repairRates;
	}

	/**
	 * Sets {@link String} {@code type}.
	 * 
	 * @param type
	 */
	public FailureNode(final String type) {
		this.type = type;
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
	public void setType(final String type) {
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
	public void setRequired(final int required) {
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
	public void setRedundancy(final int redundancy) {
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
	public void setFailureRates(final double[] failureRates) {
		this.failureRates = failureRates;
	}

	/**
	 * @return the repairRates
	 */
	public double[] getRepairRates() {
		return repairRates;
	}

	/**
	 * @param repairRates
	 *            the repairRates to set
	 */
	public void setRepairRates(final double[] repairRates) {
		this.repairRates = repairRates;
	}

	/**
	 * @return the cascadingFailures
	 */
	public Map<String, Double> getCascadingFailures() {
		return cascadingFailures;
	}

	/**
	 * @param cascadingFailures
	 *            the cascadingFailures to set
	 */
	public void setCascadingFailures(final Map<String, Double> cascadingFailures) {
		this.cascadingFailures = cascadingFailures;
	}

	/**
	 * Adds a cascading {@link FailureNode} {@code failure} with {@link Double}
	 * {@code rate}.
	 * 
	 * @param type
	 * @param rate
	 */
	public void addCascadingFailure(final FailureNode node, final double rate) {
		cascadingFailures.put(node.getType(), rate);
	}

	/**
	 * Removes cascading {@link FailureNode} {@code failure}.
	 * 
	 * @param type
	 */
	public void removeCascadingFailure(final FailureNode type) {
		cascadingFailures.remove(type);
	}

	public double getRate(final String type2) {
		return cascadingFailures.get(type2);
	}

	@Override
	public String toString() {
		final StringBuffer cascadePrint = new StringBuffer("--Causes to fail (" + cascadingFailures.size() + ") ");
		for (String k : cascadingFailures.keySet()) {
			cascadePrint.append("\n\t\t\t");
			cascadePrint.append(k);
			cascadePrint.append(" @ ");
			cascadePrint.append(cascadingFailures.get(k));
		}
		final StringBuffer failurePrint = new StringBuffer();
		for (int i = 0; i < failureRates.length; i++) {
			failurePrint.append(", ");
			failurePrint.append(failureRates[i]);
		}
		return error("--FailureNode \t\t" + type + " @ " + failurePrint.substring(2) + "\n--Required \t\t" + required
				+ "\n--Redundancy \t\t" + redundancy + "\n" + cascadePrint);
	}

	@Override
	public int hashCode() {
		int result = type.hashCode() + failureRates.hashCode() + Integer.valueOf(redundancy).hashCode()
				+ Integer.valueOf(required).hashCode();

		for (String k : cascadingFailures.keySet()) {
			result += k.hashCode() + Double.valueOf(cascadingFailures.get(k)).hashCode();
		}
		return Integer.valueOf(result).hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof FailureNode)) {
			return false;
		}

		final FailureNode other = (FailureNode) obj;
		if (!type.equals(other.type) || redundancy != other.redundancy || required != other.required
				|| failureRates != other.failureRates || cascadingFailures.size() != other.cascadingFailures.size()) {
			return false;
		}
		for (String k : cascadingFailures.keySet()) {
			if (!other.cascadingFailures.containsKey(k) || cascadingFailures.get(k) != other.cascadingFailures.get(k)) {
				return false;
			}
		}
		return true;
	}
}
