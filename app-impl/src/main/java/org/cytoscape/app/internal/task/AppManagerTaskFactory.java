package org.cytoscape.app.internal.task;

import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSitesManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class AppManagerTaskFactory extends AbstractTaskFactory {
	final AppManager appManager;
	final CyServiceRegistrar serviceRegistrar;
	final CySwingApplication swingApplication;
	final DownloadSitesManager downloadSitesManager;
	final Boolean focus;

	public AppManagerTaskFactory(final AppManager appManager, final CyServiceRegistrar serviceRegistrar, CySwingApplication swingApplication, final DownloadSitesManager downloadSitesManager, Boolean focus) {
		this.appManager = appManager;
		this.serviceRegistrar = serviceRegistrar;
		this.swingApplication = swingApplication;
		this.downloadSitesManager = downloadSitesManager;
		this.focus = focus;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new AppManagerTask(appManager, serviceRegistrar, swingApplication, downloadSitesManager, focus));
	}

	@Override
	public boolean isReady() { return true; }

}
