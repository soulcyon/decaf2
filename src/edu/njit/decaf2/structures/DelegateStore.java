package edu.njit.decaf2.structures;

import java.util.ArrayList;
import java.util.Map;

public class DelegateStore {
	public String root = "";
	public ArrayList<String> levels;
	public State ft;
	public double rate;
	public Map<String, ArrayList<String>> bfhMap;
	
	/**
	 * 
	 * @param l
	 * @param f
	 * @param sr
	 * @param bfhCopy
	 */
	public DelegateStore(ArrayList<String> l, State f, double sr, Map<String, ArrayList<String>> bfhCopy) {
		levels = l;
		ft = f;
		rate = sr;
		bfhMap = bfhCopy;
		
		root = levels.get(0).substring(levels.get(0).indexOf(":") + 1);
	}
	
	public String toString(){
		return root + " => " + levels.get(1);
	}
}
