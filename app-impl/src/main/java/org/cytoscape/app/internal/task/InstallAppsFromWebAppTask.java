package org.cytoscape.app.internal.task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.DownloadStatus;
import org.cytoscape.app.internal.net.WebApp;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class InstallAppsFromWebAppTask extends AbstractTask {
	
	private final List<WebApp> webApps;
	private final AppManager appManager;
	private final boolean promptToReplace;
	
	public InstallAppsFromWebAppTask(final List<WebApp> webApps, final AppManager appManager, final boolean promptToReplace) {
		this.webApps = webApps;
		this.appManager = appManager;
		this.promptToReplace = promptToReplace;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Install from App Store");
		taskMonitor.setTitle("");
		
		DownloadStatus status = new DownloadStatus(taskMonitor);
		List<File> appFiles = new ArrayList<File>();
		for(WebApp webApp: webApps) {
			taskMonitor.setStatusMessage("Downloading " + webApp.getFullName());
			// Download app
			File appFile = appManager.getWebQuerier().downloadApp(webApp, null, new File(appManager.getDownloadedAppsPath()), status);
			if( appFile == null) {
				throw new Exception("Unable to find download URL for about-to-be-installed " + webApp.getFullName());
			}
			appFiles.add(appFile);
		}
		insertTasksAfterCurrentTask(new InstallAppsFromFileTask(appFiles, appManager, promptToReplace));
	}

}
