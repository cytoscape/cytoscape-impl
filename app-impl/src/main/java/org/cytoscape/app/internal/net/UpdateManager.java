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
 * Copyright (C) 2008 - 2026 The Cytoscape Consortium
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

		// [bell-diag] If listenerCount is 0 here, the bell action had not yet registered its listener
		// when the update set was computed (startup race) -> badge never receives the count.
		sysLogger.info("[bell-diag] fireUpdatesChangedEvent: notifying {} listener(s), updates.size()={}",
				updatesChangedListeners.size(), updates.size());

		for (UpdatesChangedListener listener : updatesChangedListeners)
			listener.updatesChanged(evt);
	}
	
	public Calendar getLastUpdateCheckTime() {
		return lastUpdateCheckTime;
	}

	@Override
	public void handleEvent(AppsFinishedStartingEvent evt) {
		sysLogger.info("[bell-diag] handleEvent(AppsFinishedStartingEvent) invoked on thread {}", Thread.currentThread().getName());
		final ExecutorService service = Executors.newSingleThreadExecutor();
		service.submit(() -> {
			// [bell-diag] Wrap the whole body: previously any exception here was captured in the
			// discarded Future and silently swallowed, leaving the update bell dark with no log trace.
			try {
				final int siteCount = downloadSitesManager.getDownloadSites().size();
				final int installedCount = appManager.getInstalledApps().size();
				sysLogger.info("[bell-diag] startup update check START: {} download site(s), {} installed app(s)", siteCount, installedCount);
				userLogger.info("[bell-diag] App update startup check started (" + siteCount + " site(s), " + installedCount + " installed app(s))");

				// [bell-diag][FIX] Always fetch the DEFAULT Cytoscape App Store catalog first.
				// The loop below only covers custom download sites (DownloadSitesManager), which
				// does NOT include the default store; without this fetch, checkForUpdates() compares
				// installed apps against an empty appsByUrl cache and finds nothing -> bell stays dark.
				appManager.getWebQuerier().setCurrentAppStoreUrl(WebQuerier.DEFAULT_APP_STORE_URL);
				final int defaultStoreCount = appManager.getWebQuerier().getAllApps().size();
				sysLogger.info("[bell-diag][FIX] default store getAllApps ({}) returned {} app(s)",
						WebQuerier.DEFAULT_APP_STORE_URL, defaultStoreCount);

				for (DownloadSite downloadSite : downloadSitesManager.getDownloadSites()) {
					appManager.getWebQuerier().setCurrentSiteName(downloadSite.getSiteName());
					appManager.getWebQuerier().setCurrentAppStoreUrl(downloadSite.getSiteUrl());
					final int appCount = appManager.getWebQuerier().getAllApps().size();
					sysLogger.info("[bell-diag] getAllApps for site '{}' ({}) returned {} app(s)",
							downloadSite.getSiteName(), downloadSite.getSiteUrl(), appCount);
				}

				checkForUpdates(appManager.getInstalledApps());

				// [bell-diag] Log the count UNCONDITIONALLY (the original only logged when > 0, so the
				// "found zero" case was invisible).
				sysLogger.info("[bell-diag] startup update check FINISHED: updates.size()={}", updates.size());
				userLogger.info("[bell-diag] App update startup check finished: " + updates.size() + " update(s) found");

				for (Update update : updates) {
					userLogger.info(
							"Update for " + update +
							" available (latest version: " + update.getUpdateVersion() + ", " + update.getApp().getVersion() + " installed)"
					);
				}

				if (updates.size() > 0)
					userLogger.info(updates.size() + " " + (updates.size() == 1 ? "update" : "updates") + " available");
			} catch (Throwable t) {
				sysLogger.error("[bell-diag] startup update check FAILED with an exception", t);
				userLogger.error("[bell-diag] App update startup check failed: " + t);
			}
		});
	}
}
