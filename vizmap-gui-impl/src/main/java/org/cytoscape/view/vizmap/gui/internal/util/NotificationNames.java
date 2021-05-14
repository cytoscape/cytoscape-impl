package org.cytoscape.view.vizmap.gui.internal.util;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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
 * PureMVC notification names must be added to this interface.
 */
public final class NotificationNames {

	/** Body: null */
	public static final String STARTUP = "STARTUP";
	/** Body: null */
	public static final String LOAD_DEFAULT_VISUAL_STYLES = "LOAD_DEFAULT_VISUAL_STYLES";
	/** Body: null */
	public static final String LOAD_VISUAL_STYLES = "LOAD_VISUAL_STYLES";
	/** Body: Set<VisualMappingFunction> */
	public static final String REMOVE_VISUAL_MAPPINGS = "REMOVE_VISUAL_MAPPINGS";
	/** Body: {@link org.cytoscape.view.vizmap.gui.internal.model.LockedValuesVO} */
	public static final String SET_LOCKED_VALUES = "SET_LOCKED_VALUES";
	/** Body: {@link org.cytoscape.view.vizmap.gui.internal.model.LockedValuesVO} */
	public static final String REMOVE_LOCKED_VALUES = "REMOVE_LOCKED_VALUES";
	
	// --- DATA UPDATED Events ---
	
	/** Body: SortedSet<VisualStyle> */
	public static final String VISUAL_STYLE_SET_CHANGED = "VISUAL_STYLE_SET_CHANGED";
	/** Body: {@link org.cytoscape.view.vizmap.VisualStyle} */
	public static final String VISUAL_STYLE_ADDED = "VISUAL_STYLE_ADDED";
	/** Body: {@link org.cytoscape.view.vizmap.VisualStyle} */
	public static final String VISUAL_STYLE_REMOVED = "VISUAL_STYLE_REMOVED";
	/** Body: {@link org.cytoscape.model.CyNetwork} */
	public static final String CURRENT_NETWORK_CHANGED = "CURRENT_NETWORK_CHANGED";
	/** Body: {@link org.cytoscape.view.model.CyNetworkView} */
	public static final String CURRENT_NETWORK_VIEW_CHANGED = "CURRENT_NETWORK_VIEW_CHANGED";
	/** Body: {@link org.cytoscape.view.vizmap.VisualStyle} */
	public static final String CURRENT_VISUAL_STYLE_CHANGED = "CURRENT_VISUAL_STYLE_CHANGED";
	/** Body: {@link org.cytoscape.view.vizmap.VisualStyle} */
	public static final String VISUAL_STYLE_UPDATED = "VISUAL_STYLE_UPDATED";
	/** Body: {@link org.cytoscape.view.vizmap.VisualStyle} */
	public static final String VISUAL_STYLE_NAME_CHANGED = "VISUAL_STYLE_NAME_CHANGED";
	
	public static final String CURRENT_TABLE_VISUAL_STYLE_CHANGED = "CURRENT_TABLE_VISUAL_STYLE_CHANGED";
	public static final String CURRENT_TABLE_CHANGED = "CURRENT_TABLE_CHANGED";
	
	/** Body: {@link java.util.Properties} */
	public static final String VIZMAP_PROPS_CHANGED = "VIZMAP_PROPS_CHANGED";
	
	
	private NotificationNames() {
		// restrict instantiation
	}
}
