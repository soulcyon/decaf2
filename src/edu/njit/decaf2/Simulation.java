/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;

import com.ctc.wstx.sax.WstxSAXParserFactory;

import edu.njit.decaf2.generators.DependabilityUnthreaded;
import edu.njit.decaf2.generators.QMatrixGenerator;
import edu.njit.decaf2.generators.QMatrixGeneratorUnthreaded;
import edu.njit.decaf2.generators.StateGenerator;
import edu.njit.decaf2.structures.FailureNode;
import edu.njit.decaf2.structures.State;

/**
 * 
 * DECAF - Simulation
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public class Simulation {
	private boolean debug;
	public static double meanTimeToFailure;
	public static double steadyStateUnavailability;

	public static double treeGenerationTime;
	public static double stateGenerationTime;
	public static double mttfCalculationTime;
	public static double ssuCalculationTime;
	public static double qMatrixTime;
	public static int numberOfTrees;
	public static int numberOfAvoidedTrees;
	public static int numberOfUniqueTrees;
	public static int numberOfTransitions;

	public static DenseDoubleMatrix2D qmatrix;
	public static State[] states;
	public static HashMap<State, Integer> stateMap = new HashMap<State, Integer>();
	public static double[][] demandMatrix;
	public static ArrayList<String> typeList = new ArrayList<String>();
	public static HashMap<String, FailureNode> nodeMap = new HashMap<String, FailureNode>();

	public Simulation(){
		debug = true;
		meanTimeToFailure = 0;
		steadyStateUnavailability = 0;
		treeGenerationTime = 0;
		stateGenerationTime = 0;
		mttfCalculationTime = 0;
		ssuCalculationTime = 0;
		qMatrixTime = 0;
		
		numberOfTrees = 0;
		numberOfAvoidedTrees = 0;
		numberOfUniqueTrees = 0;
		numberOfTransitions = 0;
		
		qmatrix = null;
		states = new State[0];
		stateMap = new HashMap<State, Integer>();
		demandMatrix = new double[0][0];
		typeList = new ArrayList<String>();
		nodeMap = new HashMap<String, FailureNode>();
	}
	
	/**
	 * Run through console. Initializes new instance of Simulation and runs
	 * appropriate algorithms.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Simulation sim = new Simulation();
		sim.setDebug(true);
		sim.run("data/input.xml");
	}

	/**
	 * Runs with CPU Time stopwatch. Set DECAF.VerboseDebug = true for detailed
	 * statistics.
	 * @param qMatrixTime 
	 */
	private void debug_run(String input) {
		if (!debug) {
			return;
		}

		double resultProcessing = 0.0;
		double t = System.nanoTime();

		/* ------------------ XML PARSING ------------------ */
		loadSimulationData(input);

		resultProcessing += System.nanoTime() - t;

		/* ------------------ State Generation ------------------ */
		t = System.nanoTime();

		StateGenerator.generateStates();

		resultProcessing += System.nanoTime() - t;
		stateGenerationTime = (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0;

		for (int i = 0; i < states.length; i++) {
			stateMap.put(states[i], i);
		}

		/* ------------------ QMatrix Generation ------------------ */
		t = System.nanoTime();
		if (DECAF.enableThreading) {
			QMatrixGenerator.init();
		} else {
			QMatrixGeneratorUnthreaded.init();
		}

		String[] nodeKeyArray = new String[nodeMap.keySet().size()];
		nodeMap.keySet().toArray(nodeKeyArray);
		Simulation.qmatrix = new DenseDoubleMatrix2D(states.length, states.length);
		
		if (DECAF.enableThreading) {
			QMatrixGenerator.generateQMatrix();
		} else {
			QMatrixGeneratorUnthreaded.generateQMatrix();
		}

		resultProcessing += System.nanoTime() - t;
		qMatrixTime = (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0;

		if (DECAF.sriniOutput) {
			t = System.nanoTime();
			try {
				FileWriter fstream = new FileWriter("output.txt");
				BufferedWriter out = new BufferedWriter(fstream);
				out.write("On the way");
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			resultProcessing += System.nanoTime() - t;
		}

		/* ------------------ MTTF Calculation ------------------ */
		t = System.nanoTime();

		if (DECAF.enableThreading) {
			meanTimeToFailure = DependabilityUnthreaded.calculateMTTF();
		} else {
			meanTimeToFailure = DependabilityUnthreaded.calculateMTTF();
		}

		mttfCalculationTime = (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0;
		resultProcessing += System.nanoTime() - t;

		/* ------------------ SSU Calculation ------------------ */
		t = System.nanoTime();

		if (DECAF.enableThreading) {
			// steadyStateUnavailability =
			// DependabilityUnthreaded.calculateSSU();
		} else {
			// steadyStateUnavailability =
			// DependabilityUnthreaded.calculateSSU();
		}

		ssuCalculationTime = (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0;
		resultProcessing += System.nanoTime() - t;

		/* ------------------ Generic Statistics ------------------ */
		System.out.println("State Gen Time:           " + stateGenerationTime);
		System.out.println("Tree Gen Time:            " + treeGenerationTime);
		System.out.println("QMatrix Gen Time:         " + qMatrixTime);
		System.out.println("MTTF Calc Time:           " + mttfCalculationTime);
		System.out.println("SSU Calc Time:            " + ssuCalculationTime);
		System.out.println("");
		System.out.println("Number of States:         " + states.length);
		System.out.println("Number of Trees:          " + numberOfTrees);
		System.out.println("Number of Trees Avoided:  " + numberOfAvoidedTrees);
		System.out.println("Number of Unique Trees:   " + numberOfUniqueTrees);
		System.out.println("Number of F-Transitions:  " + numberOfTransitions);
		System.out.println("");
		System.out.println("Mean Time To Failure:     " + meanTimeToFailure + " s");
		System.out.println("SS Unavailability:        " + steadyStateUnavailability);
		System.out.println("Total CPU Time:           " + resultProcessing / 1000.0 / 1000.0 / 1000.0 + " s");
		
		System.out.println("im done");
		return;
	}

	/**
	 * ===Note=== Deprecated until debug_run is finalized.
	 */
	public void run(String input) {
		if (debug) {
			debug_run(input);
		}
	}

	/**
	 * Proprietary simulation configuration engine.
	 * 
	 * @param filename
	 *            Path to XML configuration file
	 */
	public void loadSimulationData(String filename) {
		try {
			File xmlFile = new File(filename);
			SAXParserFactory spf = new WstxSAXParserFactory();
			SAXParser so = spf.newSAXParser();
			so.parse(xmlFile, DECAF_SAXHandler.getInstance());

			demandMatrix = DECAF_SAXHandler.getDemand();
			nodeMap = DECAF_SAXHandler.getNodeMap();
			typeList = DECAF_SAXHandler.getTypeList();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * @param debug
	 *            the debug to set
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
