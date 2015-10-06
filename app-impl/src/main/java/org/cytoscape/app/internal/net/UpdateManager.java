package org.cytoscape.app.internal.net;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;

import org.cytoscape.app.event.AppsFinishedStartingEvent;
import org.cytoscape.app.event.AppsFinishedStartingListener;
import org.cytoscape.app.internal.event.UpdatesChangedEvent;
import org.cytoscape.app.internal.event.UpdatesChangedListener;
import org.cytoscape.app.internal.exception.AppDownloadException;
import org.cytoscape.app.internal.exception.AppInstallException;
import org.cytoscape.app.internal.exception.AppParsingException;
import org.cytoscape.app.internal.exception.AppUninstallException;
import org.cytoscape.app.internal.exception.AppUpdateException;
import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.App.AppStatus;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.manager.SimpleApp;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSite;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSitesManager;
import org.cytoscape.application.CyUserLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateManager implements AppsFinishedStartingListener {
	
	private static final Logger sysLogger = LoggerFactory.getLogger(UpdateManager.class);
	private static final Logger userLogger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	private AppManager appManager;
	private DownloadSitesManager downloadSitesManager;
	
	private List<UpdatesChangedListener> updatesChangedListeners;
	private Set<Update> updates;
	
	private Calendar lastUpdateCheckTime;
	
	private Object updateMutex = new Object();
	
	public UpdateManager(AppManager appManager, DownloadSitesManager downloadSitesManager) {
		this.appManager = appManager;
		this.downloadSitesManager = downloadSitesManager;
		
		this.updatesChangedListeners = new CopyOnWriteArrayList<UpdatesChangedListener>();
		this.updates = null;
		
		lastUpdateCheckTime = null;
	}
	
	/**
	 * Checks for updates using the given {@link WebQuerier} for the given set of apps.
	 * @param webQuerier The {@link WebQuerier} used to access app store data
	 * @param apps The set of apps to check for updates for
	 */
	public void checkForUpdates(Set<App> apps) {
		
		Set<Update> potentialUpdates = appManager.getWebQuerier().checkForUpdates(apps, appManager);
		
		synchronized (updateMutex) {
			this.updates = potentialUpdates;
			
			// Update last update check time
			lastUpdateCheckTime = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
		}
		
		
		fireUpdatesChangedEvent();
	}
	
	public Set<Update> getUpdates() {
		Set<Update> updatesCopy = new HashSet<Update>();
		
		synchronized (updateMutex) {
			for (Update update : updates) {
				updatesCopy.add(update);
			}
		}
		
		return updatesCopy;
	}
	
	public void addUpdatesChangedListener(UpdatesChangedListener updatesChangedListener) {
		this.updatesChangedListeners.add(updatesChangedListener);
	}
	
	public void removeUpdatesChangedListener(UpdatesChangedListener updatesChangedListener) {
		this.updatesChangedListeners.remove(updatesChangedListener);
	}
	
	private void fireUpdatesChangedEvent() {
		UpdatesChangedEvent updatesChangedEvent = new UpdatesChangedEvent(this);
		
		for (UpdatesChangedListener updatesChangedListener : updatesChangedListeners) {
			updatesChangedListener.updatesChanged(updatesChangedEvent);
		}
	}
	
	public Calendar getLastUpdateCheckTime() {
		return lastUpdateCheckTime;
	}

	@Override
	public void handleEvent(AppsFinishedStartingEvent event) {
		for (DownloadSite downloadSite : downloadSitesManager.getDownloadSites()) {
			appManager.getWebQuerier().setCurrentAppStoreUrl(downloadSite.getSiteUrl());
			appManager.getWebQuerier().getAllApps();
		}
		checkForUpdates(appManager.getInstalledApps());
		if(updates.size() > 0)
			userLogger.info(updates.size() + " " + 
						(updates.size() == 1 ? "update" : "updates") + " available" );
	}
}
