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

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CutTask extends AbstractTask {
	CyNetwork net;
	CyNetworkView view;
	ClipboardManagerImpl mgr;
	final UndoSupport undoSupport;
	List<CyNode> selNodes;
	List<CyEdge> selEdges;
	
	public CutTask(final CyNetworkView netView, final ClipboardManagerImpl clipMgr, 
	               final UndoSupport undoSupport) {
		this.view = netView;
		this.undoSupport = undoSupport;
		// Get all of the selected nodes and edges
		selNodes = CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED, true);
		selEdges = CyTableUtil.getEdgesInState(netView.getModel(), CyNetwork.SELECTED, true);

		// Save them in our list
		mgr = clipMgr;
	}

	public CutTask(final CyNetworkView netView, final View<?extends CyIdentifiable> objView, 
	               final ClipboardManagerImpl clipMgr, final UndoSupport undoSupport) {

		// Get all of the selected nodes and edges first
		this(netView, clipMgr, undoSupport);

		// Now, make sure we add our
		if (objView.getModel() instanceof CyNode) {
			CyNode node = ((View<CyNode>)objView).getModel();
			if (!selNodes.contains(node))
				selNodes.add(node);
		} else if (objView.getModel() instanceof CyEdge) {
			CyEdge edge = ((View<CyEdge>)objView).getModel();
			if (!selEdges.contains(edge))
				selEdges.add(edge);
		}
	}

	public void run(TaskMonitor tm) throws Exception {
		mgr.cut(view, selNodes, selEdges);
		undoSupport.postEdit(new CutEdit(mgr, view));
	}
}
