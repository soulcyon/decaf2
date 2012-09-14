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

import com.ctc.wstx.sax.WstxSAXParserFactory;

import edu.njit.decaf2.data.FailureNode;
import edu.njit.decaf2.data.QMatrix;
import edu.njit.decaf2.data.State;
import edu.njit.decaf2.generators.Dependability;
import edu.njit.decaf2.generators.DependabilityUnthreaded;
import edu.njit.decaf2.generators.QMatrixGenerator;
import edu.njit.decaf2.generators.QMatrixGeneratorUnthreaded;
import edu.njit.decaf2.generators.StateGenerator;

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
	public static int numberOfTrees;
	public static int numberOfTransitions;
	
	public static State[] states;
	public static double[][] demandMatrix;
	public static ArrayList<String> typeList = new ArrayList<String>();
	public static HashMap<String, FailureNode> nodeMap = new HashMap<String, FailureNode>();
	
	/**
	 * Run through console. Initializes new instance of Simulation and runs
	 * appropriate algorithms.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Simulation sim = new Simulation();
		sim.setDebug(true);
		sim.run();
	}

	/**
	 * Runs with CPU Time stopwatch. Set DECAF.VerboseDebug = true for detailed
	 * statistics.
	 */
	private void debug_run() {
		if (!debug) {
			return;
		}

		double resultProcessing = 0.0;
		double t = System.nanoTime();

		/* ------------------ XML PARSING ------------------ */
		loadSimulationData("data/input.xml");

		resultProcessing += System.nanoTime() - t;

		System.out.println("XML Parsing:            " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0 + " s");


		/* ------------------ State Generation ------------------ */
		t = System.nanoTime();
		
		StateGenerator.generateStates();
		
		resultProcessing += System.nanoTime() - t;
		System.out.println("State Generation:       " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0 + " s");

		/* ------------------ QMatrix Generation ------------------ */
		t = System.nanoTime();
		if (DECAF.enableThreading) {
			QMatrixGenerator.init();
		} else {
			QMatrixGeneratorUnthreaded.init();
		}

		String[] nodeKeyArray = new String[nodeMap.keySet().size()];
		nodeMap.keySet().toArray(nodeKeyArray);

		if (DECAF.enableThreading) {
			QMatrixGenerator.generateQMatrix();
		} else {
			QMatrixGeneratorUnthreaded.generateQMatrix();
		}

		resultProcessing += System.nanoTime() - t;
		System.out.println("QMatrix Generation:     " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0 + " s");

		if (DECAF.sriniOutput) {
			t = System.nanoTime();
			try {
				FileWriter fstream = new FileWriter("output.txt");
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(QMatrix.generateCCM());
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			resultProcessing += System.nanoTime() - t;
		}
		
		/* ------------------ MTTF Calculation ------------------ */
		t = System.nanoTime();
		
		if (DECAF.enableThreading) {
			meanTimeToFailure = Dependability.calculateMTTF();
		} else {
			meanTimeToFailure = DependabilityUnthreaded.calculateMTTF();
		}

		System.out.println("Calculate MTTF:         " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0 + " s");
		resultProcessing += System.nanoTime() - t;
		
		/* ------------------ SSU Calculation ------------------ */
		t = System.nanoTime();
		
		if (DECAF.enableThreading) {
			steadyStateUnavailability = Dependability.calculateSSU();
		} else {
			steadyStateUnavailability = DependabilityUnthreaded.calculateSSU();
		}

		System.out.println("Calculate SSU:          " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0 + " s");
		resultProcessing += System.nanoTime() - t;
		
		System.out.println("Total CPU Time:         " + resultProcessing / 1000.0 / 1000.0 / 1000.0 + " s");

		/* ------------------ Generic Statistics ------------------ */
		System.out.println("");
		System.out.println("Number of States:       " + states.length);
		System.out.println("Number of Trees:        " + numberOfTrees);
		System.out.println("Number of Transitions:  " + numberOfTransitions);
		System.out.println("");
		System.out.println("Mean Time To Failure:   " + meanTimeToFailure + " s");
		System.out.println("SS Unavailability:      " + steadyStateUnavailability);
	}

	/**
	 * ===Note=== Deprecated until debug_run is finalized.
	 */
	private void run() {
		if (debug) {
			debug_run();
			return;
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
