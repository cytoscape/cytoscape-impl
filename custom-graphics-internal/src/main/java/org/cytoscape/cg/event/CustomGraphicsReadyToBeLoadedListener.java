package org.cytoscape.cg.event;

import org.cytoscape.event.CyListener;

public interface CustomGraphicsReadyToBeLoadedListener extends CyListener {
	
	void handleEvent(CustomGraphicsReadyToBeLoadedEvent e);
}
