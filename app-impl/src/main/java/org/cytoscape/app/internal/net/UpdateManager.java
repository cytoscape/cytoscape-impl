package org.cytoscape.app.internal.net;

import java.io.File;
import java.sql.Time;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.cytoscape.app.internal.event.UpdatesChangedEvent;
import org.cytoscape.app.internal.event.UpdatesChangedListener;
import org.cytoscape.app.internal.exception.AppDownloadException;
import org.cytoscape.app.internal.exception.AppInstallException;
import org.cytoscape.app.internal.exception.AppParsingException;
import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.util.DebugHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateManager {
	
	private static final Logger logger = LoggerFactory.getLogger(UpdateManager.class);
	
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
		
		WebQuerier webQuerier = appManager.getWebQuerier();
		
		File appFile = null;
		try {
			appFile = webQuerier.downloadApp(update.getWebApp(), 
					update.getRelease().getReleaseVersion(), 
					new File(appManager.getDownloadedAppsPath()));
		} catch (AppDownloadException e) {
			logger.warn("Failed to obtain update for " 
					+ update.getApp().getAppName() + ", " + e.getMessage());
			return;
		}
		
		if (appFile != null) {
    		
    		App parsedApp;
			try {
				// Parse app
				parsedApp = appManager.getAppParser().parseApp(appFile);
				
				// Install app
				appManager.installApp(parsedApp);
				
				if (this.updates.contains(update)) {
					this.updates.remove(update);
				}
				
				fireUpdatesChangedEvent();
			} catch (AppParsingException e) {
				e.printStackTrace();
			} catch (AppInstallException e) {
				e.printStackTrace();
			}    		
		}
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
