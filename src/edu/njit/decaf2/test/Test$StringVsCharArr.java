package edu.njit.decaf2.test;

/**
 * ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |_____  |     | |            __|__ __|__
 *
 * @author Sashank Tadepalli
 *
 */
public class Test$StringVsCharArr {
	public static void main(String[] args){
		/****************************************************************************/
		/* String */
		/****************************************************************************/
		int i = 0;
		int max = 1000000000;
		
		double t = System.nanoTime();
		while( i++ < max ){
			String temp = "1";
		}
		System.out.println("Strings:");
		System.out.println((System.nanoTime() - t)/1000.0/1000.0/1000.0);
		
		/****************************************************************************/
		/* CharArr */
		/****************************************************************************/
		i = 0;
		max = 1000000000;
		
		t = System.nanoTime();
		while( i++ < max ){
			char[] temp = new char[0];
			char[] temp2 = new char[]{ '1' };
			temp = temp2;
		}
		System.out.println("Char Arr:");
		System.out.println((System.nanoTime() - t)/1000.0/1000.0/1000.0);
	}
}
