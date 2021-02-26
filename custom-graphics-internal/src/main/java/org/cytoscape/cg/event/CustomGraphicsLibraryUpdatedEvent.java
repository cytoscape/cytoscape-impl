package org.cytoscape.cg.event;

import org.cytoscape.cg.model.CustomGraphicsManager;
import org.cytoscape.event.AbstractCyEvent;

/**
 * Local event to handle graphics library update
 */
public final class CustomGraphicsLibraryUpdatedEvent extends AbstractCyEvent<CustomGraphicsManager> {

	public CustomGraphicsLibraryUpdatedEvent(CustomGraphicsManager source) {
		super(source, CustomGraphicsLibraryUpdatedListener.class);
	}
}
