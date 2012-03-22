package org.cytoscape.editor.internal;

import java.awt.datatransfer.Transferable;
import java.awt.geom.Point2D;

import javax.swing.JOptionPane;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddNodeTask extends AbstractNetworkViewTask{

	private final VisualMappingManager vmm;
	private final CyRootNetworkManager rnm;
	//private final Transferable t;
	private final Point2D xformPt;
	private final CyEventHelper eh;
	private static int new_node_index =1;

	private static final Logger logger = LoggerFactory.getLogger(AddNodeTask.class);
	
	
	public AddNodeTask(final VisualMappingManager vmm, final CyRootNetworkManager rnm, final CyNetworkView view,
			//final Transferable t,
			final Point2D xformPt, final CyEventHelper eh) {
		super(view);
		this.vmm = vmm;
		this.rnm = rnm;
		//this.t = t;
		this.xformPt = xformPt;
		this.eh = eh;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		/*
		if ( !DropUtil.transferableMatches(t,"Node") ) {
			logger.warn("Transferable object does not match expected type (Node) for task.");
			return;
		}

	*/
		CyNetwork net = view.getModel();
		CyNode n = net.addNode();
		
		// set the name attribute for the new node
		String nodeName = "";
		nodeName = "Node_"+ new_node_index;		
		new_node_index++;

		net.getRow(n).set(CyNode.NAME, nodeName);
		rnm.getRootNetwork(net).getRow(n).set(CyNode.NAME, nodeName);
		
		eh.flushPayloadEvents();
		View<CyNode> nv = view.getNodeView(n);
		nv.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION,xformPt.getX());
		nv.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION,xformPt.getY());
		view.updateView();
		
		// Apply visual style
		VisualStyle vs = vmm.getVisualStyle(view);
		vs.apply(view);
	}
}
