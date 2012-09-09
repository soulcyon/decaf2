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
import edu.njit.decaf2.data.State;
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
	public static State[] states;
	public static double[][] demandMatrix;
	public static ArrayList<String> typeList = new ArrayList<String>();
	public static HashMap<String, FailureNode> nodeMap = new HashMap<String, FailureNode>();
	public static double[][] qMatrix;

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
		loadSimulationData("data/input.xml");

		resultProcessing += System.nanoTime() - t;

		System.out.println("XML Parsing Time:       " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0 + " secs");

		t = System.nanoTime();
		StateGenerator sg = new StateGenerator(nodeMap, typeList, demandMatrix);
		states = sg.generateStates();

		resultProcessing += System.nanoTime() - t;

		System.out.println("StateGenerator Time:    " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0 + " secs");

		System.out.println("States Count:           " + states.length);

		QMatrixGeneratorUnthreaded.init();

		if (DECAF.verboseDebug)
			System.out.println(sg);

		t = System.nanoTime();

		String[] nodeKeyArray = new String[nodeMap.keySet().size()];
		nodeMap.keySet().toArray(nodeKeyArray);
		// System.out.println(nodeMap);

		QMatrixGeneratorUnthreaded.generateQMatrix();

		resultProcessing += System.nanoTime() - t;
		// System.out.println(nodeMap);
		System.out.println("\nQMatrixGenerator Time:  " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0 + " secs");

		System.out.println("Total Trees:            " + QMatrixGeneratorUnthreaded.getTotalTrees());
		System.out.println("Reused Trees:           " + QMatrixGeneratorUnthreaded.getReusedTrees());
		
		t = System.nanoTime(); try { FileWriter fstream = new
		FileWriter("out.txt"); BufferedWriter out = new
		BufferedWriter(fstream);
		out.write(QMatrixGeneratorUnthreaded.printQMatrix()); out.close(); }
		catch (Exception e) { e.printStackTrace(); } resultProcessing +=
		System.nanoTime() - t;
		
		System.out.println("Total CPU Time:         " + resultProcessing / 1000.0 / 1000.0 / 1000.0 + " secs");
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

	/**
	 * @return the qMatrix
	 */
	public double[][] getQMatrix() {
		return qMatrix;
	}

	/**
	 * @param qMatrix
	 *            the qMatrix to set
	 */
	public void setQMatrix(double[][] qMatrix) {
		Simulation.qMatrix = qMatrix;
	}
}
