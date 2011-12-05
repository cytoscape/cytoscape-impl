package org.cytoscape.task.internal.setcurrent;


import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;


/**
 * This class exists for (possible) use in headless mode.  The associated 
 * TaskFactory should not be registered in Swing mode, since this task doesn't 
 * make sense in GUI mode.
 */
public class SetCurrentNetworkTask extends AbstractTask {
	private final CyApplicationManager applicationManager;
	private final CyNetworkManager networkManager;
	private TaskMonitor taskMonitor;
	
	public SetCurrentNetworkTask(final CyApplicationManager applicationManager,
				     final CyNetworkManager networkManager)
	{
		this.applicationManager = applicationManager;
		this.networkManager = networkManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Verify that we want an essentially random network and that this
		// task shouldn't be NetworkTask instead.
		this.taskMonitor = taskMonitor;
		taskMonitor.setProgress(0.0);
		Object[] setNetworks = networkManager.getNetworkSet().toArray();
		taskMonitor.setProgress(0.3);
		applicationManager.setCurrentNetwork(((CyNetwork) setNetworks[setNetworks.length-1]));
		taskMonitor.setProgress(1.0);
	}
}
