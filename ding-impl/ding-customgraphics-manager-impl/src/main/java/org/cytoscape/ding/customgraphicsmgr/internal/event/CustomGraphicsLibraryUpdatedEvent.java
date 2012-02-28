package org.cytoscape.ding.customgraphicsmgr.internal.event;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.event.AbstractCyEvent;

/**
 * Local event to handle graphics library update
 *
 */
public final class CustomGraphicsLibraryUpdatedEvent extends AbstractCyEvent<CustomGraphicsManager>{

	public CustomGraphicsLibraryUpdatedEvent(CustomGraphicsManager source) {
		super(source, CustomGraphicsLibraryUpdatedListener.class);
	}
}
