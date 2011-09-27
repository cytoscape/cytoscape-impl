package org.cytoscape.editor.internal;


import java.awt.geom.Point2D;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskMonitor;

import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import org.cytoscape.dnd.DropUtil;
import org.cytoscape.editor.internal.gui.ShapePalette;
import org.cytoscape.event.CyEventHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import javax.swing.JOptionPane;

public class DropNetworkViewTask extends AbstractNetworkViewTask {

	private final Transferable t;
	private final Point2D xformPt;
	private final CyEventHelper eh;
	private static int new_node_index =1;
	private VisualMappingManager vmm;

	private static final Logger logger = LoggerFactory.getLogger(DropNetworkViewTask.class);
	
	
	public DropNetworkViewTask(VisualMappingManager vmm, CyNetworkView view, Transferable t, Point2D xformPt, CyEventHelper eh) {
		super(view);
		this.t = t;
		this.xformPt = xformPt;
		this.eh = eh;
		this.vmm = vmm;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		if ( !DropUtil.transferableMatches(t,"Node") ) {
			logger.warn("Transferable object does not match expected type (Node) for task.");
			return;
		}

		CyNetwork net = view.getModel();
		CyNode n = net.addNode();
		
		// set the name attribute for the new node
		String nodeName = "";
		
		if (ShapePalette.specifyIdentifier){
			nodeName = JOptionPane.showInputDialog("Please specify a name");
		}
		else {
			nodeName = "Node_"+ new_node_index;		
			new_node_index++;
		}

		n.getCyRow().set("name", nodeName);
		
		eh.flushPayloadEvents();
		View<CyNode> nv = view.getNodeView(n);
		nv.setVisualProperty(MinimalVisualLexicon.NODE_X_LOCATION,xformPt.getX());
		nv.setVisualProperty(MinimalVisualLexicon.NODE_Y_LOCATION,xformPt.getY());
		view.updateView();
		
		// Apply visual style
		VisualStyle vs = vmm.getVisualStyle(view);
		vs.apply(view);

	}
}
