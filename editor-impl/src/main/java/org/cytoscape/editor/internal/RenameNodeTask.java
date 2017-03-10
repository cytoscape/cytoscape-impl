package org.cytoscape.editor.internal;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.undo.UndoSupport;

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

public class RenameNodeTask extends AbstractTask {
	
	CyNetwork net;
	CyNode node;
	private final CyServiceRegistrar serviceRegistrar;

	@Tunable(description="New name for node:")
	public String newName;
	
	public RenameNodeTask(final CyNetworkView netView, final View<? extends CyIdentifiable> objView,
			final CyServiceRegistrar serviceRegistrar) {
		this.net = netView.getModel();
		this.serviceRegistrar = serviceRegistrar;
		
		if (objView.getModel() instanceof CyNode)
			node = (CyNode)objView.getModel();
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		if (node == null)
			return;

		CyRow nodeRow = net.getRow(node);
		String oldName = nodeRow.get(CyNetwork.NAME, String.class);
		nodeRow.set(CyNetwork.NAME, newName);
		nodeRow.set(CyRootNetwork.SHARED_NAME, newName);

		final UndoSupport undoSupport = serviceRegistrar.getService(UndoSupport.class);
		undoSupport.postEdit(new RenameNodeEdit(net, node, oldName, newName));
	}
}
