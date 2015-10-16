package org.cytoscape.ding.impl;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


import java.awt.Point;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.task.AbstractNodeViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AddEdgeTask extends AbstractNodeViewTask {

	private final VisualMappingManager vmm;
	private final CyEventHelper eh;
	
	private static final Logger logger = LoggerFactory.getLogger(AddEdgeTask.class);

	public AddEdgeTask(final View<CyNode> nv,
					   final CyNetworkView view,
					   final VisualMappingManager vmm,
					   final CyEventHelper eh) {
		super(nv, view);
		this.vmm = vmm;
		this.eh = eh;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		CyNode sourceNode = AddEdgeStateMonitor.getSourceNode(netView);
		
		if (sourceNode == null) {
			AddEdgeStateMonitor.setSourceNode(netView, nodeView.getModel());
			double[] coords = new double[2];
			coords[0] = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
			coords[1] = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
			((DGraphView) netView).xformNodeToComponentCoords(coords);
			
			Point sourceP = new Point();
			sourceP.setLocation(coords[0], coords[1]);
			AddEdgeStateMonitor.setSourcePoint(netView, sourceP);
		} else {
			// set the name attribute for the new node
			CyNetwork net = netView.getModel();
			CyNode targetNode = nodeView.getModel();

			final CyEdge newEdge = net.addEdge(sourceNode, targetNode, true);
			final String interaction = "interacts with";
			String edgeName = net.getRow(sourceNode).get(CyRootNetwork.SHARED_NAME, String.class);
			edgeName += " (" + interaction + ") ";
			edgeName += net.getRow(targetNode).get(CyRootNetwork.SHARED_NAME, String.class);

			CyRow edgeRow = net.getRow(newEdge, CyNetwork.DEFAULT_ATTRS);
			edgeRow.set(CyNetwork.NAME, edgeName);
			edgeRow.set(CyEdge.INTERACTION, interaction);

			AddEdgeStateMonitor.setSourceNode(netView, null);
			
			// Apply visual style
			eh.flushPayloadEvents(); // To make sure the edge view is created before applying the style
			VisualStyle vs = vmm.getVisualStyle(netView);
			View<CyEdge> edgeView = netView.getEdgeView(newEdge);
			if (edgeView != null)
				vs.apply(edgeRow, edgeView);
			netView.updateView();
		}
	}
}
