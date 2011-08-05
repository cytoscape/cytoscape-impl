package org.cytoscape.biopax.internal;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.biopax.internal.action.ExportAsBioPAXTaskFactory;
import org.cytoscape.biopax.internal.action.LaunchExternalBrowser;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.session.CyApplicationManager;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskManager;

public class BioPaxFactory {

	private final CyNetworkViewManager networkViewManager;
	private final FileUtil fileUtil;
	private final CyApplicationManager applicationManager;
	private final CyFileFilter bioPaxFilter;
	private final TaskManager taskManager;
	private final LaunchExternalBrowser launchExternalBrowser;
	private final CySwingApplication cySwingApplication;

	public BioPaxFactory(CyNetworkViewManager networkViewManager, FileUtil fileUtil, CyApplicationManager applicationManager, CyFileFilter bioPaxFilter, TaskManager taskManager, LaunchExternalBrowser launchExternalBrowser, CySwingApplication cySwingApplication) {
		this.networkViewManager = networkViewManager;
		this.fileUtil = fileUtil;
		this.applicationManager = applicationManager;
		this.bioPaxFilter = bioPaxFilter;
		this.taskManager = taskManager;
		this.launchExternalBrowser = launchExternalBrowser;
		this.cySwingApplication = cySwingApplication;
	}
	
	public CyNetworkViewManager getCyNetworkViewManager() {
		return networkViewManager;
	}

	public FileUtil getFileUtil() {
		return fileUtil;
	}

	public CyApplicationManager getCyApplicationManager() {
		return applicationManager;
	}

	public ExportAsBioPAXTaskFactory createExportAsBioPAXTaskFactory(String fileName) {
		return new ExportAsBioPAXTaskFactory(fileName, bioPaxFilter);
	}

	public TaskManager getTaskManager() {
		return taskManager;
	}

	public LaunchExternalBrowser getLaunchExternalBrowser() {
		return launchExternalBrowser;
	}

	public CySwingApplication getCySwingApplication() {
		return cySwingApplication;
	}

}
