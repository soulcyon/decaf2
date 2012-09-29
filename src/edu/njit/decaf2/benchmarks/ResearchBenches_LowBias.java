/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.benchmarks;

import java.util.HashSet;

import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.Simulation;
import edu.njit.decaf2.generators.DependabilityUnthreaded;
import edu.njit.decaf2.generators.QMatrixGeneratorUnthreaded;

/**
 * DECAF - ResearchBenches_LowBias
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public class ResearchBenches_LowBias {
	public static double treeGenTime = 0;
	public static double stateGenTime = 0;
	public static double mttf = 0;
	public static double ssu = 0;
	public static double totalTrees = 0;
	public static double avoidedTrees = 0;
	public static double uniqueTrees = 0;

	public static void main(String[] args) {
		DECAF.biasType = "low";
		DECAF.heightThreshold = 100;
		DECAF.rateThreshold = 0;

		// Set original
		Simulation sim = new Simulation();
		sim.run("data/input.xml");
		treeGenTime = Simulation.treeGenerationTime;
		stateGenTime = Simulation.stateGenerationTime;
		mttf = Simulation.meanTimeToFailure;
		ssu = Simulation.steadyStateUnavailability;
		totalTrees = Simulation.numberOfTrees;
		avoidedTrees = Simulation.numberOfAvoidedTrees;
		uniqueTrees = Simulation.numberOfUniqueTrees;
		System.gc();
		Runtime.getRuntime().gc();
		
		// Run height threshold trials
		DECAF.heightThreshold = 3;
		DECAF.rateThreshold = 0;
		executeSimulation();

		DECAF.heightThreshold = 4;
		executeSimulation();

		DECAF.heightThreshold = 5;
		executeSimulation();

		DECAF.heightThreshold = 6;
		executeSimulation();

		// Run rate threshold trials
		DECAF.heightThreshold = 100;
		DECAF.rateThreshold = 0.1;
		executeSimulation();

		DECAF.rateThreshold = 0.01;
		executeSimulation();

		DECAF.rateThreshold = 0.001;
		executeSimulation();

		DECAF.rateThreshold = 0.0001;
		executeSimulation();

		// Run height and rate threshold trials
		DECAF.heightThreshold = 3;
		DECAF.rateThreshold = 0.1;
		executeSimulation();

		DECAF.rateThreshold = 0.01;
		executeSimulation();

		DECAF.rateThreshold = 0.001;
		executeSimulation();

		DECAF.rateThreshold = 0.0001;
		executeSimulation();

		DECAF.heightThreshold = 4;
		DECAF.rateThreshold = 0.1;
		executeSimulation();

		DECAF.rateThreshold = 0.01;
		executeSimulation();

		DECAF.rateThreshold = 0.001;
		executeSimulation();

		DECAF.rateThreshold = 0.0001;
		executeSimulation();

		DECAF.heightThreshold = 5;
		DECAF.rateThreshold = 0.1;
		executeSimulation();

		DECAF.rateThreshold = 0.01;
		executeSimulation();

		DECAF.rateThreshold = 0.001;
		executeSimulation();

		DECAF.rateThreshold = 0.0001;
		executeSimulation();

		DECAF.heightThreshold = 6;
		DECAF.rateThreshold = 0.1;
		executeSimulation();

		DECAF.rateThreshold = 0.01;
		executeSimulation();

		DECAF.rateThreshold = 0.001;
		executeSimulation();

		DECAF.rateThreshold = 0.0001;
		executeSimulation();
	}

	/**
	 * 
	 */
	private static void executeSimulation() {
		Simulation.numberOfTrees = 0;
		Simulation.numberOfAvoidedTrees = 0;
		Simulation.numberOfUniqueTrees = 0;
		Simulation.numberOfTransitions = 0;
		Simulation.qmatrix = new DenseDoubleMatrix2D(Simulation.states.length, Simulation.states.length);
		QMatrixGeneratorUnthreaded.init();
		QMatrixGeneratorUnthreaded.generateQMatrix();
		DependabilityUnthreaded.systemDownStates = new HashSet<Integer>();
		Simulation.meanTimeToFailure = DependabilityUnthreaded.calculateMTTF();
		printTrialResults();
		System.gc();
		Runtime.getRuntime().gc();
	}

	private static void printTrialResults() {
		System.out.println("============================================================================");
		System.out.println("States           " + Simulation.states.length);
		System.out.println("--CUT HEIGHT @   " + DECAF.heightThreshold);
		System.out.println("--CUT RATE @     " + DECAF.rateThreshold);
		System.out.println("Tree Gen Time    " + Simulation.treeGenerationTime + " ("
				+ Math.abs((Simulation.treeGenerationTime / treeGenTime) - 1) * 100 + "%)");

		// System.out.println("State Gen Time   " +
		// Simulation.stateGenerationTime + " ("
		// + ((Simulation.stateGenerationTime / stateGenTime) - 1) + "%)");

		System.out.println("MTTF             " + Simulation.meanTimeToFailure + " ("
				+ Math.abs((Simulation.meanTimeToFailure / mttf) - 1) * 100 + "%)");

		// System.out.println("SSU              " +
		// Simulation.steadyStateUnavailability + " ("
		// + ((Simulation.steadyStateUnavailability / ssu) - 1) + "%)");

		System.out.println("Total Trees      " + Simulation.numberOfTrees + " ("
				+ Math.abs((Simulation.numberOfTrees / totalTrees) - 1) * 100 + "%)");

		System.out.println("Avoided Trees    " + Simulation.numberOfAvoidedTrees + " ("
				+ Math.abs((Simulation.numberOfAvoidedTrees / avoidedTrees) - 1) * 100 + "%)");

		System.out.println("Unique Trees     " + Simulation.numberOfUniqueTrees + " ("
				+ Math.abs((Simulation.numberOfUniqueTrees / uniqueTrees) - 1) * 100 + "%)");
	}
}
