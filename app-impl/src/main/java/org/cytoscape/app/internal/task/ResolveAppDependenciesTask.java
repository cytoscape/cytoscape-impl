package org.cytoscape.app.internal.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.DownloadStatus;
import org.cytoscape.app.internal.net.WebApp;
import org.cytoscape.app.internal.net.WebApp.Release;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.application.CyVersion;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;




public class ResolveAppDependenciesTask extends AbstractTask {
	
	private TaskMonitor taskMonitor;
	private DownloadStatus status;
	private Queue<App> appQueue;
	private Stack<String> dependencyStack;
	private List<App> appsToInstall;
	private Map<App, App> appsToReplace;
	
	final private AppManager appManager;
	final private boolean promptToReplace;
	
	public ResolveAppDependenciesTask(final Collection<App> apps, final AppManager appManager, final boolean promptToReplace) {
		appQueue = new LinkedList<>(apps);
		dependencyStack = new Stack<>();
		appsToInstall = new ArrayList<>();
		appsToReplace = new HashMap<>();
		this.appManager = appManager;
		this.promptToReplace = promptToReplace;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		status = new DownloadStatus(taskMonitor);
		while(!appQueue.isEmpty()) {
			App app = appQueue.poll();
			resolveAppDependencies(app);
		}
		if(promptToReplace && !appsToReplace.isEmpty())
			insertTasksAfterCurrentTask(new ResolveAppConflictTask(appsToInstall, appsToReplace, appManager));
		else
			insertTasksAfterCurrentTask(new InstallAppsTask(appsToInstall, appsToReplace, appManager));
	}
	
	private void resolveAppDependencies(App appToInstall) throws Exception {
		CyVersion version = appManager.getCyVersion();
		if(!appToInstall.isCompatible(version))
			throw new Exception("Unable to install " + appToInstall.getAppName() + 
					".\nIt is incompatible with this version of Cytoscape (" + version.getVersion() +
					").");
		
		taskMonitor.setStatusMessage("Resolving dependencies for " + appToInstall.getAppName() + "...");

		for(App installedApp: appManager.getInstalledApps()) {
			if(installedApp.getAppName().equals(appToInstall.getAppName())) {
				appsToReplace.put(appToInstall, installedApp);
				break;
			}
		}
		dependencyStack.push(appToInstall.getAppName());
		if(appToInstall.getDependencies() != null)
		for(App.Dependency dep : appToInstall.getDependencies()){
			if(dependencyStack.contains(dep.getName()))
				throw new Exception("Invalid circular dependency: " + dep.getName());
			else if(findAppForDep(dep, appsToInstall) != null)
				continue;
			else if(findAppForDep(dep, appManager.getInstalledApps()) != null)
				continue;
			else {
				App dependencyApp = findAppForDep(dep, appQueue);
				if(dependencyApp != null) {
					appQueue.remove(dependencyApp);
				}
				else {
					Set<WebApp> webApps = appManager.getWebQuerier().getAllApps();
					if(webApps == null)
						throw new Exception("Cannot access the App Store to resolve dependencies. Please check your internet connection.");
					WebApp webApp = findWebAppForDep(dep, webApps);
					if(webApp == null)
						throw new Exception("Cannot find dependency: " + dependencyStack.firstElement()
								+ " requires " + dep.getName() + ", which is not available in the App Store");
					
					List<Release> releases = webApp.getReleases();
					Release latestRelease = releases.get(releases.size() - 1);
					if(WebQuerier.compareVersions(dep.getVersion(), latestRelease.getReleaseVersion()) >= 0) {
						taskMonitor.setStatusMessage("Downloading dependency for " + dependencyStack.firstElement() +": "+ webApp.getFullName());
						File appFile = appManager.getWebQuerier().downloadApp(webApp, null, new File(appManager.getDownloadedAppsPath()), status);
						dependencyApp = appManager.getAppParser().parseApp(appFile);
					}
					else
						throw new Exception("Cannot find dependency: " + dependencyStack.firstElement() + " requires "  +dep.getName() +" "
								+ dep.getVersion() + " or later, latest release in App Store is " + latestRelease.getReleaseVersion());
				}
				resolveAppDependencies(dependencyApp);
			}
		}
		dependencyStack.pop();
		appsToInstall.add(appToInstall);
	}
	
	private App findAppForDep(App.Dependency dep, Collection<App> apps) {
		for(App app: apps) {
			if(app.getAppName().equalsIgnoreCase(dep.getName()) &&
					WebQuerier.compareVersions(dep.getVersion(), app.getVersion()) >= 0)
				return app;
		}
		return null;
	}
	
	private WebApp findWebAppForDep(App.Dependency dep, Collection<WebApp> webApps) {
		for(WebApp webApp: webApps) {
			if(webApp.getName().equalsIgnoreCase(dep.getName())){
				return webApp;
			}
		}
		return null;
	}

}
