package org.cytoscape.plugin.internal;

import java.util.Map;

import org.cytoscape.application.events.CytoscapeStartEvent;
import org.cytoscape.application.swing.CySwingApplication; 
import org.cytoscape.event.CyEventHelper;
/**
 * A simple class whose instantiation indicates that startup is
 * largely (but not necessarily 100 percent) complete for Cytoscape.
 * This class should only exist once in the application and the reason it
 * exists here is that the plugin-impl has dependencies on nearly every
 * aspect of the system, so once those dependencies are fulfilled, we 
 * we can assume that the application is ready for use, and thus the
 * splash screen can be closed.
 */
class StartupMostlyFinished {

	public StartupMostlyFinished(final CyEventHelper eventHelper) {

		// fire event "start up mostly finished"
		eventHelper.fireEvent(new CytoscapeStartEvent(this));
	}	
}
