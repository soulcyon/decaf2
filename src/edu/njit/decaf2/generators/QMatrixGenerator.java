/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveAction;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.Simulation;
import edu.njit.decaf2.structures.State;

/**
 * 
 * DECAF - QMatrixGenerator
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public final class QMatrixGenerator extends DECAF {
	private static String[] vectorKeys;
	protected static Map<State, CopyOnWriteArrayList<String>> likeTransitionMap;

	/**
	 * 
	 */
	private QMatrixGenerator() {
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
		likeTransitionMap = new ConcurrentHashMap<State, CopyOnWriteArrayList<String>>();
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
		RecursiveAction task = new QMatrixAction(0, statesLen, statesLen);
		
        DECAF.threadPool.invoke(task);
        
		// Generate trees as required
		TreeGenerator.initSubTrees();

		// Fill diagonals with negative row sum
		for (int i = 0; i < statesLen; i++) {
			double sum = 0.0;
			for (int j = 0; j < statesLen; j++) {
				sum += Simulation.qmatrix.getQuick(i, j);
			}
			Simulation.qmatrix.setQuick(i, i, -sum);
		}

		generateStatistics();
	}

	/**
	 * 
	 * @param from
	 * @param to
	 * @return rate
	 */
	protected static double fillQMatrix(final State from, final State to) {
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

		if (enviroTransition) {
			return Simulation.demandMatrix[fromDemand][toDemand];
		}
		if (failedTransition) {
			return Double.NaN;
		}
		if (repairTransition && repair != null) {
			return (double) from.getVector().get(repair) * Simulation.nodeMap.get(repair).getRepairRates()[fromDemand]
					/ (double) from.sum();
		}
		return 0.0;
	}

	/**
	 * 
	 */
	public static void generateStatistics() {
		for (CopyOnWriteArrayList<String> value : likeTransitionMap.values()) {
			for (String j : value) {
				String[] t = j.split(",");

				if (Simulation.qmatrix.getQuick(Integer.parseInt(t[0]), Integer.parseInt(t[1])) != 0) {
					Simulation.numberOfTransitions++;
				}
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public static String printQMatrix() {
		final int statesLen = Simulation.states.length;
		final StringBuffer result = new StringBuffer();
		for (int i = 0; i < statesLen; i++) {
			for (int j = 0; j < statesLen; j++) {
				if (Simulation.qmatrix.getQuick(i, j) != 0.0) {
					result.append(Simulation.qmatrix.get(i, j) + "@(" + i + "," + j + "); ");
				}
			}
		}
		return result.toString();
	}

	/**
	 * 
	 * @param flag
	 * @return
	 */
	public static String printQMatrix(final boolean flag) {
		final StringBuffer result = new StringBuffer();
		final int statesLen = Simulation.states.length;
		for (int i = 0; i < statesLen; i++) {
			for (int j = 0; j < statesLen; j++) {
				final String point = Double.toString(Simulation.qmatrix.getQuick(i, j));
				result.append('(');
				result.append(point.substring(0, Math.min(point.length(), 5)));
				result.append(")\t");
			}
			result.append('\n');
		}
		return error(result.toString());
	}
}
