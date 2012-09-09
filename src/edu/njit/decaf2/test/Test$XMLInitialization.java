/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.test;

import java.util.Arrays;
import java.util.HashSet;

/**
 * DECAF - Test$XMLInitialization
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public class Test$XMLInitialization {
	private static HashSet<String> comb1 = new HashSet<String>();
	private static String[] arr1 = new String[100];
	private static HashSet<String> comb2 = new HashSet<String>();
	private static String[] arr2 = new String[100];

	public static void main(String[] args) {
		int comps = 6;
		int max = 3;

		/****************************************************************************/
		/* Sashank Bench */
		/****************************************************************************/
		double t = System.nanoTime();
		comb(comps, max, 0, "");
		arr1 = new String[comb1.size()];
		Arrays.sort(comb1.toArray(arr1));
		System.out.println("Sashank Comb: " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0);

		/****************************************************************************/
		/* Princeton Bench */
		/****************************************************************************/
		t = System.nanoTime();
		String s = "";

		// Create generator string with appropriate number of redundancies
		for (int j = 0; j < comps; j++) {
			for (int i = 0; i < max; i++) {
				s += i;
			}
		}

		// Run algorithm over generator string
		comb2 = findCombinations(s, comps);
		arr2 = new String[comb2.size()];
		Arrays.sort(comb2.toArray(arr2));
		System.out.println("Princeton Comb: " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0);

		System.out.println((arr1.length == arr2.length) + " => verified? @ " + arr1.length);
		System.out.print("[");
		for (int i = 0; i < arr1.length; i++) {
			System.out.print(arr1[i] + ",");
		}
		System.out.print("]\n[");
		for (int i = 0; i < arr2.length; i++) {
			System.out.print(arr2[i] + ",");
		}
		System.out.print("]");

	}

	private static void comb(int len, int max, int curr, String pref) {
		if (pref.length() == len) {
			comb1.add(pref);
		}
		if (curr > len) {
			return;
		}
		for (int i = 0; i < max; i++) {
			comb(len, max, curr + 1, pref + i + "");
		}
	}

	private static HashSet<String> findCombinations(String input, int k) {
		if (k == 0) {
			HashSet<String> result = new HashSet<String>();
			result.add("");
			return result;
		}

		if (input.isEmpty())
			return new HashSet<String>();

		HashSet<String> finalResult = new HashSet<String>();
		String prefix = new StringBuilder().append((input.charAt(0))).toString();
		String restOfString = input.substring(1);
		HashSet<String> intermediate = findCombinations(restOfString, k - 1);
		for (String s : intermediate) {
			s = (prefix + s);
			finalResult.add(s);
		}
		HashSet<String> tailResult = findCombinations(restOfString, k);
		finalResult.addAll(tailResult);

		return finalResult;
	}
}
