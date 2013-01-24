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

/**
 * A class representing an app store, or a download site that can be used to obtain
 * apps and app information.
 */
public class DownloadSite {
	
	/** The name of the site, does not have to be an official name, mainly used for convenient displaying
	 */
	private String siteName;
	
	/** The site's url.
	 */
	private String siteUrl;
	
	public DownloadSite() {
		this.siteName = "";
		this.siteUrl = "";
	}
	
	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getSiteUrl() {
		return siteUrl;
	}

	public void setSiteUrl(String siteUrl) {
		this.siteUrl = siteUrl;
	}

	@Override
	public String toString() {
		return this.siteName;
	}
	
	/**
	 * Returns true if the given site has same name and Url.
	 * @param downloadSite Other site
	 * @return <code>true</code> if other site has same name and Url, <code>false</code> otherwise.
	 */
	public boolean sameSiteAs(DownloadSite downloadSite) {
		if (downloadSite.siteName.equals(this.siteName)
				&& downloadSite.siteUrl.equals(this.siteUrl)) {
			return true;
		} else {
			return false;
		}
	}
}
