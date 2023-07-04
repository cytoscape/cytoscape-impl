package org.cytoscape.app.internal.manager;

import org.cytoscape.app.event.AppsFinishedStartingEvent;
import org.cytoscape.app.event.AppsFinishedStartingListener;
import org.cytoscape.app.internal.task.AppManagerTaskFactory;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSitesManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

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
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
    AppManagerTaskFactory factory = new AppManagerTaskFactory(appManager, registrar, swingApplication, downloadSitesManager, focus);
    TaskManager<?,?> taskManager = registrar.getService(TaskManager.class);
    TaskIterator ti = factory.createTaskIterator();
    taskManager.execute(ti);
	}
}
