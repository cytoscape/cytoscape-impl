package org.cytoscape.ding.customgraphicsmgr.internal.event;

import org.cytoscape.event.CyListener;

public interface CustomGraphicsLibraryUpdatedListener extends CyListener {
	
	public void handleEvent(CustomGraphicsLibraryUpdatedEvent e);
}
