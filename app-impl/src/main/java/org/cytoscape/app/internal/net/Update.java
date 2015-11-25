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

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;

/**
 * This class represents a downloadable update for an app.
 */
public class Update {
	
	/** The App object representing the app to be updated by this update */
	private App app;
	
	/** Information about the update, such as changes that the update introduces. */
	private String updateInformation;
	
	/** The version of the new updated app */
	private String updateVersion;

	private WebApp webApp;
	
	private WebApp.Release release;
	
	/** Obtain the {@link App} object that the update applies to. */
	public App getApp() {
		return app;
	}
	
	/** Obtain information about the update. */
	public String getUpdateInformation() {
		return updateInformation;
	}
	
	/** Obtain the new version of the app once the update is applied. */
	public String getUpdateVersion() {
		return updateVersion;
	}
	
	/**
	 * @return The associate {@link WebApp} object containing information from the app 
	 * store about this app
	 */
	public WebApp getWebApp() {
		return webApp;
	}
	
	/**
	 * @return The latest release of the app, to be downloaded with this update
	 */
	public WebApp.Release getRelease() {
		return release;
	}
	
	public boolean isInstalled(AppManager appManager) {
		for(App other: appManager.getApps()) {
			if (app.getAppName().equalsIgnoreCase(other.getAppName()) &&
				WebQuerier.compareVersions(release.getReleaseVersion(), other.getVersion()) == 0 &&
				app.getSha512Checksum().equalsIgnoreCase(release.getSha512Checksum()))
				return true;
		}
		return false;
	}
	
	public void setApp(App app) {
		this.app = app;
	}

	public void setUpdateInformation(String updateInformation) {
		this.updateInformation = updateInformation;
	}

	public void setUpdateVersion(String updateVersion) {
		this.updateVersion = updateVersion;
	}

	public void setWebApp(WebApp webApp) {
		this.webApp = webApp;
	}
	
	public void setRelease(WebApp.Release release) {
		this.release = release;
	}
	
	@Override
	public String toString() {
		return this.app.getAppName();
	}
}
