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

import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

public class CutTaskFactory extends AbstractNetworkViewTaskFactory {
	
	private final ClipboardManagerImpl clipMgr;
	private final VisualMappingManager vmMgr;
	private final UndoSupport undoSupport;
	private CyEventHelper eventHelper;

	public CutTaskFactory(final ClipboardManagerImpl clipboardMgr,
						  final VisualMappingManager vmMgr,
						  final UndoSupport undoSupport, 
	                      final CyEventHelper eventHelper) {
		this.clipMgr = clipboardMgr;
		this.vmMgr = vmMgr;
		this.undoSupport = undoSupport;
		this.eventHelper = eventHelper;
	}

	@Override
	public boolean isReady(CyNetworkView networkView) {
		if (!super.isReady(networkView))
			return false;

		// Make sure we've got something selected
		List<CyNode> selNodes = CyTableUtil.getNodesInState(networkView.getModel(), CyNetwork.SELECTED, true);
		if (selNodes != null && selNodes.size() > 0) return true;

		List<CyEdge> selEdges = CyTableUtil.getEdgesInState(networkView.getModel(), CyNetwork.SELECTED, true);
		if (selEdges != null && selEdges.size() > 0) return true;

		return false;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		return new TaskIterator(new CutTask(networkView, clipMgr, vmMgr, undoSupport, eventHelper));
	}
}
