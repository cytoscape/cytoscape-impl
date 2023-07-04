package org.cytoscape.app.internal.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.App.AppStatus;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.WebApp;
import org.cytoscape.app.internal.net.WebApp.Release;
import org.cytoscape.work.AbstractTask;

public abstract class AbstractAppTask extends AbstractTask {
	final protected AppManager appManager;
	protected Set<App> appList;
	protected Set<WebApp> webAppList;

	AbstractAppTask(AppManager appManager) {
		this.appManager = appManager;
		// updateApps();
	}

	protected App getApp(String appName) {
		List<App> matchingApps = new ArrayList<App>();
		for (App app: appList) {
			if (appName.equalsIgnoreCase(app.getAppName())) {
				if (app.getStatus() == AppStatus.INSTALLED)
					return app;
				matchingApps.add(app);
			}
		}
		if (matchingApps.size() == 0)
			return null;

		// OK, we have multiple matching apps.  Find the latest version
		// and return that one
		Collections.sort(matchingApps, new VersionCompare());
		return matchingApps.get(matchingApps.size()-1);
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
	}

	protected void updateWebApps() {
		webAppList = appManager.getWebQuerier().getAllApps();
	}

	protected String getVersion(WebApp webApp) {
		List<Release> releases = webApp.getReleases();
		Collections.sort(releases);
		Release release = releases.get(releases.size()-1);
		return release.getReleaseVersion();
	}

	class VersionCompare implements Comparator<App> {
		@Override
		public int compare(App o1, App o2) {
			String version1 = o1.getVersion();
			String version2 = o2.getVersion();
			String[] v1 = version1.split("\\.");
			String[] v2 = version2.split("\\.");
			int major1 = Integer.parseInt(v1[0]);
			int major2 = Integer.parseInt(v2[0]);
			if (major1 != major2)
				return Integer.compare(major1, major2);
			if (v1.length == 1 || v2.length == 1) {
				return Integer.compare(v1.length, v2.length);
			}

			int minor1 = Integer.parseInt(v1[1]);
			int minor2 = Integer.parseInt(v2[1]);
			if (minor1 != minor2)
				return Integer.compare(minor1, minor2);
			if (v1.length == 2 || v2.length == 2) {
				return Integer.compare(v1.length, v2.length);
			}

			int patch1 = Integer.parseInt(v1[2]);
			int patch2 = Integer.parseInt(v2[2]);
			return Integer.compare(patch1, patch2);
		}
	}

}
