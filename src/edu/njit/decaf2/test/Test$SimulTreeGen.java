package edu.njit.decaf2.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Test$SimulTreeGen {

	private static String[] types;
	private static String [][] gamma;
	private static HashMap<String, ArrayList<String>> binaryEnumCache = new HashMap<String, ArrayList<String>>(); 
	
	public static void main(String[] args) {
		
		types = new String[] {"A", "B", "C", "D"};
		gamma = new String[][] {{"B", "C", "D"}, {"C", "A", "D"}, {"A", "D"}, {"C"}};
		
		buildBinaryEnumCache();
		printBinaryEnumCache();
		
		initTrees();
	}
	
	private static void printBinaryEnumCache() {
		
		System.out.println("Cache:");  
		for(String key : binaryEnumCache.keySet()) {
			System.out.print(key + " => ");

			ArrayList<String> values = binaryEnumCache.get(key);
			for(int i = 0; i < values.size(); i++) {
				System.out.print(values.get(i) + " | ");
			}
			
			System.out.println();
		}
		System.out.println("\n\n");
	}

	private static void buildBinaryEnumCache() {
		
		for(int i = 0; i < types.length; i++) {
			binaryEnumCache.put(types[i], powerSet(gamma[i]));
		}	
	}

	private static ArrayList<String> powerSet(String[] members) {
		
		if(members.length == 0)
			return new ArrayList<String>();
		
		int permutations = (int)Math.pow(2, members.length);
		ArrayList<String> binaryEnum = new ArrayList<String>(permutations); 
		
		for(int p = 0; p < permutations; p++) {
			
			String binary =  Integer.toBinaryString(p);
			while(binary.length() < members.length)
				binary = 0 + binary; 
			
			String block = "";
			for(int b = 0; b < binary.length(); b++) {
				block += binary.charAt(b) + ":" + members[b] + ","; 
			}
			block = block.substring(0, block.length() - 1);
			binaryEnum.add(p, block);
		}
		
		return binaryEnum;		
	}
	
	private static void initTrees() {
		
		for(int i = 0; i < types.length; i++) {
			
			int[] initFailureTransition = {0, 0, 0, 0};
			initFailureTransition[i]++;
			
			ArrayList<String> levels = new ArrayList<String>();
			levels.add("1:" + types[i]);
			buildTrees(levels, initFailureTransition);
		}
	}
	
	private static void buildTrees(ArrayList<String> levels, int[] failureTransition) {
		
		String[] terminalNodes = levels.get(levels.size() - 1).split(","); 
		ArrayList<Integer> gammaPermutations = new ArrayList<Integer>();
		
		for(int t = 0; t < terminalNodes.length; t++) {
			String terminalNode = terminalNodes[t];
			if(terminalNode.charAt(0) == '1') {
				String type = terminalNode.substring(terminalNode.indexOf(":") + 1);
				gammaPermutations.add(binaryEnumCache.get(type).size());
			}	
		}
			
		ArrayList<ArrayList<Integer>> cartesianProductEnum = new ArrayList<ArrayList<Integer>>(); 
		cartesianProduct(gammaPermutations, 0, new ArrayList<Integer>(gammaPermutations.size()), cartesianProductEnum);

	}

	private static void cartesianProduct(ArrayList<Integer> limits, int x, ArrayList<Integer> current, ArrayList<ArrayList<Integer>> list) {
		
		if (current.size() == limits.size()) {
			list.add(current);
		}
		
		if (x >= limits.size())
			return;

		for (int i = 0; i <= limits.get(x); i++) {
			current.add(i);
			cartesianProduct(limits, x + 1, current, list);
		}
	}
	
	public static void printArray(int[] a) {
		
		String s = "[";
		for(int i : a)
			s += i + " ";
		s = s.substring(0, s.length() - 1);
		s +="]";
		System.out.println(s);
	}
}
