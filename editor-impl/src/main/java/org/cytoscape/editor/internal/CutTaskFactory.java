package org.cytoscape.editor.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.work.TaskIterator;

/*
 * #%L
 * Cytoscape Editor Impl (editor-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class CutTaskFactory extends AbstractNetworkViewTaskFactory {
	
	private final ClipboardManagerImpl clipMgr;
	private final CyServiceRegistrar serviceRegistrar;

	public CutTaskFactory(ClipboardManagerImpl clipboardMgr, CyServiceRegistrar serviceRegistrar) {
		this.clipMgr = clipboardMgr;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		return new TaskIterator(new CutTask(networkView, clipMgr, serviceRegistrar));
	}

	@Override
	public boolean isReady(CyNetworkView networkView) {
		if (!super.isReady(networkView))
			return false;

		// Make sure we've got something selected
		var nodes = CyTableUtil.getNodesInState(networkView.getModel(), CyNetwork.SELECTED, true);

		if (nodes != null && !nodes.isEmpty())
			return true;

		var edges = CyTableUtil.getEdgesInState(networkView.getModel(), CyNetwork.SELECTED, true);

		if (edges != null && !edges.isEmpty())
			return true;

		var annotations = serviceRegistrar.getService(AnnotationManager.class).getSelectedAnnotations(networkView);

		if (annotations != null && !annotations.isEmpty())
			return true;

		return false;
	}
}
