package org.cytoscape.task.internal.quickstart;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.internal.quickstart.subnetworkbuilder.SubnetworkBuilderUtil;
import org.cytoscape.work.TaskIterator;

public class QuickStartTaskFactory2 extends QuickStartTaskFactory {

	public QuickStartTaskFactory2(final ImportTaskUtil util, 
			final CyNetworkManager networkManager, final SubnetworkBuilderUtil subnetworkUtil){
		super(util,networkManager, subnetworkUtil);
	}
	
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new StartTask(new QuickStartState(), util, networkManager, subnetworkUtil));
	} 
}
