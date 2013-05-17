package org.cytoscape.view.vizmap.gui.internal;

import org.cytoscape.view.vizmap.gui.internal.controller.StartupCommand;
import org.puremvc.java.multicore.patterns.facade.Facade;

public class ApplicationFacade extends Facade {

	/** Key of this facade */
	public static final String NAME = "ApplicationFacade";

	/** Body: null */
	public static final String STARTUP = "STARTUP";
	/** Body: null */
	public static final String LOAD_DEFAULT_VISUAL_STYLES = "LOAD_DEFAULT_VISUAL_STYLES";
	/** Body: null */
	public static final String LOAD_VISUAL_STYLES = "LOAD_VISUAL_STYLES";
	
	// --- DATA UPDATED Events ---
	
	/** Body: SortedSet<VisualStyle> */
	public static final String VISUAL_STYLE_SET_CHANGED = "VISUAL_STYLE_SET_CHANGED";
	/** Body: CyNetworkView */
	public static final String CURRENT_NETWORK_VIEW_CHANGED = "CURRENT_NETWORK_VIEW_CHANGED";
	/** Body: VisualStyle */
	public static final String CURRENT_VISUAL_STYLE_CHANGED = "CURRENT_VISUAL_STYLE_CHANGED";
	/** Body: VisualStyle */
	public static final String VISUAL_STYLE_UPDATED = "VISUAL_STYLE_UPDATED";
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public ApplicationFacade(final StartupCommand startupCommand) {
		super(NAME);
		
		if (startupCommand == null)
			throw new IllegalArgumentException("'startupCommand' must not be null");
		
		registerCommand(STARTUP, startupCommand); // This command will register the other ones
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	/**
     * Start the application.
     */
	public final void startup() {
		sendNotification(STARTUP);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
}
