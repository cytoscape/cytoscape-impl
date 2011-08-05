package org.cytoscape.task.internal.quickstart.subnetworkbuilder;

import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class ApplyLayoutTask extends AbstractTask {

    private final SubnetworkBuilderUtil util;
    private final CyLayoutAlgorithmManager layouts;

    ApplyLayoutTask(final SubnetworkBuilderUtil util, final CyLayoutAlgorithmManager layouts) {
	this.layouts = layouts;
	this.util = util;
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
//	final CyLayoutAlgorithm layout = layouts.getLayout(CyLayoutAlgorithmManager.DEFAULT_LAYOUT_NAME);
	final CyLayoutAlgorithm layout = layouts.getDefaultLayout();
	layout.setNetworkView(util.appManager.getCurrentNetworkView());
	insertTasksAfterCurrentTask(layout.getTaskIterator());
    }

}
