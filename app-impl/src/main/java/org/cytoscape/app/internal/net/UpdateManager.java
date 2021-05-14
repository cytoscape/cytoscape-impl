package org.cytoscape.app.internal.net;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.cytoscape.app.event.AppsFinishedStartingEvent;
import org.cytoscape.app.event.AppsFinishedStartingListener;
import org.cytoscape.app.internal.event.UpdatesChangedEvent;
import org.cytoscape.app.internal.event.UpdatesChangedListener;
import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSite;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSitesManager;
import org.cytoscape.application.CyUserLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2021 The Cytoscape Consortium
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

public class UpdateManager implements AppsFinishedStartingListener {
	
	private static final Logger sysLogger = LoggerFactory.getLogger(UpdateManager.class);
	private static final Logger userLogger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	private AppManager appManager;
	private DownloadSitesManager downloadSitesManager;
	
	private List<UpdatesChangedListener> updatesChangedListeners;
	private Set<Update> updates = new HashSet<>();
	
	private Calendar lastUpdateCheckTime;
	
	private Object updateMutex = new Object();
	
	public UpdateManager(AppManager appManager, DownloadSitesManager downloadSitesManager) {
		this.appManager = appManager;
		this.downloadSitesManager = downloadSitesManager;
		this.updatesChangedListeners = new CopyOnWriteArrayList<>();
	}
	
	/**
	 * Checks for updates for all installed apps.
	 * @param apps The set of apps to check for updates for
	 */
	public void checkForUpdates() {
		checkForUpdates(appManager.getInstalledApps());
	}
	
	/**
	 * Checks for updates for the given set of apps.
	 * @param apps The set of apps to check for updates for
	 */
	public void checkForUpdates(Set<App> apps) {
		Set<Update> set = appManager.getWebQuerier().checkForUpdates(apps, appManager);
		
		synchronized (updateMutex) {
			updates.clear();
			updates.addAll(set);
			
			// Update last update check time
			lastUpdateCheckTime = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
		}
		
		fireUpdatesChangedEvent();
	}
	
	public Set<Update> getUpdates() {
		Set<Update> set = new HashSet<>();
		
		synchronized (updateMutex) {
			set.addAll(updates);
		}
		
		return set;
	}
	
	public int getUpdateCount() {
		return updates.size();
	}
	
	public void addUpdatesChangedListener(UpdatesChangedListener listener) {
		this.updatesChangedListeners.add(listener);
	}
	
	public void removeUpdatesChangedListener(UpdatesChangedListener listener) {
		this.updatesChangedListeners.remove(listener);
	}
	
	private void fireUpdatesChangedEvent() {
		UpdatesChangedEvent evt = new UpdatesChangedEvent(this);
		
		for (UpdatesChangedListener listener : updatesChangedListeners)
			listener.updatesChanged(evt);
	}
	
	public Calendar getLastUpdateCheckTime() {
		return lastUpdateCheckTime;
	}

	@Override
	public void handleEvent(AppsFinishedStartingEvent evt) {
		final ExecutorService service = Executors.newSingleThreadExecutor();
		service.submit(() -> {
			for (DownloadSite downloadSite : downloadSitesManager.getDownloadSites()) {
				appManager.getWebQuerier().setCurrentSiteName(downloadSite.getSiteName());
				appManager.getWebQuerier().setCurrentAppStoreUrl(downloadSite.getSiteUrl());
				appManager.getWebQuerier().getAllApps();
			}
			
			checkForUpdates(appManager.getInstalledApps());
			
			for (Update update : updates) {
				userLogger.info(
						"Update for " + update + 
						" available (latest version: " + update.getUpdateVersion() + ", " + update.getApp().getVersion() + " installed)"
				);
			}
			
			if (updates.size() > 0)
				userLogger.info(updates.size() + " " + (updates.size() == 1 ? "update" : "updates") + " available");
		});
	}
}
