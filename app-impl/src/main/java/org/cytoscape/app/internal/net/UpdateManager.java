package org.cytoscape.app.internal.net;

import java.sql.Time;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.cytoscape.app.internal.event.UpdatesChangedEvent;
import org.cytoscape.app.internal.event.UpdatesChangedListener;
import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;

public class UpdateManager {
	
	private Set<UpdatesChangedListener> updatesChangedListeners;
	private Set<Update> updates;
	
	private Calendar lastUpdateCheckTime;
	
	public UpdateManager() {
		this.updatesChangedListeners = new HashSet<UpdatesChangedListener>();
		this.updates = null;
	}
	
	/**
	 * Checks for updates using the given {@link WebQuerier} for the given set of apps.
	 * @param webQuerier The {@link WebQuerier} used to access app store data
	 * @param apps The set of apps to check for updates for
	 */
	public void checkForUpdates(WebQuerier webQuerier, Set<App> apps, AppManager appManager) {
		
		Set<Update> potentialUpdates = webQuerier.checkForUpdates(apps, appManager);
		
		this.updates = potentialUpdates;
		
		fireUpdatesChangedEvent();
		
		// Update last update check time
		lastUpdateCheckTime = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
	}
	
	public Set<Update> getUpdates() {
		return this.updates;
	}
	
	/**
	 * Installs the given {@link Update} via the given {@link AppManager}
	 * @param update The update to install
	 * @param appManager The {@link AppManager} keeping track of current apps
	 */
	public void installUpdate(Update update, AppManager appManager) {
		
		if (this.updates.contains(update)) {
			this.updates.remove(update);
		}
		
		fireUpdatesChangedEvent();
	}
	
	private void fireUpdatesChangedEvent() {
		UpdatesChangedEvent updatesChangedEvent = new UpdatesChangedEvent(this);
		
		for (UpdatesChangedListener updatesChangedListener : updatesChangedListeners) {
			updatesChangedListener.appsChanged(updatesChangedEvent);
		}
	}
	
	public Calendar getLastUpdateCheckTime() {
		return lastUpdateCheckTime;
	}
}
