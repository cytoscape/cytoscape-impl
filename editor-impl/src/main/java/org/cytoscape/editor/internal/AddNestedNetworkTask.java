package org.cytoscape.editor.internal;


import java.util.ArrayList;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AddNestedNetworkTask extends AbstractNodeViewTask {

	private final SelectedVisualStyleManager svsmMgr;
	
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
								final SelectedVisualStyleManager svsmMgr) {
		super(nv,view);
		this.svsmMgr = svsmMgr;
		
		nestedNetwork = new ListSingleSelection<CyNetwork>(new ArrayList<CyNetwork>(mgr.getNetworkSet()));
		final CyNetwork netPointer = nodeView.getModel().getNetworkPointer();
		
		if (netPointer != null)
			nestedNetwork.setSelectedValue(netPointer);
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		final CyNode n = nodeView.getModel();
		n.setNetworkPointer(nestedNetwork.getSelectedValue());
		
		nodeView.setLockedValue(BasicVisualLexicon.NODE_NESTED_NETWORK_IMAGE_VISIBLE, Boolean.TRUE);
		svsmMgr.getCurrentVisualStyle().apply(netView);
		netView.updateView();
	}
}
