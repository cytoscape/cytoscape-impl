package org.cytoscape.app.internal;

public enum AppStatus	{
	CURRENT("CurrentApps"),
	DELETE("DeleteApps"),
	INSTALL("InstallApps");
	
	private String statusText;
	
	private AppStatus(String status) {
		statusText = status;
	}
	
	public String getTagName() {
		return statusText;
	}
}


