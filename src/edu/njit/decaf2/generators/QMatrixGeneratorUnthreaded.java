/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.util.ArrayList;
import java.util.HashMap;
//import java.util.Iterator;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.Simulation;
import edu.njit.decaf2.data.State;

/**
 * DECAF 2 - QMatrixGenerator
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public class QMatrixGeneratorUnthreaded extends DECAF {
	public static double numberOfTrees;
	private static String[] vectorKeys;
	public static HashMap<State, ArrayList<String>> likeTransitionMap;

	/**
	 * Sets {@link State} {@code transitionStates}, {@link String}[]
	 * {@code vectorKeys}, {@link Double}[][] {@code demandMatrix}
	 * 
	 * @param states
	 * @param vectorKeys
	 */
	public static void init() {
		vectorKeys = new String[Simulation.nodeMap.size()];
		vectorKeys = Simulation.nodeMap.keySet().toArray(vectorKeys);
	}

	/**
	 * Algorithm 2.0 - Generating QMatrix via {@link QMatrixRunnable} Refer to
	 * annotated source for details
	 * 
	 * @return qMatrix
	 */
	public static void generateQMatrix() {
		// Cache the length of valid transition states
		int statesLen = Simulation.states.length;

		// Initialize brand new qMatrix[][] array
		Simulation.qMatrix = new double[statesLen][statesLen];

		likeTransitionMap = new HashMap<State, ArrayList<String>>();

		// Iterate over matrix, ignore diagonal
		for (int i = 0; i < statesLen; i++) {
			for (int j = i == 0 ? 1 : 0; j < statesLen; j = j == i - 1 ? j + 2 : j + 1) {

				if (j == i)
					continue;
				double fillV = fillQMatrix(Simulation.states[i], Simulation.states[j]);

				if (Double.isNaN(fillV)) {
					State differenceState = Simulation.states[i].diff(Simulation.states[j]);
					String str = i + "," + j;

					if (likeTransitionMap.containsKey(differenceState)) {
						likeTransitionMap.get(differenceState).add(str);
					} else {
						ArrayList<String> temp = new ArrayList<String>();
						temp.add(str);
						likeTransitionMap.put(differenceState, temp);
					}
				} else {
					Simulation.qMatrix[i][j] = fillV;
				}
			}
		}

		// Generate trees as required
		TreeGeneratorUnthreaded.initSubTrees();

		// Fill diagonals with negative row sum
		for (int i = 0; i < statesLen; i++) {
			double sum = 0.0;
			for (int j = i == 0 ? 1 : 0; j < statesLen; j = j == i - 1 ? j + 2 : j + 1)
				sum += Simulation.qMatrix[i][j];
			Simulation.qMatrix[i][i] = -sum;
		}
	}

	/**
	 * 
	 * @return
	 */

	public HashMap<State, ArrayList<String>> getLikeTransitionMap() {
		return likeTransitionMap;
	}

	/**
	 * === NOTE === QMatrixRunnable will call this method, please use the
	 * refactoring tool in Eclipse to make any modifications.
	 * 
	 * @param from
	 * @param to
	 * @return rate
	 */
	public static double fillQMatrix(State from, State to) {
		boolean repairTransition = true;
		boolean failedTransition = true;
		boolean enviroTransition = false;
		String repair = null;
		int fromDemand = from.getDemand();
		int toDemand = to.getDemand();

		if (fromDemand != toDemand) {
			enviroTransition = true;
		}
		for (int i = 0; i < vectorKeys.length; i++) {
			int iFrom = from.getVector().get(vectorKeys[i]), iTo = to.getVector().get(vectorKeys[i]);

			if (enviroTransition && iFrom != iTo) {
				return 0.0;
			} else if (repair == null && repairTransition && iTo == iFrom - 1) {
				repair = vectorKeys[i];
			} else if (iFrom != iTo) {
				repairTransition = false;
			}
			if (iTo < iFrom) {
				failedTransition = false;
			}
		}

		if (enviroTransition)
			return Simulation.demandMatrix[fromDemand][toDemand];
		if (failedTransition)
			return Double.NaN;

		if (repairTransition && repair != null)
			return (double) from.getVector().get(repair) * Simulation.nodeMap.get(repair).getRepairRates()[fromDemand]
					/ (double) from.sum();
		return 0.0;
	}

	public static int getReusedTrees() {
		int result = 0;
		for (State k : likeTransitionMap.keySet()) {
			for (String j : likeTransitionMap.get(k)) {
				int f = Integer.parseInt(j.split(",")[0]);
				int t = Integer.parseInt(j.split(",")[1]);
				if( Simulation.qMatrix[f][t] != 0 )
					result++;
			}
		}
		return result;
	}

	public static int getTotalTrees() {
		return (int) numberOfTrees;
	}

	public static String printQMatrix() {
		int statesLen = Simulation.states.length;
		String result = "";
		for (int i = 0; i < statesLen; i++) {
			for (int j = 0; j < statesLen; j++) {
				if (Simulation.qMatrix[i][j] != 0.0)
					result += Simulation.qMatrix[i][j] + "@(" + i + "," + j + "); ";
			}
			// result += "\n";
		}
		return result;
	}

	public static String printQMatrix(boolean b) {
		String result = "";
		int statesLen = Simulation.states.length;
		for (int i = 0; i < statesLen; i++) {
			for (int j = 0; j < statesLen; j++) {
				String t = (Simulation.qMatrix[i][j] + "");
				result += "(" + t.substring(0, Math.min(t.length(), 5)) + ")" + "\t";
			}
			result += "\n";
		}
		return error(result);
	}
}
