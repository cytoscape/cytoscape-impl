package org.cytoscape.view.vizmap.gui.internal;

import org.cytoscape.view.vizmap.gui.internal.controller.StartupCommand;
import org.cytoscape.view.vizmap.gui.internal.util.NotificationNames;
import org.puremvc.java.multicore.patterns.facade.Facade;

public class ApplicationFacade extends Facade {

	/** Key of this facade */
	public static final String NAME = "ApplicationFacade";

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public ApplicationFacade(final StartupCommand startupCommand) {
		super(NAME);
		
		if (startupCommand == null)
			throw new IllegalArgumentException("'startupCommand' must not be null");
		
		registerCommand(NotificationNames.STARTUP, startupCommand); // This command will register the other ones
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	/**
     * Start the application.
     */
	public final void startup() {
		sendNotification(NotificationNames.STARTUP);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
}
