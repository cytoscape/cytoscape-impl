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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.net.WebQuerier.AppTag;
import org.cytoscape.app.internal.util.AppHelper;
import org.cytoscape.application.CyVersion;

/**
 * This class is intended to be a container for information obtained about an app from the app store website.
 */
public class WebApp {
	
	/** Name of the app used by the app store site as a unique app identifier. */
	private String name;
	
	/** The name of the app that is displayed to the user. */
	private String fullName;
	
	/** The name of the app to be used for listing available apps from the app store. May contain postfixes */
	private String appListDisplayName;
	
	/** A short description of the app. */
	private String description;
	
	/** Detailed information about the app, formatted in Markdown */
	private String details;
	
	/** The URL to the icon used to represent the app */
	private String iconUrl;

	/** The URL to the app's page on the app store website */
	private String pageUrl;
	
	/** The number of downloads recorded for this app */
	private int downloadCount;
	
	/** The average number of stars, ranging from 0 to 100, given to this app by users who had voted on the app*/
	private int starsPercentage;
	
	/** The number of people who had voted on the app*/
	private int votes;
	
	/** The set of tags associated with this app, which can be useful for dividing apps into categories by tag */
	private Set<AppTag> appTags;
	
	/** The version of Cytoscape that this app has been known to be compatible with */
	private String compatibleCytoscapeVersion;

	private String citation;
	
	private ImageIcon imageIcon;
	
	private List<Release> releases;
	
	private App correspondingApp;
	
	public class Release implements Comparable<Release> {
		private String baseUrl;
		private String relativeUrl;
		private String releaseDate;
		private String releaseVersion;
		private String compatibleCytoscapeVersions;
		
		@Override
		public int compareTo(Release other) {
//			return WebQuerier.compareVersions(other., second)
			return releaseDate.compareToIgnoreCase(other.releaseDate);
		}
		
		public WebApp getWebApp() {
			return WebApp.this;
		}
		
		public String getBaseUrl() {
			return baseUrl;
		}
		
		public String getRelativeUrl() {
			return relativeUrl;
		}
		
		public String getReleaseDate() {
			return releaseDate;
		}
		
		public String getReleaseVersion() {
			return releaseVersion;
		}
		
		public String getCompatibleCytoscapeVersions() {
			return compatibleCytoscapeVersions;
		}
		
		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}
		
		public void setRelativeUrl(String relativeUrl) {
			this.relativeUrl = relativeUrl;
		}
		
		public void setReleaseDate(String releaseDate) {
			this.releaseDate = releaseDate;
		}
		
		public void setReleaseVersion(String releaseVersion) {
			this.releaseVersion = releaseVersion;
		}
		
		public void setCompatibleCytoscapeVersions(String compatibleCytoscapeVersions) {
			this.compatibleCytoscapeVersions = compatibleCytoscapeVersions;
		}
		
		public boolean isCompatible(final CyVersion cyVer) {
			return AppHelper.isCompatible(cyVer, compatibleCytoscapeVersions);
		}
	}
	
	public WebApp() {
		appTags = new HashSet<AppTag>();
		releases = new LinkedList<Release>();
		
		correspondingApp = null;
	}
	
	/** 
	 * Obtain the app name that is used as a unique identifier on the app store website 
	 * @return The unique representative name used by the app store website
	 */
	public String getName() {
		return name;
	}

	/** 
	 * Obtain the name of the app that is displayed to the user 
	 * @return The app name displayed to the user
	 */
	public String getFullName() {
		return fullName;
	}
	
	/**
	 * Obtain the name of the app to be used in the available apps listing
	 * @return The name of the app to be used for the available apps listing
	 */
	public String getAppListDisplayName() {
		return appListDisplayName;
	}

	/** 
	 * Obtain a short description of the app 
	 * @return A short description of the app obtained from the app store website
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Get detailed information about the app, formatted in Markdown
	 * @return Detailed app information, formatted in Markdown
	 */
	public String getDetails() {
		return details;
	}
	
	/**
	 * Obtain the URL to the icon image for the app
	 * @return The URL of the app icon image
	 */
	public String getIconUrl() {
		return iconUrl;
	}
	
	/**
	 * Obtain the URL of the app's page on the app store website
	 * @return The URL to the app store page for this app
	 */
	public String getPageUrl() {
		return pageUrl;
	}
	
	/**
	 * Return the set of tags, represented by {@link AppTag} objects, associated with this app.
	 * @return The set of tags, represented by {@link AppTag} objects, associated with this app.
	 */
	public Set<AppTag> getAppTags() {
		return appTags;
	}
	
	public ImageIcon getImageIcon() {
		return imageIcon;
	}
	
	/**
	 * Obtain the download count for this app that was obtained from the app store website
	 * @return The download count for this app
	 */
	public int getDownloadCount() {
		return downloadCount;
	}
	
	/**
	 * Obtain the average number of stars, ranging from 0 to 100, that users had voted
	 * on the app
	 * @return The average number of stars
	 */
	public int getStarsPercentage() {
		return starsPercentage;
	}

	/**
	 * Obtain the number of people who voted on the app
	 * @return The number of people who voted on the app
	 */
	public int getVotes() {
		return votes;
	}
	
	/**
	 * Return the version of Cytoscape that this app has been known to be compatible with
	 * @return The latest version of Cytoscape that the app has been known to be compatible with
	 */
	public String getCompatibleCytoscapeVersion() {
		return compatibleCytoscapeVersion;
	}

	public String getCitation() {
		return citation;
	}
	
	public List<Release> getReleases() {
		return releases;
	}
	
	public App getCorrespondingApp() {
		return correspondingApp;
	}
	
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setAppListDisplayName(String appListDisplayName) {
		this.appListDisplayName = appListDisplayName;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}
	
	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}
	
	public void setDownloadCount(int downloadCount) {
		this.downloadCount = downloadCount;
	}
	
	public void setStarsPercentage(int starsPercentage) {
		this.starsPercentage = starsPercentage;
	}

	public void setVotes(int votes) {
		this.votes = votes;
	}
	
	public void setAppTags(Set<AppTag> appTags) {
		this.appTags = appTags;
	}
	
	public void setCompatibleCytoscapeVersion(String compatibleCytoscapeVersion) {
		this.compatibleCytoscapeVersion = compatibleCytoscapeVersion;
	}

	public void setCitation(String citation) {
		this.citation = citation;
	}
	
	public void setImageIcon(ImageIcon imageIcon) {
		this.imageIcon = imageIcon;
	}
	
	public void setReleases(List<Release> releases) {
		this.releases = releases;
	}
	
	public void setCorrespondingApp(App app) {
		this.correspondingApp = app;
	}
	
	@Override
	public String toString() {
		return appListDisplayName;
	}
}
