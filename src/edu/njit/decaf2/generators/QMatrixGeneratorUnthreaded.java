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
import edu.njit.decaf2.structures.Point;
import edu.njit.decaf2.structures.State;

/**
 * 
 * DECAF - QMatrixGeneratorUnthreaded
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public final class QMatrixGeneratorUnthreaded extends DECAF {
	public static Map<State, ArrayList<Point>> likeTransitionMap;

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
		likeTransitionMap = new HashMap<State, ArrayList<Point>>();
	}

	/**
	 * Algorithm 2.0 - Generating QMatrix via {@link QMatrixRunnable} Refer to
	 * annotated source for details
	 * 
	 * @return qMatrix
	 */
	public static void generateQMatrix() {
		double t = System.nanoTime();

		// TESTING OF VERSION 3 QMATRIX GENERATION
		final int statesLen = Simulation.states.length;

		/*final int demandLen = Simulation.demandMatrix.length;
		
		for (int i = 0; i < statesLen / demandLen; i++) {
			for (int j = 0; j < demandLen; j++) {
				for (int k = 0; k < demandLen; k++) {
					Simulation.qmatrix .setQuick(i * demandLen + j, i * demandLen + k,
						   Simulation.demandMatrix[Simulation.states[j].getDemand()][Simulation.states[k].getDemand()]);
				}
			}
		}
		
		List<Integer> redundancyLimits = new ArrayList<Integer>();
		for( int i = 0; i < Simulation.typeList.size(); i++ ){
			redundancyLimits.add(Simulation.nodeMap.get(Simulation.typeList.get(i)).getRedundancy() + 1);
		}
		for (int i = 0; i < statesLen; i++) {
			List<Integer> currentLimits = new ArrayList<Integer>();
			State diff = Simulation.states[i].diff(new State(redundancyLimits, Simulation.states[i].getDemand()));
			for( int k = 0; k < Simulation.typeList.size(); k++ ){
				currentLimits.add(diff.getComponentCount(Simulation.typeList.get(k)));
			}
			List<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
			TreeGeneratorUnthreaded.cartesianProduct(currentLimits, 0, new ArrayList<Integer>(), result);
			if( result.size() >= 1 ){
				result.remove(0);
			}
			for(ArrayList<Integer> product : result){
				final State testState = new State(product, Simulation.states[i].getDemand()).add(Simulation.states[i]);
				final State differenceState = Simulation.states[i].diff(testState);
				
				if (likeTransitionMap.containsKey(differenceState)) {
					likeTransitionMap.get(differenceState).add(new Point(i, Simulation.stateMap.get(testState)));
				} else {
					final ArrayList<Point> tempList = new ArrayList<Point>();
					tempList.add(new Point(i, Simulation.stateMap.get(testState)));
					likeTransitionMap.put(differenceState, tempList);
				}
			}
		}*/
		
		
		// Iterate over matrix, ignore diagonal
		for (int i = 0; i < statesLen; i++) {
			for (int j = 0; j < statesLen; j++) {
				if (j == i) {
					continue;
				}

				final double fillV = fillQMatrix(Simulation.states[i], Simulation.states[j]);

				if (Double.isNaN(fillV)) {
					final State differenceState = Simulation.states[i].diff(Simulation.states[j]);
					final Point ptr = new Point(i, j);

					if (likeTransitionMap.containsKey(differenceState)) {
						likeTransitionMap.get(differenceState).add(ptr);
					} else {
						final ArrayList<Point> temp = new ArrayList<Point>();
						temp.add(ptr);
						likeTransitionMap.put(differenceState, temp);
					}
				} else {
					Simulation.qmatrix.setQuick(i, j, fillV);
				}
			}
		}

		//System.out.println("---Matrix Iteration Time: " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0);

		t = System.nanoTime();
		// Generate trees as required
		TreeGeneratorUnthreaded.initSubTrees();
		/*System.out.println("---Time to Gen Trees:     "
				+ (*/Simulation.treeGenerationTime = (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0/*))*/;

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
	 * @return
	 */

	public Map<State, ArrayList<Point>> getLikeTransitionMap() {
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

		for (int i = 0; i < Simulation.typeList.size(); i++) {
			final int iFrom = from.getVector().get(Simulation.typeList.get(i));
			final int iTo = to.getVector().get(Simulation.typeList.get(i));

			if (enviroTransition && iFrom != iTo) {
				return 0.0;
			} else if (repair == null && repairTransition && iTo == iFrom - 1) {
				repair = Simulation.typeList.get(i);
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
		for (ArrayList<Point> value : likeTransitionMap.values()) {
			for (Point j : value) {
				if (Simulation.qmatrix.getQuick(j.getX(), j.getY()) != 0) {
					Simulation.numberOfTransitions++;
				}
			}
			for (Point j : value) {
				if (Simulation.qmatrix.getQuick(j.getX(), j.getY()) != 0) {
					Simulation.numberOfUniqueTrees++;
					break;
				}
			}
		}
		Simulation.numberOfUniqueTrees += Simulation.nodeMap.size();
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
