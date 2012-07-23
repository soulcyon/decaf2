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
import edu.njit.decaf2.generators.QMatrixGenerator;
import edu.njit.decaf2.generators.StateGenerator;
import edu.njit.decaf2.generators.TreeGenerator;

/**
 * DECAF 2 - Simulation
 *
 * @author Sashank Tadepalli
 * @version 2.0
 *
 */
public class Simulation extends DECAF {
	private boolean 						debug;
	private State[] 						transitionStates;
	private double[][] 						demandMatrix;
	private HashMap<String, FailureNode>	nodeMap = new HashMap<String, FailureNode>();
	private double[][]						qMatrix;
	
	/**
	 * Run through console.  Initializes new instance of Simulation and runs appropriate algorithms.
	 * @param args
	 */
	public static void main(String[] args){
		Simulation sim = new Simulation();
		sim.setDebug(true);
		sim.run();
	}
	
	/**
	 * Runs with CPU Time stopwatch.  Set DECAF.VerboseDebug = true for detailed statistics.
	 */
	private void debug_run(){
		if( !debug ){
			return;
		}

		double resultProcessing = 0.0;
		double t = System.nanoTime();
		loadSimulationData("data/input.xml");
		
		resultProcessing += System.nanoTime() - t;
		System.out.println("Time to Load XML:\t" + (System.nanoTime() - t)/1000.0/1000.0/1000.0);
		
		t = System.nanoTime();
		StateGenerator sg = new StateGenerator(nodeMap, demandMatrix);
		transitionStates = sg.generateStates();
		
		if( DECAF.VerboseDebug )
			System.out.println(transitionStates.length);
		
		resultProcessing += System.nanoTime() - t;
		System.out.println("Time to SG:\t" + (System.nanoTime() - t)/1000.0/1000.0/1000.0);

		if( DECAF.VerboseDebug )
			System.out.println(sg);
		
		t = System.nanoTime();
		
		String[] nodeKeyArray = new String[nodeMap.keySet().size()];
		nodeMap.keySet().toArray(nodeKeyArray);
		TreeGenerator tg = new TreeGenerator(nodeMap);
		QMatrixGenerator qg = new QMatrixGenerator(transitionStates, nodeKeyArray, demandMatrix, tg);
		qMatrix = qg.generateQMatrix();

		resultProcessing += System.nanoTime() - t;
		System.out.println("Time to QG:\t" + (System.nanoTime() - t)/1000.0/1000.0/1000.0);

		//if( DECAF.VerboseDebug )
			System.out.println("Generated Trees:\t" + tg.getCache().size() + " (reused " + tg.getMisses() + ")");
		
		if( DECAF.VerboseDebug )
			System.out.println(qg);
		
		System.out.println("Total CPU Time:\t"+ resultProcessing/1000.0/1000.0/1000.0);
	}
	
	/**
	 * ===Note===
	 * Deprecated until debug_run is finalized.
	 */
	private void run(){
		if( debug ){
			debug_run();
			return;
		}
	}
	
	/**
	 * Proprietary simulation configuration engine.
	 * 
	 * @param filename Path to XML configuration file
	 */
	private void loadSimulationData(String filename){
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
	 * @param debug the debug to set
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
	 * @param qMatrix the qMatrix to set
	 */
	public void setQMatrix(double[][] qMatrix) {
		this.qMatrix = qMatrix;
	}
}
