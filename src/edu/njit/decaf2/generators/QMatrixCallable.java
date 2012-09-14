/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.Simulation;
import edu.njit.decaf2.data.QMatrix;
import edu.njit.decaf2.data.State;

/**
 * DECAF - QMatrixCallable
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public class QMatrixCallable extends DECAF implements Callable<Object> {
	private int start;
	private int end;
	private int statesLen;

	public QMatrixCallable(int x, int y, int len) {
		start = x;
		end = y;
		statesLen = len;
	}

	/**
	 * 
	 */
	@Override
	public Object call() throws Exception {
		for (int i = start; i < end; i++) {
			for (int j = i == 0 ? 1 : 0; j < statesLen; j = j == i - 1 ? j + 2 : j + 1) {
				final double fillV = QMatrixGenerator.fillQMatrix(Simulation.states[i], Simulation.states[j]);

				if (Double.isNaN(fillV)) {
					final State differenceState = Simulation.states[i].diff(Simulation.states[j]);
					final String str = i + "," + j;

					if (QMatrixGenerator.likeTransitionMap.containsKey(differenceState)) {
						QMatrixGenerator.likeTransitionMap.get(differenceState).add(str);
					} else {
						final CopyOnWriteArrayList<String> temp = new CopyOnWriteArrayList<String>();
						temp.add(str);
						QMatrixGenerator.likeTransitionMap.put(differenceState, temp);
					}
				} else {
					QMatrix.put(i, j, fillV);
				}
			}
		}
		return null;
	}
}
