package org.cytoscape.app.internal.manager;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.app.event.AppsFinishedStartingEvent;
import org.cytoscape.app.event.AppsFinishedStartingListener;
import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSite;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSitesManager;
import org.cytoscape.app.internal.task.AppManagerTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.application.swing.CySwingApplication;

public class LaunchManager implements AppsFinishedStartingListener {

	private AppManager appManager;
	private DownloadSitesManager downloadSitesManager;
  private CyServiceRegistrar registrar;
  private CySwingApplication swingApplication;
	private Boolean focus;

	public LaunchManager(AppManager appManager, DownloadSitesManager downloadSitesManager, CyServiceRegistrar registrar, CySwingApplication swingApplication, Boolean focus) {
		this.appManager = appManager;
		this.downloadSitesManager = downloadSitesManager;
    this.registrar = registrar;
    this.swingApplication = swingApplication;
		this.focus = focus;
	}

	@Override
	public void handleEvent(AppsFinishedStartingEvent evt){
    AppManagerTaskFactory factory = new AppManagerTaskFactory(appManager, registrar, swingApplication, downloadSitesManager, focus);
    TaskManager<?,?> taskManager = registrar.getService(TaskManager.class);
    TaskIterator ti = factory.createTaskIterator();
    taskManager.execute(ti);
	}
}
