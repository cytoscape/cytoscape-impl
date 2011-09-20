package org.cytoscape.ding;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Constants represent position of graphics objects. This is Swing-independent
 * definition.
 * 
 */
public enum Position {

	NORTH_WEST("Northwest", "NW", Conversion.NW), NORTH("North", "N", Conversion.N), 
	NORTH_EAST("Northeast","NE", Conversion.NE), WEST("West", "W", Conversion.W), 
	CENTER("Center", "C", Conversion.C), EAST("East", "E", Conversion.E), 
	NONE("None", "NONE", Conversion.NONE), SOUTH_WEST("Southwest", "SW", Conversion.SW), 
	SOUTH("South", "S", Conversion.S), SOUTH_EAST("Southeast", "SE", Conversion.SE);

	private static SortedSet<String> displayNames;

	private final String displayName;
	private final String shortName;
	private final int conversionConstant;

	private Position(final String displayName, final String shortName, final int conversionConstant) {
		this.displayName = displayName;
		this.shortName = shortName;
		this.conversionConstant = conversionConstant;
	}

	public String getName() {
		return this.displayName;
	}

	public String getShortName() {
		return this.shortName;
	}
	
	public int getConversionConstant() {
		return this.conversionConstant;
	}

	/**
	 * Accepts both short/display names.
	 * 
	 * @param value
	 * @return
	 */
	public static Position parse(final String value) {
		for (final Position p : Position.values()) {
			if (p.getName().equals(value) || p.getShortName().equals(value))
				return p;
		}

		// If not found, return center.
		return CENTER;
	}
	
	public static Position parse(final int conversionConstant) {
		for (final Position p : Position.values()) {
			if (p.getConversionConstant() == conversionConstant)
				return p;
		}
		
		return null;
	}
	
	public static SortedSet<String> getDisplayNames() {
		if(displayNames == null) {
			displayNames = new TreeSet<String>();
			for(Position p: values()) {
				displayNames.add(p.displayName);
			}
		}
		
		return displayNames;
	}
	
	private static final class Conversion {
		// TODO: remove these numbers.
		private static final int NW = 0;
		private static final int N = 1;
		private static final int NE = 2;
		private static final int W = 3;
		private static final int C = 4;
		private static final int E = 5;
		private static final int SW = 6;
		private static final int S = 7;
		private static final int SE = 8;
		private static final int NONE = 127;
	}
}
