package org.cytoscape.task.internal.quickstart.subnetworkbuilder;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a new Visual Style for the subnetwork.
 * 
 */
public class BuildVisualStyleTask extends AbstractTask {

    private static final Logger logger = LoggerFactory.getLogger(BuildVisualStyleTask.class);

    private final SubnetworkBuilderUtil util;

    BuildVisualStyleTask(final SubnetworkBuilderUtil util) {
	this.util = util;
    }

    @Override
    public void run(TaskMonitor tm) throws Exception {

	// Assume current network is the target
	final CyNetwork targetNetwork = util.appManager.getCurrentNetwork();
	logger.debug("Network: " + targetNetwork);
	logger.debug("Builder: " + util.vsBuilder);
	
	final String networkName = targetNetwork.getCyRow().get(CyTableEntry.NAME, String.class);
	final VisualStyle style = util.vsBuilder.buildStyle(networkName + " Style");

	util.vmm.addVisualStyle(style);

	logger.debug("New Visual Style created: " + style.getTitle());
    }

}
