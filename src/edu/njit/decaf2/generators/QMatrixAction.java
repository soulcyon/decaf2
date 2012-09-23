/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.generators;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveAction;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.Simulation;
import edu.njit.decaf2.structures.Point;
import edu.njit.decaf2.structures.State;

/**
 * DECAF - QMatrixCallable
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public class QMatrixAction extends RecursiveAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8744535069909809193L;
	private int start;
	private int end;
	private int statesLen;
	
	public QMatrixAction(int x, int y, int len) {
		start = x;
		end = y;
		statesLen = len;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.RecursiveAction#compute()
	 */
	@Override
	protected void compute() {
		if (start - end > (statesLen * statesLen / DECAF.threadCount)) {
			int mid = (start + end) >>> 1;
			invokeAll(new QMatrixAction(start, mid, statesLen), new QMatrixAction(mid, end, statesLen));
		} else {
			for (int i = start; i < end; i++) {
				for (int j = i == 0 ? 1 : 0; j < statesLen; j = j == i - 1 ? j + 2 : j + 1) {
					final double fillV = QMatrixGenerator.fillQMatrix(Simulation.states[i], Simulation.states[j]);

					if (Double.isNaN(fillV)) {
						final State differenceState = Simulation.states[i].diff(Simulation.states[j]);
						final Point point = new Point(i, j);

						if (QMatrixGenerator.likeTransitionMap.containsKey(differenceState)) {
							QMatrixGenerator.likeTransitionMap.get(differenceState).add(point);
						} else {
							final CopyOnWriteArrayList<Point> temp = new CopyOnWriteArrayList<Point>();
							temp.add(point);
							QMatrixGenerator.likeTransitionMap.put(differenceState, temp);
						}
					} else {
						Simulation.qmatrix.setQuick(i, j, fillV);
					}
				}
			}
		}
	}
}
