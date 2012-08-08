package org.cytoscape.app.internal.event;

/**
 * A listener used to detect when an update has become available, or installed. This is useful for UI components
 * to update their data when the list of updates has changed.
 */
public interface UpdatesChangedListener {
	
	/**
	 * Notifies the listener that updates have been updated, or added/removed from the list of available updates.
	 * @param event The {@link UpdatesChangedEvent} containing information about the change event.
	 */
	public void appsChanged(UpdatesChangedEvent event);
}
