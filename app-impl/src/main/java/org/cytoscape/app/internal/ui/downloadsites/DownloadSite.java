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
	public int hashCode() {
		final int prime = 7;
		int result = 11;
		result = prime * result + ((siteName == null) ? 0 : siteName.hashCode());
		result = prime * result + ((siteUrl == null) ? 0 : siteUrl.hashCode());
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		DownloadSite other = (DownloadSite) obj;
		
		if (siteName == null) {
			if (other.siteName != null)
				return false;
		} else if (!siteName.equals(other.siteName)) {
			return false;
		}
		if (siteUrl == null) {
			if (other.siteUrl != null)
				return false;
		} else if (!siteUrl.equals(other.siteUrl)) {
			return false;
		}
		
		return true;
	}

	@Override
	public String toString() {
		return siteName;
	}
}
