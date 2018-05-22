package org.cytoscape.internal.view;

import org.cytoscape.application.swing.CytoPanelName;

/**
 * The BOTTOM option for the automation panel is not currently exposed as API.
 * This enum is a drop-in replacement for CytoPanelName to be used in this implementation bundle
 * as a substitute for CytoPanelName. If BOTTOM is ever made API delete this file and replace
 * all occurrences of CytoPanelNameInternal with CytoPanelName in this bundle.
 */
public enum CytoPanelNameInternal {
	SOUTH,
	EAST,
	WEST,
	SOUTH_WEST,
	BOTTOM;
	
	/**
	 * @throws NPE if cytoPanelName is null.
	 */
	public static CytoPanelNameInternal valueOf(CytoPanelName cytoPanelName) {
		switch(cytoPanelName) {
			case EAST: return EAST;
			case SOUTH: return SOUTH;
			case SOUTH_WEST: return SOUTH_WEST;
			case WEST: return WEST;
			default: return BOTTOM;
		}
	}

	public CytoPanelName toCytoPanelName() {
		switch(this) {
			case SOUTH:      return CytoPanelName.SOUTH;
			case EAST:       return CytoPanelName.EAST;
			case WEST:       return CytoPanelName.WEST;
			case SOUTH_WEST: return CytoPanelName.SOUTH_WEST;
			default:         return null;
		}
	}

	public String getTitle() {
		if(this == CytoPanelNameInternal.BOTTOM)
			return "Automation Panel";
		else
			return toCytoPanelName().getTitle();
	}
}
