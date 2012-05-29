package org.cytoscape.app.internal.event;

/**
 * A listener used to detect when an app is added, removed, or changed. This is useful for UI components
 * to update their data when the list of apps has changed.
 */
public interface AppsChangedListener {
	
	/**
	 * Notifies the listener that apps have been updated, or added/removed from the list of available apps.
	 * @param event The {@link AppsChangedEvent} containing information about the app change event.
	 */
	public void appsChanged(AppsChangedEvent event);
}
