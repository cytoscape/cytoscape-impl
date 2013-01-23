package org.cytoscape.ding;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import org.cytoscape.graph.render.stateful.NodeDetails;


public enum Justification {
	JUSTIFY_CENTER("Center Justified", "c", NodeDetails.LABEL_WRAP_JUSTIFY_CENTER), 
	JUSTIFY_LEFT("Left Justified", "l", NodeDetails.LABEL_WRAP_JUSTIFY_LEFT), 
	JUSTIFY_RIGHT("Right Justified", "r", NodeDetails.LABEL_WRAP_JUSTIFY_RIGHT);
	
	private static String[] JUSTIFY;

	private final String displayName;
	private final String shortName;
	
	private final int nativeConstant;

	private Justification(final String displayName, final String shortName, final int nativeConstant) {
		this.displayName = displayName;
		this.shortName = shortName;
		this.nativeConstant = nativeConstant;
	}

	public String getName() {
		return this.displayName;
	}

	public String getShortName() {
		return this.shortName;
	}
	
	public int getConversionConstant() {
		return this.nativeConstant;
	}
	
	public static Justification parse(final String value) {
		for (final Justification j : values()) {
			if (j.getName().equals(value) || j.getShortName().equals(value))
				return j;
		}
		return null;
	}


	public static String[] getNames() {
		// Lazy instantiation
		if(JUSTIFY == null) {
			JUSTIFY = new String[values().length];
			int i = 0;
			for(Justification j: values()) {
				JUSTIFY[i] = j.displayName;
				i++;
			}
		}
		
		return JUSTIFY;
	}

}
