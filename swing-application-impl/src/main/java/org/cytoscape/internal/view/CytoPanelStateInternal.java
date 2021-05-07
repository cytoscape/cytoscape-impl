package org.cytoscape.internal.view;

import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelState;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
 * Some new states (from version 3.8) can not be exposed as API, for backwards compatibility.
 * This enum is a drop-in replacement for {@link CytoPanelState} to be used in this implementation bundle
 * as a substitute for {@link CytoPanelState}. If the new states () are ever made API delete this file and
 * replace all occurrences of CytoPanelStateInternal with CytoPanelState in this bundle.
 */
public enum CytoPanelStateInternal {
	/**
	 * The {@link CytoPanel} will be hidden and its buttons removed from the sidebar.
	 * This should only be set by the end user.
	 */
	HIDE,

	/**
	 * The {@link CytoPanel} will be open and appear as a separate frame, 
	 * independent of the application.
	 */
	FLOAT,

	/**
	 * The {@link CytoPanel} will be open and appear as a nested container within the application.
	 */
	DOCK,
	
	// These are not exposed as API:
	/**
	 * The {@link CytoPanel} will be open and appear as an undocked panel, over the application's glass pane.
	 */
	UNDOCK,
	
	/**
	 * The {@link CytoPanel} itself will be hidden but it will show its sidebar buttons.
	 */
	MINIMIZE,
	;
	
	/**
	 * @throws NPE if state is null.
	 */
	public static CytoPanelStateInternal valueOf(CytoPanelState state) {
		switch (state) {
			case HIDE:  return HIDE;
			case FLOAT: return FLOAT;
			case DOCK:  return DOCK;
			default:    return HIDE;
		}
	}

	public CytoPanelState toCytoPanelState() {
		switch (this) {
			case DOCK:
			case UNDOCK:   return CytoPanelState.DOCK;
			case FLOAT:    return CytoPanelState.FLOAT;
			case HIDE:
			case MINIMIZE:
			default:       return CytoPanelState.HIDE;
		}
	}
}
