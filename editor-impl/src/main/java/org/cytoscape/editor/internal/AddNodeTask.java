package org.cytoscape.editor.internal;

import java.awt.geom.Point2D;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Editor Impl (editor-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class AddNodeTask extends AbstractNetworkViewTask implements ObservableTask {

	private final Point2D xformPt;
	private final String nodeName;
	private final CyServiceRegistrar registrar;
	
	private boolean postUndo = true;
	private CyNode node = null;
	
	public AddNodeTask(CyNetworkView view, Point2D xformPt, String nodeName, CyServiceRegistrar registrar) {
		super(view);
		this.xformPt = xformPt;
		this.nodeName = nodeName;
		this.registrar = registrar;
	}
	
	public void setPostUndo(boolean postUndo) {
		this.postUndo = postUndo;
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		final CyNetwork net = view.getModel();
		node = net.addNode();
		
		final CyRow nodeRow = net.getRow(node);
		nodeRow.set(CyNetwork.NAME, nodeName);

		final CyEventHelper eventHelper = registrar.getService(CyEventHelper.class);
		eventHelper.flushPayloadEvents();
		View<CyNode> nv = view.getNodeView(node);
		nv.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, xformPt.getX());
		nv.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, xformPt.getY());

		// Apply visual style
		final VisualMappingManager vmMgr = registrar.getService(VisualMappingManager.class);
		VisualStyle vs = vmMgr.getVisualStyle(view);
		vs.apply(net.getRow(node), nv);
		view.updateView();
		
		if(postUndo) {
			AddNodeEdit addNodeEdit = new AddNodeEdit(view, node, xformPt, nodeName, registrar);
			addNodeEdit.post();
		}
	}

	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(CyNode.class.equals(type)) {
			return type.cast(node);
		}
		return null;
	}
}
