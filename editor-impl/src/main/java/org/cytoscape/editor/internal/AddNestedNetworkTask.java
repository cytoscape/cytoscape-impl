package org.cytoscape.editor.internal;



import java.util.ArrayList;

import org.cytoscape.task.AbstractNodeViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AddNestedNetworkTask extends AbstractNodeViewTask {

	private static final Logger logger = LoggerFactory.getLogger(AddNestedNetworkTask.class);

	@Tunable(description="Network")
	public ListSingleSelection<CyNetwork> nestedNetwork;

	@ProvidesTitle
	public String getTitle() {
		return "Choose Network for Node";
	}
	
	public AddNestedNetworkTask(View<CyNode> nv, CyNetworkView view, CyNetworkManager mgr) {
		super(nv,view);
		nestedNetwork = new ListSingleSelection<CyNetwork>(new ArrayList<CyNetwork>(mgr.getNetworkSet()));
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
	
		CyNode n = nodeView.getModel();
		n.setNetworkPointer( nestedNetwork.getSelectedValue() );
		netView.updateView();
	}
}
