package org.cytoscape.internal.view;

import org.cytoscape.application.swing.CytoPanelName;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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
			case EAST:       return EAST;
			case SOUTH:      return SOUTH;
			case SOUTH_WEST: return SOUTH_WEST;
			case WEST:       return WEST;
			default:         return BOTTOM;
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
		return this == CytoPanelNameInternal.BOTTOM ? "Command Panel" : toCytoPanelName().getTitle();
	}
}
