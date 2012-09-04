/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.test;

import java.util.HashMap;
import edu.njit.decaf2.data.FailureNode;
import edu.njit.decaf2.data.State;
import edu.njit.decaf2.data.TreeNode;

/**
 * DECAF 2 - Test$TreeGens
 * 
 * @author Sashank Tadepalli
 * 
 */
public class Test$TreeGens {
	private static HashMap<String, FailureNode> nodeMap = new HashMap<String, FailureNode>();

	public static void main(String[] args) {
		/****************************************************************************/
		/* TreeGen 1 */
		/****************************************************************************/
		double t = System.nanoTime();

		FailureNode a = new FailureNode(0, "A", 2, new double[] { 0.5, 0.5 });
		FailureNode b = new FailureNode(0, "B", 2, new double[] { 0.5, 0.5 });
		FailureNode c = new FailureNode(0, "C", 2, new double[] { 0.5, 0.5 });

		a.addCascadingFailure(b, 0.25);
		a.addCascadingFailure(c, 0.25);
		b.addCascadingFailure(a, 0.25);
		b.addCascadingFailure(c, 0.25);
		c.addCascadingFailure(a, 0.25);
		c.addCascadingFailure(b, 0.25);

		nodeMap.put("A", a);
		nodeMap.put("B", b);
		nodeMap.put("C", c);

		for (String k : nodeMap.keySet()) {
			TreeNode tempNode = new TreeNode(nodeMap.get(k), 0);
			tempNode.makeRoot();
			
			HashMap<String, Integer> failureState = new HashMap<String, Integer>();
			failureState.put("A", 0);
			failureState.put("B", 0);
			failureState.put("C", 0);
			//State tempState = new State(failureState, 0);
			//tempState.incrementComponentCount(nodeMap.get(k));
			
			//buildTrees(tempNode, tempState);
		}

		System.out.println("Test A1: " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0);
	}

	private static void buildTrees(TreeNode curr, State failureTransition) {
		HashMap<String, Double> cf = curr.getFailureNode().getCascadingFailures();
		String[] gamma = new String[cf.keySet().size()];
		cf.keySet().toArray(gamma);
		int gammaLength = gamma.length;

		System.out.println(curr.getChildren().length);
		for (int g = 0; g < (int) Math.pow(2, gammaLength); g++) {
			
			// If curr has no gamma, then do not assign gInBinary
			String gInBinary = "";
			if (gammaLength > 0) {
				gInBinary = String.format("%" + gammaLength + "s", Integer.toBinaryString(g)).replace(' ', '0');
			}
			for (int b = 0; b < gInBinary.length(); b++) {
				FailureNode temp = nodeMap.get(gamma[b]);
				if (gInBinary.charAt(b) == '1' && failureTransition.getComponentCount(gamma[b]) < temp.getRedundancy() ){
					System.out.println(":" + curr);
					curr.addChild(temp);
					System.out.println("Adding " + gamma[b] + " to " + curr.getFailureNode().getType());
					failureTransition.incrementComponentCount(temp);
				}
			}
			System.out.println(curr);
			for( TreeNode child : curr.getChildren() ){
				buildTrees(child, failureTransition.clone());
				child.clearChildren();
			}
		}
	}
}
