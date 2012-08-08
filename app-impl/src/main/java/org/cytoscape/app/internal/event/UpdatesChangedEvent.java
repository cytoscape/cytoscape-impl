package org.cytoscape.app.internal.event;

import org.cytoscape.app.internal.net.UpdateManager;

/**
 * An event used by the {@link UpdateManager} to notify UpdatesChangedListeners that an update
 * has become available or installed,  and that the listeners (such as UI components) should 
 * update their data to reflect the change.
 */
public final class UpdatesChangedEvent {

	/** The {@link UpdateManager} that created the event. */
	private UpdateManager source;

	/**
	 * Create a new UpdatesChangedEvent.
	 * @param source The {@link UpdateManager} creating the event.
	 */
	public UpdatesChangedEvent(UpdateManager source) {
		this.source = source;
	}
	
	/**
	 * Find the source of the event.
	 * @return The {@link UpdateManager} that triggered the event.
	 */
	public UpdateManager getSource() {
		return source;
	}
}
