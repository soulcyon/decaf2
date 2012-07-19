/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.data;

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
	private TreeNode[]						children;
	private double							rate;
	private int								currentDemand;
	
	/**
	 * 
	 */
	public TreeNode(FailureNode root, int currentDemand){
		setRoot(root);
		setCurrentDemand(currentDemand);
		setRate(root.getFailureRates()[currentDemand]);
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
	 * 
	 * @param child
	 */
	public void addChild(FailureNode child){
		if( children == null )
			children = new TreeNode[0];

		TreeNode[] temp = new TreeNode[children.length + 1];
		TreeNode subTree = new TreeNode(child, currentDemand);
		subTree.setParentNode(this);
		for( int i = 0; i < children.length; i++ ){
			temp[i] = children[i];
		}
		temp[temp.length - 1] = subTree;
	}
	
	/**
	 * 
	 * @return
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
		return parentNode.getRoot();
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
}