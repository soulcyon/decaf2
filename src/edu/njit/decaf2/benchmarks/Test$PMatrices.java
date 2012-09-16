/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.benchmarks;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * DECAF - Test$PMatrices
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public class Test$PMatrices {

	/**
	 * @param matrixSize
	 * @return
	 */
	public static double[][] grab(int matrixSize) {
		try {
			double[][] result = new double[matrixSize][matrixSize];
			int currRow = 0;
			String temp = "";
			FileReader fstream = new FileReader("matrix." + matrixSize + ".txt");
			BufferedReader in = new BufferedReader(fstream);
			while( (temp = in.readLine()) != null ){
				for( int i = 0; i < temp.split(",").length; i++ ){
					result[currRow][i] = Double.parseDouble(temp.split(",")[i]);
				}
				currRow++;
			}
			in.close();
			return result;
		} catch(Exception e){
			e.printStackTrace();
			return new double[0][0];
		}
	}
}