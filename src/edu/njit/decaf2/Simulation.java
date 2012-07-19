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
	private boolean 						decaf_debug;
	private boolean							decaf_debugVerbose;
	private State[] 						decaf_transitionStates;
	private double[][] 						decaf_demandMatrix;
	private HashMap<String, FailureNode>	decaf_nodeMap = new HashMap<String, FailureNode>();
	private double[][]						decaf_qMatrix;
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		Simulation sim = new Simulation();
		sim.setDebug(true);
		sim.run();
	}
	
	/**
	 * 
	 */
	private void debug_run(){
		if( !decaf_debug ){
			return;
		}

		double resultProcessing = 0.0;
		double t = System.nanoTime();
		loadSimulationData("data/input.xml");
		
		resultProcessing += System.nanoTime() - t;
		System.out.println("Time to Load XML:\t" + (System.nanoTime() - t)/1000.0/1000.0/1000.0);
		
		t = System.nanoTime();
		StateGenerator sg = new StateGenerator(decaf_nodeMap, decaf_demandMatrix);
		decaf_transitionStates = sg.generateStates();
		
		if( decaf_debugVerbose )
			System.out.println(decaf_transitionStates.length);
		
		resultProcessing += System.nanoTime() - t;
		System.out.println("Time to SG:\t" + (System.nanoTime() - t)/1000.0/1000.0/1000.0);

		if( decaf_debugVerbose )
			System.out.println(sg);
		
		t = System.nanoTime();
		
		String[] nodeKeyArray = new String[decaf_nodeMap.keySet().size()];
		decaf_nodeMap.keySet().toArray(nodeKeyArray);
		QMatrixGenerator qg = new QMatrixGenerator(decaf_transitionStates, nodeKeyArray, decaf_demandMatrix);
		TreeGenerator tg = new TreeGenerator(decaf_nodeMap);
		qg.setTreeGenerator(tg);
		setDecaf_qMatrix(qg.generateQMatrix());

		resultProcessing += System.nanoTime() - t;
		System.out.println("Time to QG:\t" + (System.nanoTime() - t)/1000.0/1000.0/1000.0);

		if( decaf_debugVerbose )
			System.out.println("Generated Trees:\t" + tg.getStateCache().size() + " (reused " + tg.getMisses() + ")");
		
		if( decaf_debugVerbose )
			System.out.println(qg);
		
		System.out.println("Total CPU Time:\t"+ resultProcessing/1000.0/1000.0/1000.0);
	}
	
	/**
	 * 
	 */
	private void run(){
		if( decaf_debug ){
			debug_run();
			return;
		}
		
		loadSimulationData("data/input.xml");
		StateGenerator sg = new StateGenerator(decaf_nodeMap, decaf_demandMatrix);
		decaf_transitionStates = sg.generateStates();
		
		String[] nodeKeyArray = new String[decaf_nodeMap.keySet().size()];
		decaf_nodeMap.keySet().toArray(nodeKeyArray);
		QMatrixGenerator qg = new QMatrixGenerator(decaf_transitionStates, nodeKeyArray, decaf_demandMatrix);
		setDecaf_qMatrix(qg.generateQMatrix());
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
			
			decaf_demandMatrix = DECAF_SAXHandler.getDemand();
			decaf_nodeMap = DECAF_SAXHandler.getNodeMap();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * @param decaf_debug the decaf_debug to set
	 */
	public void setDebug(boolean decaf_debug) {
		this.decaf_debug = decaf_debug;
	}

	/**
	 * @return the decaf_qMatrix
	 */
	public double[][] getDecaf_qMatrix() {
		return decaf_qMatrix;
	}

	/**
	 * @param decaf_qMatrix the decaf_qMatrix to set
	 */
	public void setDecaf_qMatrix(double[][] decaf_qMatrix) {
		this.decaf_qMatrix = decaf_qMatrix;
	}
}
