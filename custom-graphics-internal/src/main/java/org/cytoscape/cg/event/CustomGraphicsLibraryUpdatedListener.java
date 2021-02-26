package org.cytoscape.cg.event;

import org.cytoscape.event.CyListener;

public interface CustomGraphicsLibraryUpdatedListener extends CyListener {
	
	void handleEvent(CustomGraphicsLibraryUpdatedEvent e);
}
