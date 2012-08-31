/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2;

import java.io.File;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.ctc.wstx.sax.WstxSAXParserFactory;

import edu.njit.decaf2.data.FailureNode;
import edu.njit.decaf2.data.State;
import edu.njit.decaf2.generators.QMatrixGeneratorUnthreaded;
import edu.njit.decaf2.generators.StateGenerator;

/**
 * DECAF 2 - Simulation
 * 
 * @author Sashank Tadepalli
 * @version 2.0
 * 
 */
public class Simulation extends DECAF {
	private boolean debug;
	public static State[] states;
	public static double[][] demandMatrix;
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

		System.out.println("LoadXML Time: \t\t" + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0 + " secs");

		t = System.nanoTime();
		StateGenerator sg = new StateGenerator(nodeMap, demandMatrix);
		states = sg.generateStates();

		resultProcessing += System.nanoTime() - t;

		System.out.println("StateGenerator Time: \t" + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0 + " secs");

		System.out.println("States Count: \t\t" + states.length);

		QMatrixGeneratorUnthreaded.init();

		if (verboseDebug)
			System.out.println(sg);

		t = System.nanoTime();

		String[] nodeKeyArray = new String[nodeMap.keySet().size()];
		nodeMap.keySet().toArray(nodeKeyArray);
		// System.out.println(nodeMap);

		qMatrix = QMatrixGeneratorUnthreaded.generateQMatrix();

		if (verboseDebug)
			System.out.println(QMatrixGeneratorUnthreaded.printQMatrix());

		resultProcessing += System.nanoTime() - t;
		// System.out.println(nodeMap);
		System.out.println("QMatrixGenerator Time:\t" + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0);

		System.out.println("Total CPU Time:\t\t" + resultProcessing / 1000.0 / 1000.0 / 1000.0 + " secs");
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
