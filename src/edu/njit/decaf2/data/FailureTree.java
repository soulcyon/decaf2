package edu.njit.decaf2.data;

import edu.njit.decaf2.DECAF;

/**
 * ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |_____  |     | |            __|__ __|__
 *
 * @author Sashank Tadepalli
 *
 */
public class FailureTree extends DECAF {
	private FailureNode						root;
	private FailureTree[]					children;
	
	/**
	 * 
	 */
	public FailureTree(FailureNode root){
		setRoot(root);
	}
	
	/**
	 * 
	 */
	public FailureTree(FailureNode root, FailureTree[] children){
		setRoot(root);
		setChildren(children);
	}
	
	/**
	 * @return the root
	 */
	public FailureNode getRoot() {
		return root;
	}
	
	/**
	 * @param root the root to set
	 */
	public void setRoot(FailureNode root) {
		this.root = root;
	}
	
	/**
	 * @return the children
	 */
	public FailureTree[] getChildren() {
		return children;
	}
	
	/**
	 * @param children the children to set
	 */
	public void setChildren(FailureTree[] children) {
		this.children = children;
	}
	
	/**
	 * 
	 * @param child
	 */
	public void addChild(FailureTree child){
		FailureTree[] temp = new FailureTree[children.length + 1];
		for( int i = 0; i < children.length; i++ ){
			temp[i] = children[i];
		}
		temp[temp.length - 1] = child;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isLeaf(){
		return children == null || children.length == 0;
	}
}
