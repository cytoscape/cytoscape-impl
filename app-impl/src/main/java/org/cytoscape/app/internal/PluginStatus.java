package org.cytoscape.app.internal;

public enum PluginStatus	{
	CURRENT("CurrentPlugins"),
	DELETE("DeletePlugins"),
	INSTALL("InstallPlugins");
	
	private String statusText;
	
	private PluginStatus(String status) {
		statusText = status;
	}
	
	public String getTagName() {
		return statusText;
	}
}


