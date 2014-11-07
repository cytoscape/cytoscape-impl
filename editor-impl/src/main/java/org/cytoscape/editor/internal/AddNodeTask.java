package org.cytoscape.editor.internal;

/*
 * #%L
 * Cytoscape Editor Impl (editor-impl)
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

import java.awt.geom.Point2D;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;

public class AddNodeTask extends AbstractNetworkViewTask{

	private final VisualMappingManager vmm;
	private final Point2D xformPt;
	private final CyEventHelper eh;
	private static int new_node_index =1;
	
	public AddNodeTask(final VisualMappingManager vmm,
					   final CyNetworkView view,
					   final Point2D xformPt,
					   final CyEventHelper eh) {
		super(view);
		this.vmm = vmm;
		this.xformPt = xformPt;
		this.eh = eh;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		final CyNetwork net = view.getModel();
		final CyNode n = net.addNode();

		// set the name attribute for the new node
		final String nodeName = "Node " + new_node_index;
		new_node_index++;

		final CyRow nodeRow = net.getRow(n);
		nodeRow.set(CyNetwork.NAME, nodeName);

		eh.flushPayloadEvents();
		View<CyNode> nv = view.getNodeView(n);
		nv.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, xformPt.getX());
		nv.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, xformPt.getY());

		// Apply visual style
		VisualStyle vs = vmm.getVisualStyle(view);
		vs.apply(net.getRow(n), nv);
		view.updateView();
	}
}
