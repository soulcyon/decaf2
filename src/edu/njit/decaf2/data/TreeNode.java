/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.data;

import java.util.ArrayList;
import java.util.Arrays;

import edu.njit.decaf2.DECAF;

/**
 * DECAF 2 - FailureTree
 *
 * @author Sashank Tadepalli
 * @version 2.0
 *
 */
public class TreeNode extends DECAF {
	private TreeNode						parentNode;
	private FailureNode						currentNode;
	private TreeNode[]						children = new TreeNode[0];
	private double							rate;
	private int								currentDemand;

	/**
	 * Sets {@link FailureNode} {@code root} and {@link Integer} {@code currentDemand}
	 * 
	 * @param root
	 * @param currentDemand
	 */
	public TreeNode(FailureNode root, int currentDemand){
		this.currentNode = root;
		this.currentDemand = currentDemand;
		this.rate = root.getFailureRates()[currentDemand];
	}
	
	/**
	 * Sets {@link FailureNode} {@code root}
	 * This constructor makes demand independent trees
	 * set rate will be used later for n lambda
	 * @param root
	 * @param currentDemand
	 */
	public TreeNode(FailureNode root){
		this.currentNode = root;
		this.rate = 0.0;
	}
	
	
	/**
	 * @return the root
	 */
	public FailureNode getRoot() {
		return currentNode;
	}
	
	/**
	 * @param root the root to set
	 */
	public void setRoot(FailureNode root) {
		this.currentNode = root;
	}
	
	/**
	 * @return the children
	 */
	public TreeNode[] getChildren() {
		return children;
	}
	
	/**
	 * clears out the children
	 */
	public void clearChildren() {
		children = new TreeNode[0];
	}
	
	/**
	 * Naive implementation of ArrayList method to addChild to array.
	 * 
	 * @param child
	 */
	public void addChild(FailureNode child){
		// TODO Analyze performance
		
		/*if( children == null )
			children = new TreeNode[0];

		TreeNode[] temp = new TreeNode[children.length + 1];
		TreeNode childNode = new TreeNode(child, currentDemand);
		childNode.parentNode = this;
		for( int i = 0; i < children.length; i++ ){
			temp[i] = children[i];
		}
		temp[temp.length - 1] = childNode;
		*/
		
		ArrayList<TreeNode> listOfChildren = new ArrayList<TreeNode>(Arrays.asList(children));
		TreeNode childNode = new TreeNode(child, currentDemand);
		childNode.parentNode = this;
		listOfChildren.add(childNode);
		children = (TreeNode[]) listOfChildren.toArray();

	}
	
	/**
	 * Returns true if this {@link TreeNode} is a leaf node.
	 * 
	 * @return true/false
	 */
	public boolean isLeaf(){
		return children == null || children.length == 0;
	}

	/**
	 * @return the rate
	 */
	public double getRate() {
		return rate;
	}

	/**
	 * @param rate the rate to set
	 */
	public void setRate(double rate) {
		if( this.parentNode == null ) return;
		
		this.parentNode.setRate(rate);
		this.rate = rate;
	}

	/**
	 * @return the parentNode
	 */
	public FailureNode getParentNode() {
		return parentNode.currentNode;
	}

	/**
	 * @param parentNode the parentNode to set
	 */
	public void setParentNode(TreeNode parentNode) {
		this.parentNode = parentNode;
	}

	/**
	 * @return the currentDemand
	 */
	public int getCurrentDemand() {
		return currentDemand;
	}

	/**
	 * @param currentDemand the currentDemand to set
	 */
	public void setCurrentDemand(int currentDemand) {
		this.currentDemand = currentDemand;
	}
	
	/**
	 * @return the FailureNode in this TreeNode
	 */
	public FailureNode getFailureNode() {
		return currentNode;
	}
}
