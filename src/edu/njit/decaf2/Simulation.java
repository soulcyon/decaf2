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

/**
 * 
 * @author Sashank Tadepalli
 *
 */
public class Simulation extends DECAF {
	private boolean 						decaf_debug;
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

		
		double t = System.nanoTime();
		loadSimulationData("data/input.xml");
		System.out.println("Time to Load XML: " + (System.nanoTime() - t)/1000.0/1000.0/1000.0);
		
		t = System.nanoTime();
		StateGenerator sg = new StateGenerator(decaf_nodeMap, decaf_demandMatrix);
		decaf_transitionStates = sg.generateStates();
		
		System.out.println(decaf_transitionStates.length);
		System.out.println("Time to SG: " + (System.nanoTime() - t)/1000.0/1000.0/1000.0);
		//System.out.println(sg);
		
		t = System.nanoTime();
		
		String[] nodeKeyArray = new String[decaf_nodeMap.keySet().size()];
		decaf_nodeMap.keySet().toArray(nodeKeyArray);
		QMatrixGenerator qg = new QMatrixGenerator(decaf_transitionStates, nodeKeyArray, decaf_demandMatrix);
		setDecaf_qMatrix(qg.generateTransitionMatrix());
		
		System.out.println("Time to QG: " + (System.nanoTime() - t)/1000.0/1000.0/1000.0);
		//System.out.println(qg);
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
		setDecaf_qMatrix(qg.generateTransitionMatrix());
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
