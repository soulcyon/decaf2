/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2;

/**
 * DECAF 2 - DECAF
 *
 * @author Sashank Tadepalli
 * @version 2.0
 *
 */
public class DECAF {
	public static boolean ForceStateDemandValidate = false;
	public static boolean VerboseDebug = true;
	
	public String error(String message){
		return "[DECAF." + this.getClass().getSimpleName() + "::\n" + message + "]\n";
	}
}
