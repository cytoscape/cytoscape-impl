package org.cytoscape.task.internal.select;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

public class DeselectAllEdgesTask extends AbstractSelectTask {
	
	public DeselectAllEdgesTask(CyNetwork net, CyServiceRegistrar serviceRegistrar) {
		super(net, serviceRegistrar);
	}

	@Override
	public void run(TaskMonitor tm) {
		tm.setTitle("Deselect All Edges");
		tm.setProgress(0.0);

		CyNetworkView view = getNetworkView(network);

		serviceRegistrar.getService(UndoSupport.class).postEdit(
				new SelectionEdit("Deselect All Edges", network, view, SelectionEdit.SelectionFilter.EDGES_ONLY,
						serviceRegistrar));

		tm.setStatusMessage("Deselecting Edges...");
		tm.setProgress(0.2);
		selectUtils.setSelectedEdges(network, network.getEdgeList(), false);

		tm.setStatusMessage("Updating View...");
		tm.setProgress(0.6);
		updateView();

		tm.setProgress(1.0);
	}
}
