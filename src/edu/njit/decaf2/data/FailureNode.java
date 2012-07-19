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
	private String 							type;
	private int 							required;
	private int 							redundancy;
	private double[]						failureRates;
	private HashMap<FailureNode, Double> 	cascadingFailures = new HashMap<FailureNode, Double>();

	/**
	 * 
	 * @param type
	 * @param failureRate
	 */
	public FailureNode(String type, double[] failureRates){
		setType(type);
		setFailureRates(failureRates);
	}
	
	/**
	 * 
	 * @param required
	 * @param type
	 * @param redundancy
	 * @param failureRate
	 */
	public FailureNode(int required, String type, int redundancy, double[] failureRates){
		setRequired(required);
		setType(type);
		setRedundancy(redundancy);
		setFailureRates(failureRates);
	}
	
	/**
	 * 
	 * @param type
	 */
	public FailureNode(String type){
		setType(type);
	}
	
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * @param type the type to set
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
	 * @param required the required to set
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
	 * @param redundancy the redundancy to set
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
	 * @param failureRate the failureRate to set
	 */
	public void setFailureRates(double[] failureRates) {
		this.failureRates = failureRates;
	}
	
	/**
	 * @return the cascadingFailures
	 */
	public HashMap<FailureNode, Double> getCascadingFailures() {
		return cascadingFailures;
	}
	
	/**
	 * @param cascadingFailures the cascadingFailures to set
	 */
	public void setCascadingFailures(HashMap<FailureNode, Double> cascadingFailures) {
		this.cascadingFailures = cascadingFailures;
	}
	
	/**
	 * 
	 * @param type
	 * @param rate
	 */
	public void addCascadingFailure(FailureNode type, double rate) {
		cascadingFailures.put(type, rate);
	}
	
	/**
	 * 
	 * @param type
	 */
	public void removeCascadingFailure(FailureNode type){
		cascadingFailures.remove(type);
	}
	
	@Override
	public String toString(){
		String cascadePrint = "--Causes to fail (" + cascadingFailures.size() + ") ";
		for( FailureNode k : cascadingFailures.keySet() ){
			cascadePrint += "\n\t\t\t" + k.type + " @ " + cascadingFailures.get(k);
		}
		return error("--FailureNode \t\t" + type + " @ " + failureRates +
				"\n--Required \t\t" + required + "\n--Redundancy \t\t" + redundancy
				+ "\n" + cascadePrint);
	}
	
	@Override
	public int hashCode() {
		int result = type.hashCode() +
				failureRates.hashCode() +
				new Integer(redundancy).hashCode() + 
				new Integer(required).hashCode();
		
		for( FailureNode k : cascadingFailures.keySet() ){
			result += k.type.hashCode() +
				k.failureRates.hashCode() +
				new Integer(k.redundancy).hashCode() + 
				new Integer(k.required).hashCode();
		}
		return new Integer(result).hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if( !(o instanceof FailureNode) ) return false;
		
		FailureNode other = (FailureNode) o;
		if( !type.equals(other.type) || 
			redundancy != other.redundancy || 
			required != other.required ||
			failureRates != other.failureRates ||
			cascadingFailures.size() != other.cascadingFailures.size() )
			return false;
		for( FailureNode k : cascadingFailures.keySet() ){
			if( !other.cascadingFailures.containsKey(k) ||
				cascadingFailures.get(k) != other.cascadingFailures.get(k) ){
				return false;
			}
		}
		return true;
	}
}
