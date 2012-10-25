package edu.njit.decaf2.structures;

import java.util.Comparator;

import edu.njit.decaf2.Simulation;

public class PowerSetComparator implements Comparator<String> {
	private String parentType;

	public PowerSetComparator(String pt) {
		parentType = pt;
	}

	@Override
	public int compare(String o1, String o2) {
		String[] test1 = o1.split(",");
		String[] test2 = o2.split(",");
		double r1 = 1;
		double r2 = 1;
		for (int i = 0; i < test1.length; i++) {
			String[] t1 = test1[i].split(":");
			String[] t2 = test2[i].split(":");
			if (t1[0].compareTo("0") != 0) {
				r1 *= Simulation.nodeMap.get(parentType).getRate(t1[1])
						* (1 - Simulation.nodeMap.get(parentType).getRate(t1[1]));
			}
			if (t2[0].compareTo("0") != 0) {
				r2 *= Simulation.nodeMap.get(parentType).getRate(t2[1])
						* (1 - Simulation.nodeMap.get(parentType).getRate(t2[1]));
			}
		}
		return Double.compare(r1, r2);
	}

}
