package org.cytoscape.editor.internal;


import java.util.ArrayList;
import java.util.List;

import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AddNestedNetworkTask extends AbstractNodeViewTask {

	private final VisualMappingManager vmMgr;
	
	private static final Logger logger = LoggerFactory.getLogger(AddNestedNetworkTask.class);

	@Tunable(description="Network")
	public ListSingleSelection<CyNetwork> nestedNetwork;

	@ProvidesTitle
	public String getTitle() {
		return "Choose Network for Node";
	}
	
	public AddNestedNetworkTask(final View<CyNode> nv,
								final CyNetworkView view,
								final CyNetworkManager mgr,
								final VisualMappingManager vmMgr,
								final CyGroupManager grMgr) {
		super(nv,view);
		this.vmMgr = vmMgr;
		
		final List<CyNetwork> networks = new ArrayList<CyNetwork>(mgr.getNetworkSet());
		nestedNetwork = new ListSingleSelection<CyNetwork>(networks);
		final CyNetwork netPointer = nodeView.getModel().getNetworkPointer();
		
		if (netPointer != null && networks.contains(netPointer))
			nestedNetwork.setSelectedValue(netPointer);
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		final CyNode node = nodeView.getModel();
		node.setNetworkPointer(nestedNetwork.getSelectedValue());
		
		final VisualStyle style = vmMgr.getVisualStyle(netView);
		style.apply(netView);
		netView.updateView();
	}
}
