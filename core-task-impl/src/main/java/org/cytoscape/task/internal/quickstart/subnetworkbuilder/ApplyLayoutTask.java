package org.cytoscape.task.internal.quickstart.subnetworkbuilder;

import java.util.Collections;
import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.layout.CyLayoutContext;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
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
		taskMonitor.setProgress(0.0);
	final CyLayoutAlgorithm layout = layouts.getDefaultLayout();
	taskMonitor.setProgress(0.1);
	CyLayoutContext context = layout.createLayoutContext();
	taskMonitor.setProgress(0.2);
	CyNetworkView view = util.appManager.getCurrentNetworkView();
	Set<View<CyNode>> nodes = Collections.emptySet();
	insertTasksAfterCurrentTask(layout.createTaskIterator(view, context, nodes));
	
	taskMonitor.setProgress(1.0);
    }

}
