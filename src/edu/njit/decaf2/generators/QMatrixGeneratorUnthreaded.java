/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.Simulation;
import edu.njit.decaf2.data.QMatrix;
import edu.njit.decaf2.data.State;

/**
 * 
 * DECAF - QMatrixGeneratorUnthreaded
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public final class QMatrixGeneratorUnthreaded extends DECAF {
	private static String[] vectorKeys;
	public static Map<State, ArrayList<String>> likeTransitionMap;
	
	/**
	 * 
	 */
	private QMatrixGeneratorUnthreaded() {
		super();
	}

	/**
	 * 
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

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
		likeTransitionMap = new HashMap<State, ArrayList<String>>();
	}

	/**
	 * Algorithm 2.0 - Generating QMatrix via {@link QMatrixRunnable} Refer to
	 * annotated source for details
	 * 
	 * @return qMatrix
	 */
	public static void generateQMatrix() {
		// Cache the length of valid transition states
		final int statesLen = Simulation.states.length;
		
		// Iterate over matrix, ignore diagonal
		for (int i = 0; i < statesLen; i++) {
			for (int j = i == 0 ? 1 : 0; j < statesLen; j = j == i - 1 ? j + 2 : j + 1) {
				
				final double fillV = fillQMatrix(Simulation.states[i], Simulation.states[j]);

				if (Double.isNaN(fillV)) {
					final State differenceState = Simulation.states[i].diff(Simulation.states[j]);
					final String str = i + "," + j;

					if (likeTransitionMap.containsKey(differenceState)) {
						likeTransitionMap.get(differenceState).add(str);
					} else {
						final ArrayList<String> temp = new ArrayList<String>();
						temp.add(str);
						likeTransitionMap.put(differenceState, temp);
					}
				} else {
					QMatrix.put(i, j, fillV);
				}
			}
		}

		// Generate trees as required
		TreeGeneratorUnthreaded.initSubTrees();

		// Fill diagonals with negative row sum
		for (int i = 0; i < statesLen; i++) {
			double sum = 0.0;
			for (int j = i == 0 ? 1 : 0; j < statesLen; j = j == i - 1 ? j + 2 : j + 1){
				sum += QMatrix.get(i, j);
			}
			QMatrix.put(i, i, -sum);
		}
		
		// Fill statistics
		setValidTransitionCount();
	}

	/**
	 * 
	 * @return
	 */

	public Map<State, ArrayList<String>> getLikeTransitionMap() {
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
	public static double fillQMatrix(final State from, final State to) {
		boolean repairTransition = true;
		boolean failedTransition = true;
		boolean enviroTransition = false;
		String repair = null;
		final int fromDemand = from.getDemand();
		final int toDemand = to.getDemand();

		if (fromDemand != toDemand) {
			enviroTransition = true;
		}
		for (int i = 0; i < vectorKeys.length; i++) {
			final int iFrom = from.getVector().get(vectorKeys[i]);
			final int iTo = to.getVector().get(vectorKeys[i]);

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

		if (enviroTransition){
			return Simulation.demandMatrix[fromDemand][toDemand];
		}
		if (failedTransition){
			return Double.NaN;
		}
		if (repairTransition && repair != null){
			return (double) from.getVector().get(repair) * Simulation.nodeMap.get(repair).getRepairRates()[fromDemand]
					/ (double) from.sum();
		}
		return 0.0;
	}

	public static void setValidTransitionCount() {
		for (State k : likeTransitionMap.keySet()) {
			for (String j : likeTransitionMap.get(k)) {
				final int fIndex = Integer.parseInt(j.split(",")[0]);
				final int tIndex = Integer.parseInt(j.split(",")[1]);
				if (QMatrix.get(fIndex, tIndex) != 0){
					Simulation.numberOfTransitions++;
				}
			}
		}
	}

	public static String printQMatrix() {
		final int statesLen = Simulation.states.length;
		final StringBuffer result = new StringBuffer();
		for (int i = 0; i < statesLen; i++) {
			for (int j = 0; j < statesLen; j++) {
				if (QMatrix.get(i, j) != 0.0){
					result.append(QMatrix.get(i, j) + "@(" + i + "," + j + "); ");
				}
			}
			// result += "\n";
		}
		return result.toString();
	}

	public static String printQMatrix(final boolean flag) {
		final StringBuffer result = new StringBuffer();
		final int statesLen = Simulation.states.length;
		for (int i = 0; i < statesLen; i++) {
			for (int j = 0; j < statesLen; j++) {
				final String point = Double.toString(QMatrix.get(i, j));
				result.append('(');
				result.append(point.substring(0, Math.min(point.length(), 5)));
				result.append(")\t");
			}
			result.append('\n');
		}
		return error(result.toString());
	}
}
