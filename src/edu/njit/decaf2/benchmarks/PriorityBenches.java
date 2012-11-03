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
public class PriorityBenches {
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
	private static int start = 10;
	private static int increment = 25;
	private static int max = 1000;
	private static int length = 1 + ((max - start) / increment);
	private static double[] treeTimes = new double[length];
	private static double[] mttfs = new double[length];
	private static double[] treeEFF = new double[length];
	private static double[] mttfEFF = new double[length];
	private static double[] effs = new double[length];
	private static double[] treeCounts = new double[length];
	private static double[] avoidedCounts = new double[length];
	private static double[] uniqueCounts = new double[length];

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		DECAF.biasType = "high";
		DECAF.completeTreeRate = false;
		DECAF.heightThreshold = 100;
		DECAF.rateThreshold = 0;
		DECAF.treeThreshold = 1000000;
		Simulation sim = new Simulation();
		sim.run("data/input.xml");
		treeGenTime = Simulation.treeGenerationTime;
		stateGenTime = Simulation.stateGenerationTime;
		mttf = Simulation.meanTimeToFailure;
		ssu = Simulation.steadyStateUnavailability;
		totalTrees = Simulation.numberOfTrees;
		avoidedTrees = Simulation.numberOfAvoidedTrees;
		uniqueTrees = Simulation.numberOfUniqueTrees;
		resultCSV.append("States," + Simulation.states.length + "\n");
		
		printTrialResults();
		
		for( int i = start; i < max; i+=increment ){
			System.out.println("doing: " + i);
			// Run height threshold trials
			DECAF.treeThreshold = i;
			executeSimulation();
		}
		nextRun();
		
		if( toCSV ){
			try{
				FileWriter fstream = new FileWriter("Bench" + Simulation.states.length + ".30-" + DECAF.treeThreshold + ".csv");
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
		treeTimes[cCount] = Simulation.treeGenerationTime;
		treeEFF[cCount] = 1 - (Simulation.treeGenerationTime / treeGenTime);
		mttfs[cCount] = Simulation.meanTimeToFailure;
		mttfEFF[cCount] = Math.abs((Simulation.meanTimeToFailure / mttf) - 1);
		effs[cCount] = (1 - mttfEFF[cCount]) * treeEFF[cCount];
		treeCounts[cCount] = Simulation.numberOfTrees;
		avoidedCounts[cCount] = Simulation.numberOfAvoidedTrees;
		uniqueCounts[cCount] = Simulation.numberOfUniqueTrees;
		cCount++;
	}
	
	private static void nextRun(){
		resultCSV = resultCSV.append("\n").append("\n").append("\n").append("\n").append("\n").append("\n");
		
		for( int i = start; i < max; i+=increment ){
			resultCSV.append(",").append(i);
		}
		resultCSV.append("\n").append("Tree Gen Time");
		for( int i = 0; i < length; i++ ){
			resultCSV.append(",").append(treeTimes[i]);
		}
		resultCSV.append("\n").append("Tree Gen Discrep");
		for( int i = 0; i < length; i++ ){
			resultCSV.append(",").append(treeEFF[i]);
		}
		resultCSV.append("\n").append("MTTF");
		for( int i = 0; i < length; i++ ){
			resultCSV.append(",").append(mttfs[i]);
		}
		resultCSV.append("\n").append("MTTF Discrep");
		for( int i = 0; i < length; i++ ){
			resultCSV.append(",").append(mttfEFF[i]);
		}
		resultCSV.append("\n").append("Efficiency");
		for( int i = 0; i < length; i++ ){
			resultCSV.append(",").append(effs[i]);
		}
		resultCSV.append("\n").append("Total Trees");
		for( int i = 0; i < length; i++ ){
			resultCSV.append(",").append(treeCounts[i]);
		}
		resultCSV.append("\n").append("Avoided Trees");
		for( int i = 0; i < length; i++ ){
			resultCSV.append(",").append(avoidedCounts[i]);
		}
		resultCSV.append("\n").append("Unique Trees");
		for( int i = 0; i < length; i++ ){
			resultCSV.append(",").append(uniqueCounts[i]);
		}
		resultCSV.append("\n");
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
