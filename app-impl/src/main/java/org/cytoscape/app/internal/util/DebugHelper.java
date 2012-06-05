package org.cytoscape.app.internal.util;

/**
 * A class used to manage print commands for aiding in debugging
 */
public class DebugHelper {
	private static boolean debug = false;
	
	public static void print(String message) {
		if (debug) {
			System.out.println(message);
		}
	}
	
	/**
	 * Prints a message with a source. This method is meant to be called as print(this, message).
	 * @param source The source, you can usually use the 'this' pointer.
	 * @param message The message to print
	 */
	public static void print(Object source, String message) {
		if (debug) {
			System.out.println("<" + source + ">: " + message);
		}
	}
}
