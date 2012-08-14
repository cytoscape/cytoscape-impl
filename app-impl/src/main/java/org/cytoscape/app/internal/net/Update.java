package org.cytoscape.app.internal.net;

import org.cytoscape.app.internal.manager.App;

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
