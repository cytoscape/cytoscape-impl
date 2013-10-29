package org.cytoscape.app.internal.event;

import org.cytoscape.app.internal.manager.App;

public interface AppStatusChangedListener {
	void handleAppStatusChanged(String symbolicName, String version, App.AppStatus status);
}
