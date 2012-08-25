/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.data;

import java.util.ArrayList;

import edu.njit.decaf2.DECAF;

/**
 * DECAF 2 - ComplementPhi
 * 
 * @author Sashank Tadepalli
 * @version 2.0
 *
 */
public class ComplementPhi extends DECAF {
	private String 						type;
	private ArrayList<String> 			parentList = new ArrayList<String>();
	private int						currIndex = 0;
	
	public ComplementPhi(String t){
		type = t;
	}
	
	public void addParent(String p){
		parentList.add(p);
	}
	
	public String getTopParent(){
		currIndex++;
		return parentList.get(currIndex - 1);
	}
	
	public int size(){
		return parentList.size();
	}
	
	public String getType(){
		return type;
	}
	
	public void reset(){
		currIndex = 0;
	}
	
	public void removeTopParent(){
		parentList.remove(0);
	}

	@Override
	public String toString(){
		return error("Type: " + type + "\nParents: " + parentList);
	}
}
