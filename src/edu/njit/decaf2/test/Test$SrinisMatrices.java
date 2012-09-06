package edu.njit.decaf2.test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class Test$SrinisMatrices {
	public static void main(String[] args) {
		String newCode = "";
		String oldCode = "";
		try {
			FileInputStream fstream = new FileInputStream("out.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = "";
			
			while ((strLine = br.readLine()) != null) {
				newCode += strLine;
			}
			
			in.close();
			
			
			fstream = new FileInputStream("srini.txt");
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			strLine = "";
			
			while ((strLine = br.readLine()) != null) {
				oldCode += strLine;
			}
			
			in.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		String[] newSplit = newCode.split("; ");
		String[] oldSplit = oldCode.split("; ");
		int count = newSplit.length;
		if( count != oldSplit.length ){
			System.err.println("Cannot verify QMatrix: " + oldSplit.length + " vs " + count);
			System.exit(-1);
		}
		
		System.out.println("Total count of cells: " + count);
		System.out.println("\n==Listing Discrepencies==");
		for( int i = 0; i < count; i++ ){
			double newDouble = Double.parseDouble(newSplit[i].split("@")[0]);
			double oldDouble = Double.parseDouble(oldSplit[i].split("@")[0]);
			if(Math.abs((newDouble - oldDouble)/oldDouble) > 0.00000000001){
				System.out.println("Discrepency at " + i + " - " + newSplit[i] + " vs " + oldSplit[i]);
			}
		}
	}
}
