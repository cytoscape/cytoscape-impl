package org.cytoscape.app.internal.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.App.AppStatus;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.WebApp;
import org.cytoscape.app.internal.net.WebApp.Release;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;

public abstract class AbstractAppTask extends AbstractTask {
	final protected AppManager appManager;
	protected Set<App> appList;
	protected Set<WebApp> webAppList;

	AbstractAppTask(AppManager appManager) {
		this.appManager = appManager;
		updateApps();
	}

	protected App getApp(String appName) {
		for (App app: appList) {
			if (appName.equalsIgnoreCase(app.getAppName()))
				return app;
		}
		return null;
	}

	protected List<App> getApps(AppStatus status) {
		List<App> apps = new ArrayList<>();
		for (App app: appList) {
			if (app.getStatus().equals(status))
				apps.add(app);
		}
		return apps;
	}

	protected WebApp getWebApp(String appName) {
		for (WebApp app: webAppList) {
			if (app.getName().equalsIgnoreCase(appName))
				return app;
		}
		return null;
	}

	protected void updateApps() {
		appList = appManager.getApps();
		webAppList = appManager.getWebQuerier().getAllApps();
	}

	protected String getVersion(WebApp webApp) {
		List<Release> releases = webApp.getReleases();
		Collections.sort(releases);
		Release release = releases.get(releases.size()-1);
		return release.getReleaseVersion();
	}

}
