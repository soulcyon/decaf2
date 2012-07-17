package edu.njit.decaf2;
/**
 * 
 * @author Sashank Tadepalli
 *
 */
public class DECAF {
	public String error(String message){
		return "[DECAF." + this.getClass().getSimpleName() + "::\n" + message + "]\n";
	}
}
