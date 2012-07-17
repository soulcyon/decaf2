package edu.njit.decaf2.test;
import java.util.HashMap;

/**
 * 
 */

/**
 * @author Sashank Tadepalli
 *
 */
public class Test$ClassVariables {
	private double[][] 					testA;
	private HashMap<String, Double> 	testB;
	private int 						testC;
	private String 						testD;
	
	public static void main(String[] args){
		Test$ClassVariables tcv = new Test$ClassVariables();
		tcv.run();
	}
	
	private void run(){
		int max = 10000;
		this.testA = new double[max][max];
		
		/****************************************************************************/
		/* double[][] Bench */
		/****************************************************************************/
		double t = System.nanoTime();
		int i = 0;
		while( i++ < 1000000 ){
			double _ = this.testA[0][0];
		}
		System.out.println("Test A1: " + (System.nanoTime() - t)/1000.0/1000.0/1000.0);
		
		t = System.nanoTime();
		i = 0;
		while( i++ < 1000000 ){
			double _ = testA[0][0];
		}
		System.out.println("Test A2: " + (System.nanoTime() - t)/1000.0/1000.0/1000.0);
		
		t = System.nanoTime();
		i = 0;
		while( i++ < 100000 ){
			this.testA = new double[5][5];
		}
		System.out.println("Test A3: " + (System.nanoTime() - t)/1000.0/1000.0/1000.0);
		
		t = System.nanoTime();
		i = 0;
		while( i++ < 100000 ){
			testA = new double[5][5];
		}
		System.out.println("Test A4: " + (System.nanoTime() - t)/1000.0/1000.0/1000.0);
	}

	/**
	 * @return the testA
	 */
	public double[][] getTestA() {
		return testA;
	}

	/**
	 * @param testA the testA to set
	 */
	public void setTestA(double[][] testA) {
		this.testA = testA;
	}

	/**
	 * @return the testB
	 */
	public HashMap<String, Double> getTestB() {
		return testB;
	}

	/**
	 * @param testB the testB to set
	 */
	public void setTestB(HashMap<String, Double> testB) {
		this.testB = testB;
	}

	/**
	 * @return the testC
	 */
	public int getTestC() {
		return testC;
	}

	/**
	 * @param testC the testC to set
	 */
	public void setTestC(int testC) {
		this.testC = testC;
	}

	/**
	 * @return the testD
	 */
	public String getTestD() {
		return testD;
	}

	/**
	 * @param testD the testD to set
	 */
	public void setTestD(String testD) {
		this.testD = testD;
	}
}
