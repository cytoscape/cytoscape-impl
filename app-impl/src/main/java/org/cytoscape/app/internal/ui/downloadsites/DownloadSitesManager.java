package org.cytoscape.app.internal.ui.downloadsites;

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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.property.CyProperty;

/**
 * A manager for app store urls or download sites, responsible for loading/saving the 
 * list of download sites, and keeping track of the current list of download sites.
 */
public class DownloadSitesManager {
	
	public static final String DOWNLOAD_SITES_COUNT_KEY = "appStoreDownloadSiteCount";
	public static final String DOWNLOAD_SITE_NAME_KEY_PREFIX = "appStoreDownloadSiteName";
	public static final String DOWNLOAD_SITE_URL_KEY_PREFIX = "appStoreDownloadSiteUrl";
	
	private List<DownloadSite> downloadSites = new LinkedList<DownloadSite>();
	
	/** A reference to the {@link CyProperty} object 
	 */
	private CyProperty<Properties> cyProperty;
	
	/** The set of listeners listening for download sites changed events, such as for updating
	 * a GUI component containing the list of download sites.
	 */
	private Set<DownloadSitesChangedListener> downloadSitesChangedListeners;
	
	public DownloadSitesManager(CyProperty<Properties> cyProperty) {
		this.cyProperty = cyProperty;
		this.downloadSitesChangedListeners = new HashSet<DownloadSitesChangedListener>();
		
		loadDownloadSites();
	}
	
	/**
	 * Load the list of download sites
	 * @return <code>true</code> on success, <code>false</code> on failure.
	 */
	public boolean loadDownloadSites() {
		List<DownloadSite> newDownloadSites = new LinkedList<DownloadSite>();
		boolean loadFailed = false;
		
		String downloadSiteCountString = 
				cyProperty.getProperties().getProperty(DOWNLOAD_SITES_COUNT_KEY);
		
		int downloadSiteCount = 0;
		
		if (downloadSiteCountString != null) {
			try {
				downloadSiteCount = Integer.parseInt(downloadSiteCountString);
			} catch (NumberFormatException e) {
				loadFailed = true;
			}
		} else {
			loadFailed = true;
		}
		
		if (!loadFailed) {
			int siteNumber;
			String siteName, siteUrl;
			for (int i = 0; i < downloadSiteCount; i++) {
				siteNumber = i + 1;
				
				siteName = cyProperty.getProperties().getProperty(DOWNLOAD_SITE_NAME_KEY_PREFIX + siteNumber);
				siteUrl = cyProperty.getProperties().getProperty(DOWNLOAD_SITE_URL_KEY_PREFIX + siteNumber);
				
				if (siteName != null && siteUrl != null) {
					DownloadSite downloadSite = new DownloadSite();
					downloadSite.setSiteName(siteName);
					downloadSite.setSiteUrl(siteUrl);
					newDownloadSites.add(downloadSite);
				}
			}
			
			// Remove extra entries
			boolean isExtraKey;
			
			Set<Object> keysToRemove = new HashSet<Object>();
			
			for (Object key : cyProperty.getProperties().keySet()) {
				
				if (key instanceof String) {
					String keyString = (String) key;

					siteNumber = 0;
					isExtraKey = false;
					
					try {
						if (keyString.startsWith(DOWNLOAD_SITE_NAME_KEY_PREFIX)) {
							siteNumber = Integer.parseInt(keyString.substring(DOWNLOAD_SITE_NAME_KEY_PREFIX.length()).trim());
						}
						
						if (keyString.startsWith(DOWNLOAD_SITE_URL_KEY_PREFIX)) {
							siteNumber = Integer.parseInt(keyString.substring(DOWNLOAD_SITE_URL_KEY_PREFIX.length()).trim());
						}
					} catch (NumberFormatException e) {
						siteNumber = 0;
					}
					
					if (siteNumber >= downloadSiteCount + 1) {
						isExtraKey = true;
						keysToRemove.add(key);
					}
				}
			}
			
			for (Object keyToRemove : keysToRemove) {
				cyProperty.getProperties().remove(keyToRemove);
			}
			
			this.downloadSites = newDownloadSites;
			
			return true;
		} else {
			return false;
		}
	}
	
	public void saveDownloadSites() {
		// Save format:
		// Example, have 2 sites a and b. Use following properties.
		//
		// appStoreDownloadSiteCount=2
		// appStoreDownloadSiteUrl1=http://a
		// appStoreDownloadSiteName1=test
		// appStoreDownloadSiteUrl2=http://b
		// appStoreDownloadSiteName2=test_2
		
		int siteCount = downloadSites.size();
		int siteNumber;
		
		cyProperty.getProperties().setProperty(DOWNLOAD_SITES_COUNT_KEY, String.valueOf(siteCount));
		
		String siteName, siteUrl;
		
		for (int i = 0; i < siteCount; i++) {
			siteNumber = i + 1;
			
			siteName = downloadSites.get(i).getSiteName();
			siteUrl = downloadSites.get(i).getSiteUrl();
			
			cyProperty.getProperties().setProperty(DOWNLOAD_SITE_NAME_KEY_PREFIX + siteNumber, siteName);
			cyProperty.getProperties().setProperty(DOWNLOAD_SITE_URL_KEY_PREFIX + siteNumber, siteUrl);
		}
	}
	
	public List<DownloadSite> getDownloadSites() {
		return downloadSites;
	}
	
	public void addDownloadSitesChangedListener(DownloadSitesChangedListener listener) {
		this.downloadSitesChangedListeners.add(listener);
	}
	
	public void removeDownloadSitesChangedListener(DownloadSitesChangedListener listener) {
		this.downloadSitesChangedListeners.remove(listener);
	}
	
	public void addDownloadSite(DownloadSite downloadSite) {
		this.downloadSites.add(downloadSite);
		
		notifyDownloadSitesChanged();
	}
	
	public void removeDownloadSite(DownloadSite downloadSite) {
		this.downloadSites.remove(downloadSite);
		
		notifyDownloadSitesChanged();
	}
	
	/**
	 * Notify listeners that a site has been added, removed, or has its fields changed
	 */
	public void notifyDownloadSitesChanged() {
		
		DownloadSitesChangedEvent event = new DownloadSitesChangedEvent(this);
		
		for (DownloadSitesChangedListener listener : this.downloadSitesChangedListeners) {
			listener.downloadSitesChanged(event);
		}
	}
	
	public class DownloadSitesChangedEvent {
		
		private DownloadSitesManager source;
		
		public DownloadSitesChangedEvent(DownloadSitesManager source) {
			this.source = source;
		}
		
		public DownloadSitesManager getSource() {
			return source;
		}
	}
	
	public interface DownloadSitesChangedListener {
		
		public void downloadSitesChanged(DownloadSitesChangedEvent downloadSitesChangedEvent);
	}
}
