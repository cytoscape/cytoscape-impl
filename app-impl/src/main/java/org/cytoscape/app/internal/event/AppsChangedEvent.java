package org.cytoscape.app.internal.event;

import org.cytoscape.app.internal.manager.AppManager;

/**
 * An event used by the {@link AppManager} to notify AppsChangedListeners that an app has 
 * been added, removed, or changed and that the listeners (such as UI components) should 
 * update their data to reflect the change.
 */
public final class AppsChangedEvent {

	/** The {@link AppManager} that created the event. */
	private AppManager source;

	/**
	 * Create a new AppsChangedEvent.
	 * @param source The {@link AppManager} creating the event.
	 */
	public AppsChangedEvent(AppManager source) {
		this.source = source;
	}
	
	/**
	 * Find the source of the event.
	 * @return The {@link AppManager} that triggered the event.
	 */
	public AppManager getSource() {
		return source;
	}
}
