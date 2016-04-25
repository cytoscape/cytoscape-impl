package org.cytoscape.app.internal.util;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

/**
 * A class used to manage print commands for aiding in debugging
 */
public class DebugHelper {
	private static boolean debug = false;

	private DebugHelper() {
	}
//	private static boolean debug = true;
	
	public static void print(String message) {
		if (debug) {
			System.out.println("DebugHelper: " + message);
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
