package org.cytoscape.internal.prefs.lib;


public interface INumberFormatter {
	String formattedDouble(Double d);
	/** Multiply the value by 100.  The implementation is given the option of providing extra display
	 * cues, such as a percentage sign. */
	String formattedPercentDouble(double d);
	/** Should return Double.NaN if string value cannot be parsed */
	double parseDouble(String s);
}