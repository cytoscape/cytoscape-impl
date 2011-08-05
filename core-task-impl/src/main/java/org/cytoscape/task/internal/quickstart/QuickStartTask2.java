package org.cytoscape.task.internal.quickstart;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.internal.quickstart.subnetworkbuilder.SubnetworkBuilderUtil;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class QuickStartTask2 extends QuickStartTask {

	private static final String quickStartPropertyKey = "showQuickStartAtStartup";
	
	@Tunable(description="Show this dialog at Cytoscape start-up")
	public boolean showQuickStartAtStartup; // get this value from system property

	public QuickStartTask2(final QuickStartState state, final ImportTaskUtil importTaskUtil, 
			final CyNetworkManager networkManager, final SubnetworkBuilderUtil subnetworkUtil){
		super(state, importTaskUtil, networkManager, subnetworkUtil);
		
		showQuickStartAtStartup = new Boolean(importTaskUtil.getCyProperty().getProperties().
				getProperty(quickStartPropertyKey)).booleanValue();	
	}
	

	public void run(TaskMonitor e) {
		// Save the property value of showQuickStartAtStartup
		importTaskUtil.getCyProperty().getProperties().setProperty(quickStartPropertyKey, new Boolean(showQuickStartAtStartup).toString());

		doLoading();
	}
}
