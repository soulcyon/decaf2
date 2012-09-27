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
				here: for (int j = 0; j < statesLen; j++) {
					if (j == i)
						continue;

					State from = Simulation.states[i];
					State to = Simulation.states[j];
					boolean repairTransition = true;
					boolean failedTransition = true;
					boolean enviroTransition = false;
					String repair = null;
					final int fromDemand = from.getDemand();
					final int toDemand = to.getDemand();

					if (fromDemand != toDemand) {
						enviroTransition = true;
					}
					for (int k = 0; k < QMatrixGenerator.vectorKeys.length; k++) {
						final int iFrom = from.getVector().get(QMatrixGenerator.vectorKeys[k]);
						final int iTo = to.getVector().get(QMatrixGenerator.vectorKeys[k]);

						if (enviroTransition && iFrom != iTo) {
							continue here;
						} else if (repair == null && repairTransition && iTo == iFrom - 1) {
							repair = QMatrixGenerator.vectorKeys[k];
						} else if (iFrom != iTo) {
							repairTransition = false;
						}
						if (iTo < iFrom) {
							failedTransition = false;
						}
					}

					if (enviroTransition) {
						Simulation.qmatrix.setQuick(i, j, Simulation.demandMatrix[fromDemand][toDemand]);
					}
					if (repairTransition && repair != null) {
						Simulation.qmatrix.setQuick(i, j, (double) from.getVector().get(repair)
								* Simulation.nodeMap.get(repair).getRepairRates()[fromDemand] / (double) from.sum());
					}
					if (failedTransition) {
						final State differenceState = Simulation.states[i].diff(Simulation.states[j]);
						final Point point = new Point(i, j);

						if (QMatrixGenerator.likeTransitionMap.containsKey(differenceState)) {
							QMatrixGenerator.likeTransitionMap.get(differenceState).add(point);
						} else {
							final CopyOnWriteArrayList<Point> temp = new CopyOnWriteArrayList<Point>();
							temp.add(point);
							QMatrixGenerator.likeTransitionMap.put(differenceState, temp);
						}
					}
				}
			}
		}
	}
}
