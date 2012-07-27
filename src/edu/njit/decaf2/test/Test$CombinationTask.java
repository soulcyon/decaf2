/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.test;

import java.util.concurrent.RecursiveTask;

/**
 * DECAF 2 - Test$CombinationAction
 * 
 * @author Sashank Tadepalli
 *
 */
public class Test$CombinationTask extends RecursiveTask<String[]> {
	private String s;
	private String prefix;
	private static final long serialVersionUID = -5787561851763337673L;
	public Test$CombinationTask(String a, String b){
		prefix = a;
		s = b;
	}
	@Override
	protected String[] compute(){
        for (int i = 0; i < s.length(); i++)
            invokeAll(new Test$CombinationTask(prefix + s.charAt(i), s.substring(i + 1)));
		return new String[]{};
	}
}
