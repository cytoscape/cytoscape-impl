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
import org.cytoscape.task.AbstractNetworkViewLocationTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

public class PasteTaskFactory extends AbstractNetworkViewLocationTaskFactory {

	private final CyEventHelper eh;
	private final VisualMappingManager vmm;
	private final ClipboardManagerImpl clipMgr;
	private final UndoSupport undoSupport;
	
	public PasteTaskFactory(final ClipboardManagerImpl mgr, CyEventHelper eh, 
	                        UndoSupport undoSupport, VisualMappingManager vmm) {
		this.clipMgr = mgr;
		this.eh = eh;
		this.vmm = vmm;
		this.undoSupport = undoSupport;
	}

	@Override
	public boolean isReady(CyNetworkView networkView, Point2D javaPt, Point2D xformPt) {
		if (networkView == null)
			return false;

		return clipMgr.clipboardHasData();
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView, 
	                                       Point2D javaPt, Point2D xformPt) {
		return new TaskIterator(new PasteTask(vmm, networkView, xformPt, clipMgr, undoSupport));
	}

}
