package org.cytoscape.cg.event;

import org.cytoscape.event.AbstractCyEvent;
import org.cytoscape.session.CySession;

/**
 * Local event to handle graphics library update
 */
public final class CustomGraphicsReadyToBeLoadedEvent extends AbstractCyEvent<Object> {

	private final CySession session;
	
	public CustomGraphicsReadyToBeLoadedEvent(Object source, CySession session) {
		super(source, CustomGraphicsReadyToBeLoadedListener.class);
		this.session = session;
	}
	
	public CySession getLoadedSession() {
		return session;
	}
}
