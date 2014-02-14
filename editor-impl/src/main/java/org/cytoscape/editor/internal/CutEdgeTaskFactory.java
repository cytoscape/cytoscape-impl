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

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.task.AbstractEdgeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

public class CutEdgeTaskFactory extends AbstractEdgeViewTaskFactory {
	
	private final ClipboardManagerImpl clipMgr;
	private final VisualMappingManager vmMgr;
	private final UndoSupport undoSupport;
	private final CyEventHelper eventHelper;

	public CutEdgeTaskFactory(final ClipboardManagerImpl clipboardMgr,
							  final VisualMappingManager vmMgr,
							  final UndoSupport undoSupport,
	                          final CyEventHelper eventHelper) {
		this.clipMgr = clipboardMgr;
		this.vmMgr = vmMgr;
		this.undoSupport = undoSupport;
		this.eventHelper = eventHelper;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyEdge> edgeView, CyNetworkView networkView) {
		return new TaskIterator(new CutTask(networkView, edgeView, clipMgr, vmMgr, undoSupport, eventHelper));
	}
}
