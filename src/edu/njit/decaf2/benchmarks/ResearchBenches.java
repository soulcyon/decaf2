/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.benchmarks;

import java.io.BufferedWriter;
import java.io.FileWriter;

import edu.njit.decaf2.DECAF;
import edu.njit.decaf2.DECAF_SAXHandler;
import edu.njit.decaf2.Simulation;

/**
 * DECAF - ResearchBenches_LowBias
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public class ResearchBenches {
	private static boolean toCSV = true;
	private static StringBuffer resultCSV = new StringBuffer();
	
	public static double treeGenTime = 0;
	public static double stateGenTime = 0;
	public static double mttf = 0;
	public static double ssu = 0;
	public static double totalTrees = 0;
	public static double avoidedTrees = 0;
	public static double uniqueTrees = 0;
	
	private static int cCount = 0;
	private static String[] types = new String[4];
	private static double[] effs = new double[4];
	private static double[] treeTimes = new double[8];
	private static double[] mttfs = new double[8];
	private static double[] treeCounts = new double[8];
	private static double[] uniqueCounts = new double[8];

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		DECAF.biasType = "high";
		DECAF.completeTreeRate = false;
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
		uniqueTrees = Simulation.numberOfUniqueTrees;
		resultCSV.append("States," + Simulation.states.length + "\n");
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
		nextRun();
		
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
		nextRun();
		
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
		nextRun();

		DECAF.heightThreshold = 4;
		DECAF.rateThreshold = 0.1;
		executeSimulation();

		DECAF.rateThreshold = 0.01;
		executeSimulation();

		DECAF.rateThreshold = 0.001;
		executeSimulation();

		DECAF.rateThreshold = 0.0001;
		executeSimulation();
		nextRun();

		DECAF.heightThreshold = 5;
		DECAF.rateThreshold = 0.1;
		executeSimulation();

		DECAF.rateThreshold = 0.01;
		executeSimulation();

		DECAF.rateThreshold = 0.001;
		executeSimulation();

		DECAF.rateThreshold = 0.0001;
		executeSimulation();
		nextRun();

		DECAF.heightThreshold = 6;
		DECAF.rateThreshold = 0.1;
		executeSimulation();

		DECAF.rateThreshold = 0.01;
		executeSimulation();

		DECAF.rateThreshold = 0.001;
		executeSimulation();

		DECAF.rateThreshold = 0.0001;
		executeSimulation();
		nextRun();
		
		if( toCSV ){
			try{
				FileWriter fstream = new FileWriter("Bench" + Simulation.states.length + "." + DECAF.biasType + ".csv");
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(resultCSV.toString());
				out.close();
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 */
	private static void executeSimulation() {
		DECAF_SAXHandler.clear();
		Simulation sim = new Simulation();
		sim.run("data/input.xml");
		
		if( toCSV ){
			buildCSV();
		} else {
			printTrialResults();
		}
		System.gc();
		Runtime.getRuntime().gc();
	}
	
	private static void buildCSV(){
		types[cCount/2] = DECAF.heightThreshold + "h / " + DECAF.rateThreshold + "r";
		treeTimes[cCount] = Simulation.treeGenerationTime;
		treeTimes[cCount + 1] = 1 - (Simulation.treeGenerationTime / treeGenTime);
		
		mttfs[cCount] = Simulation.meanTimeToFailure;
		mttfs[cCount + 1] = Math.abs((Simulation.meanTimeToFailure / mttf) - 1);
		
		effs[cCount/2] = (1 - mttfs[cCount + 1]) * treeTimes[cCount + 1];
		
		treeCounts[cCount] = Simulation.numberOfTrees;
		treeCounts[cCount + 1] = 1 - (Simulation.numberOfTrees / totalTrees);
		
		uniqueCounts[cCount] = Simulation.numberOfUniqueTrees;
		uniqueCounts[cCount + 1] = 1 - (Simulation.numberOfUniqueTrees / uniqueTrees);
		cCount+=2;
	}
	
	private static void nextRun(){
		resultCSV = resultCSV.append("\n").append(",,")
		.append(types[0]).append(",").append("Discrep %").append(",")
		.append(types[1]).append(",").append("Discrep %").append(",")
		.append(types[2]).append(",").append("Discrep %").append(",")
		.append(types[3]).append(",").append("Discrep %").append("\n")
		.append("Tree Time,")
		.append(treeGenTime).append(",")
		.append(treeTimes[0]).append(",")
		.append(treeTimes[1] * 100 + "%").append(",")
		.append(treeTimes[2]).append(",")
		.append(treeTimes[3] * 100 + "%").append(",")
		.append(treeTimes[4]).append(",")
		.append(treeTimes[5] * 100 + "%").append(",")
		.append(treeTimes[6]).append(",")
		.append(treeTimes[7] * 100 + "%").append("\n")
		.append("MTTF,")
		.append(mttf).append(",")
		.append(mttfs[0]).append(",")
		.append(mttfs[1] * 100 + "%").append(",")
		.append(mttfs[2]).append(",")
		.append(mttfs[3] * 100 + "%").append(",")
		.append(mttfs[4]).append(",")
		.append(mttfs[5] * 100 + "%").append(",")
		.append(mttfs[6]).append(",")
		.append(mttfs[7] * 100 + "%").append("\n")
		.append("Efficiency").append(",,")
		.append(effs[0]).append(",,")
		.append(effs[1]).append(",,")
		.append(effs[2]).append(",,")
		.append(effs[3]).append("\n")
		.append("Total Trees,")
		.append(totalTrees).append(",")
		.append(treeCounts[0]).append(",")
		.append(treeCounts[1] * 100 + "%").append(",")
		.append(treeCounts[2]).append(",")
		.append(treeCounts[3] * 100 + "%").append(",")
		.append(treeCounts[4]).append(",")
		.append(treeCounts[5] * 100 + "%").append(",")
		.append(treeCounts[6]).append(",")
		.append(treeCounts[7] * 100 + "%").append("\n")
		.append("Unique Trees,")
		.append(uniqueTrees).append(",")
		.append(uniqueCounts[0]).append(",")
		.append(uniqueCounts[1] * 100 + "%").append(",")
		.append(uniqueCounts[2]).append(",")
		.append(uniqueCounts[3] * 100 + "%").append(",")
		.append(uniqueCounts[4]).append(",")
		.append(uniqueCounts[5] * 100 + "%").append(",")
		.append(uniqueCounts[6]).append(",")
		.append(uniqueCounts[7] * 100 + "%").append("\n");
		cCount = 0;
		types = new String[4];
		effs = new double[4];
		treeTimes = new double[8];
		mttfs = new double[8];
		treeCounts = new double[8];
		uniqueCounts = new double[8];
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
		
		System.out.println("Unique Trees     " + Simulation.numberOfUniqueTrees + " ("
				+ Math.abs((Simulation.numberOfUniqueTrees / uniqueTrees) - 1) * 100 + "%)");
	}
}
