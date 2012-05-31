package org.cytoscape.app.internal.util;

/**
 * A class used to manage print commands for aiding in debugging
 */
public class DebugHelper {
	public static void print(String message) {
		boolean debug = false;
		
		if (debug) {
			System.out.println(message);
		}
	}
}
