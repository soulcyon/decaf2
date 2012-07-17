package edu.njit.decaf2;

/**
 * ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |_____  |     | |            __|__ __|__
 *
 * @author Sashank Tadepalli
 *
 */
public class DECAF {
	public String error(String message){
		return "[DECAF." + this.getClass().getSimpleName() + "::\n" + message + "]\n";
	}
}
