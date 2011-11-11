package org.cytoscape.task.internal.quickstart.subnetworkbuilder;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class SubnetworkBuilderTaskFactory implements TaskFactory {

	private final CyNetworkManager networkManager;
	private final SubnetworkBuilderUtil util;

	public SubnetworkBuilderTaskFactory(final CyNetworkManager networkManager,
			final SubnetworkBuilderUtil util) {
		this.networkManager = networkManager;
		this.util = util;
	}

	public TaskIterator getTaskIterator() {
		return new TaskIterator(2,new SubnetworkBuilderTask(networkManager, util));
	}
}
