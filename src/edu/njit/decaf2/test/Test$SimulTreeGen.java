package edu.njit.decaf2.test;

import java.util.ArrayList;
import java.util.HashMap;

public class Test$SimulTreeGen {

	private static String[] types;
	private static int[] redundancies; 
	private static String [][] gamma;
	private static HashMap<String, ArrayList<String>> binaryEnumCache = new HashMap<String, ArrayList<String>>(); 
	
	/*
	public static void main(String[] args) {
		
		types = new String[] {"A", "B", "C", "D"};
		redundancies = new int[] {2, 2, 2, 2};
		gamma = new String[][] {{"B", "C", "D"}, {"C", "A", "D"}, {"A", "D"}, {"C"}};
		
		buildBinaryEnumCache();
		printBinaryEnumCache();
		
		initTrees();
	}
	*/
	
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
			if(gamma[i].length > 0) {
				binaryEnumCache.put(types[i], powerSet(gamma[i]));
			}
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
			
			HashMap<String, ArrayList<String>> breadthFirstHistory = new HashMap<String, ArrayList<String>>();
			for (int j  = 0; j < types.length; j++)
				breadthFirstHistory.put(types[j], new ArrayList<String>());
			breadthFirstHistory.get(types[i]).add("|");
			
			buildTrees(levels, initFailureTransition, 1.0, breadthFirstHistory);
		}
	}
	
	private static void buildTrees(ArrayList<String> levels, int[] failureTransition, double subRate,
			HashMap<String, ArrayList<String>> breadthFirstHistory) {

		// base case - break out if tree is invalid i.e. it has more component types than redundancy
		for(int i = 0 ; i < redundancies.length; i++) {
			if(failureTransition[i] > redundancies[i]) {
				System.out.println("HALT");
				return;
			}
		}
		
		printTree(levels);

		// grow tree
		String[] terminalNodes = levels.get(levels.size() - 1).split(",");
		ArrayList<Integer> gammaPermutations = new ArrayList<Integer>();
		ArrayList<String> terminalTypes = new ArrayList<String>();

		// determine how many growth possibilities exist
		for (int t = 0; t < terminalNodes.length; t++) {
			String terminalNode = terminalNodes[t];
			String type = terminalNode.substring(terminalNode.indexOf(":") + 1);
			if (terminalNode.charAt(0) == '1' && binaryEnumCache.containsKey(type)) {
				gammaPermutations.add(binaryEnumCache.get(type).size());
				terminalTypes.add(type);
			}
		}

		// fork by different growth possibilities
		ArrayList<ArrayList<Integer>> cartesianProductEnum = new ArrayList<ArrayList<Integer>>();
		cartesianProduct(gammaPermutations, 0, new ArrayList<Integer>(gammaPermutations.size()), cartesianProductEnum);

		for (int c = 0; c < cartesianProductEnum.size(); c++) {

			// make copies of reference types to prevent data persistence over mutually exclusive recursive calls
			ArrayList<String> levelsCopy = new ArrayList<String>(levels);

			int[] failureTransitionCopy = new int[failureTransition.length];
			System.arraycopy(failureTransition, 0, failureTransitionCopy, 0,failureTransition.length);

			HashMap<String, ArrayList<String>> breadthFirstHistoryCopy = new HashMap<String, ArrayList<String>>();
			for (String key : breadthFirstHistory.keySet()) {
				ArrayList<String> compHistory = new ArrayList<String>(breadthFirstHistory.get(key));
				breadthFirstHistoryCopy.put(key, compHistory);
			}

			// add new level
			ArrayList<Integer> breadthEncoding = cartesianProductEnum.get(c);
			String newLevel = "";

			//go through all added nodes, denoted by 1:type
			for (int b = 0; b < breadthEncoding.size(); b++) {

				String parentType = terminalTypes.get(b);
				int binEnumId = breadthEncoding.get(b);
				String block = binaryEnumCache.get(parentType).get(binEnumId);
				newLevel += block + ",";
				
				// go through each of the added nodes' children  
				String[] gammaStatus = block.split(",");

				for (int g = 0; g < gammaStatus.length; g++) {

					String childInfo = gammaStatus[g];
					String childType = childInfo.substring(childInfo.indexOf(":") + 1);

					// update failureTransition and subRate, breadthFirstHistory for tree
					if (childInfo.charAt(0) == '1') {
						increment(failureTransitionCopy, childType);
						breadthFirstHistoryCopy.get(childType).add("|");
						//TODO update subRate
					} else {
						breadthFirstHistoryCopy.get(childType).add(parentType);
					}
				}
			}

			if(c > 0) {
				newLevel = newLevel.substring(0, newLevel.length() - 1);
				levelsCopy.add(newLevel);
				buildTrees(levelsCopy, failureTransitionCopy, subRate, breadthFirstHistoryCopy);
			}
			else {
				// TODO rate calculation on existing tree, populate likeTransitions in Q
			}
			
		}
	}

	private static void printTree(ArrayList<String> levels) {
		
		System.out.println("______________________________________________________");
		for(String level : levels)
			System.out.println(level);
	}

	private static void increment(int[] a, String type) {
		
		switch(type) {
			case "A" : a[0]++; break;
			case "B" : a[1]++; break;
			case "C" : a[2]++; break;
			case "D" : a[3]++; break;
		}
	}

	private static void cartesianProduct(ArrayList<Integer> limits, int x, ArrayList<Integer> current, ArrayList<ArrayList<Integer>> list) {
		
		if (current.size() == limits.size()) {
			list.add(current);
		}
		
		if (x >= limits.size())
			return;

		for (int i = 0; i < limits.get(x); i++) {
			ArrayList<Integer> currentCopy = new ArrayList<Integer>(current); 
			currentCopy.add(i);
			cartesianProduct(limits, x + 1, currentCopy, list); 
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
