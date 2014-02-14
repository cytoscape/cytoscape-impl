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

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class CopyTask extends AbstractTask {
	
	private final CyNetworkView netView;
	private final ClipboardManagerImpl clipMgr;
	private final Set<CyNode> selNodes;
	private final Set<CyEdge> selEdges;
	
	public CopyTask(final CyNetworkView netView, final ClipboardManagerImpl clipMgr) {
		this.netView = netView;
		// Get all of the selected nodes and edges
		selNodes = new HashSet<CyNode>(CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED, true));
		selEdges = new HashSet<CyEdge>(CyTableUtil.getEdgesInState(netView.getModel(), CyNetwork.SELECTED, true));

		// Save them in our list
		this.clipMgr = clipMgr;
	}

	@SuppressWarnings("unchecked")
	public CopyTask(final CyNetworkView netView,
					final View<?extends CyIdentifiable> objView, 
	                final ClipboardManagerImpl clipMgr) {
		// Get all of the selected nodes and edges first
		this(netView, clipMgr);

		// Now, make sure we add our
		if (objView.getModel() instanceof CyNode)
			selNodes.add(((View<CyNode>)objView).getModel());
		else if (objView.getModel() instanceof CyEdge)
			selEdges.add(((View<CyEdge>)objView).getModel());
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		clipMgr.copy(netView, selNodes, selEdges);
	}
}
