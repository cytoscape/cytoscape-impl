package org.cytoscape.task.internal.welcome;

import java.io.File;
import java.util.Properties;

import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.internal.loadnetwork.AbstractLoadNetworkTask;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskMonitor;

public class LoadMitabFileTask extends AbstractLoadNetworkTask {

	private final File file;
	private final boolean applyLayout;

	public LoadMitabFileTask(boolean applyLayout, final File file, CyNetworkReaderManager mgr, CyNetworkManager netmgr,
			final CyNetworkViewManager networkViewManager, final Properties props, CyNetworkNaming namingUtil) {
		super(mgr, netmgr, networkViewManager, props, namingUtil);

		if (file == null)
			throw new NullPointerException("File is null.");

		this.file = file;
		this.applyLayout = applyLayout;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(-1);
		this.taskMonitor = taskMonitor;
		reader = mgr.getReader(file.toURI(), file.getName());

		if (cancelled)
			return;

		if (reader == null)
			throw new NullPointerException("Failed to find appropriate reader for file: " + file);

		if(applyLayout)
			props.setProperty("preferredLayoutAlgorithm", "force-directed");
		else
			props.remove("preferredLayoutAlgorithm");
		
		uri = file.toURI();
		name = file.getName();
		this.viewThreshold = Integer.MAX_VALUE;
		loadNetwork(reader);
		taskMonitor.setProgress(1.0);
	}
}
