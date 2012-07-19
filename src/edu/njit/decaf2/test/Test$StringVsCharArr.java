/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.test;

/**
 * DECAF 2 - Test$StringVsCharArr
 *
 * @author Sashank Tadepalli
 * @version 2.0
 *
 */
public class Test$StringVsCharArr {
	private static String 				temp = "";
	private static char[] 				char1;
	private static char[] 				char2;
	
	public static void main(String[] args){
		/****************************************************************************/
		/* String */
		/****************************************************************************/
		int i = 0;
		int max = 1000000000;
		
		double t = System.nanoTime();
		while( i++ < max ){
			setTemp("1");
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
			char1 = new char[0];
			char2 = new char[]{ '1' };
			char1 = char2;
			char2 = char1;
		}
		System.out.println("Char Arr:");
		System.out.println((System.nanoTime() - t)/1000.0/1000.0/1000.0);
	}

	/**
	 * @return the temp
	 */
	public static String getTemp() {
		return temp;
	}

	/**
	 * @param temp the temp to set
	 */
	public static void setTemp(String temp) {
		Test$StringVsCharArr.temp = temp;
	}

	/**
	 * @return the char1
	 */
	public static char[] getChar1() {
		return char1;
	}

	/**
	 * @param char1 the char1 to set
	 */
	public static void setChar1(char[] char1) {
		Test$StringVsCharArr.char1 = char1;
	}
}
