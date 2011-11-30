package org.cytoscape.task.internal.setcurrent;


import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;


// TODO Verify that we need this class in headless mode!
/**
 * This TaskFactory is for headless mode and not GUI mode. This
 * factory shouldn't be registered by the swing GUI as it doesn't
 * make sense in that context.
 */
public class SetCurrentNetworkTaskFactoryImpl implements TaskFactory {
	private final CyApplicationManager applicationManager;
	private final CyNetworkManager netmgr;

	public SetCurrentNetworkTaskFactoryImpl(final CyApplicationManager applicationManager,
						final CyNetworkManager netmgr)
	{
		this.applicationManager = applicationManager;
		this.netmgr = netmgr;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new SetCurrentNetworkTask(applicationManager, netmgr));
	}
}
