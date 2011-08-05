package org.cytoscape.task.internal.quickstart.datasource;


/**
 * Takes a line of entry and returns one SIF entry.
 *
 */
public interface LineProcessor {
	
	/**
	 * 
	 * @param line
	 * @return SIF formatted line (source (interaction) target).
	 */
	String processLine(final String line);
}
