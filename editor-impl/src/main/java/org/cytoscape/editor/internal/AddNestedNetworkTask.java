package org.cytoscape.editor.internal;


import java.awt.geom.Point2D;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.util.List;
import java.util.ArrayList;

import org.cytoscape.task.AbstractNodeViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.cytoscape.dnd.DropUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AddNestedNetworkTask extends AbstractNodeViewTask {

	private static final Logger logger = LoggerFactory.getLogger(AddNestedNetworkTask.class);

	@Tunable(description="Select a Network")
	public ListSingleSelection<CyNetwork> nestedNetwork;

	private final Transferable t;

	public AddNestedNetworkTask(View<CyNode> nv, CyNetworkView view, CyNetworkManager mgr,
	                            Transferable t) {
		super(nv,view);
		this.t = t;
		nestedNetwork = new ListSingleSelection<CyNetwork>(new ArrayList<CyNetwork>(mgr.getNetworkSet()));
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		if ( !DropUtil.transferableMatches(t,"Network") ) {
			logger.warn("Transferable object does not match expected type (Network) for task.");
			return;
		}

		CyNode n = nodeView.getModel();
		n.setNetworkPointer( nestedNetwork.getSelectedValue() );
		netView.updateView();
	}
}
