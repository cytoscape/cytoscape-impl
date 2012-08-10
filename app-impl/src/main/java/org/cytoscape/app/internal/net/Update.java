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

	private String updateUrl;
	
	/** Obtain the {@link App}pp object that the update is associated with. */
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

	public String getUpdateUrl() {
		return updateUrl;
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
	
	public void setUpdateUrl(String updateUrl) {
		this.updateUrl = updateUrl;
	}
	
	@Override
	public String toString() {
		return this.app.getAppName();
	}
}
