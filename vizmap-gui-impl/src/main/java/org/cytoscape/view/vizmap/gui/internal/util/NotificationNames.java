package org.cytoscape.view.vizmap.gui.internal.util;


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
	
	
	private NotificationNames() {
		// restrict instantiation
	}
}
