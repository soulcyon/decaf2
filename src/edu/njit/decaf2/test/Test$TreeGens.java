/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.test;

import java.util.HashMap;
import edu.njit.decaf2.data.FailureNode;

/**
 * DECAF 2 - Test$TreeGens
 * 
 * @author Sashank Tadepalli
 *
 */
public class Test$TreeGens {
	private static HashMap<Object, Double> 			stateCache = new HashMap<Object, Double>();
	private static HashMap<FailureNode, Integer> 	stateCounter = new HashMap<FailureNode, Integer>();
	private static HashMap<String, FailureNode> 	nodeMap = new HashMap<String, FailureNode>();
	
	public static void main(String[] args){
		/****************************************************************************/
		/* TreeGen 1 */
		/****************************************************************************/
		double t = System.nanoTime();
		
		FailureNode a = new FailureNode(0, "A", 2, new double[]{0.5, 0.5});
		FailureNode b = new FailureNode(0, "B", 2, new double[]{0.5, 0.5});
		FailureNode c = new FailureNode(0, "C", 2, new double[]{0.5, 0.5});
		
		a.addCascadingFailure(b, 0.25);
		a.addCascadingFailure(c, 0.25);
		b.addCascadingFailure(a, 0.25);
		b.addCascadingFailure(c, 0.25);
		c.addCascadingFailure(a, 0.25);
		c.addCascadingFailure(b, 0.25);
		
		nodeMap.put("A", a);
		nodeMap.put("B", b);
		nodeMap.put("C", c);
		
		stateCounter.put(a, 0);
		stateCounter.put(b, 0);
		stateCounter.put(c, 0);
		for( String k : nodeMap.keySet() ){
			System.out.println("Root : " + k + " => " + combNodes(nodeMap.get(k)));
			stateCounter.put(a, 0);
			stateCounter.put(b, 0);
			stateCounter.put(c, 0);
		}
		for( Object k : stateCache.keySet() ){
			System.out.println(k + ":" + stateCache.get(k));
		}
		System.out.println("Test A1: " + (System.nanoTime() - t)/1000.0/1000.0/1000.0);
	}
	
	private static double combNodes(FailureNode root){
		return combNodes(root.getFailureRates()[0], root, "");
	}
	
	/**
	 * @param d
	 * @param children
	 * @param is
	 */
	private static double combNodes(double d, FailureNode root, String t) {
		System.out.println(t + " " + root.getType() + " @ " + stateCounter.get(root));
		HashMap<String, Double> failures = root.getCascadingFailures();
		stateCounter.put(root, stateCounter.get(root) + 1);
		if( stateCounter.get(root) <= root.getRedundancy() ){
			for( String k : failures.keySet() ){
				d += combNodes(d * failures.get(k), nodeMap.get(k), t + "==");
			}
		}
		for( String k : failures.keySet() ){
			d *= 1 - failures.get(k);
		}
		stateCache.put(stateCounter.clone(), d);
		return d;
	}
}
