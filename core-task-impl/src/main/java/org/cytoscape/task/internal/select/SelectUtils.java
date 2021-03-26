package org.cytoscape.task.internal.select;

import java.util.ArrayList;
import java.util.Collection;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;

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

public final class SelectUtils {
	
	private final CyServiceRegistrar serviceRegistrar;

	public SelectUtils(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	public void setSelectedNodes(CyNetwork network, Collection<CyNode> nodes, boolean select) {
		setSelected(network,nodes, select, network.getDefaultNodeTable());
	}

	public void setSelectedEdges(CyNetwork network, Collection<CyEdge> edges, boolean select) {
		setSelected(network,edges, select, network.getDefaultEdgeTable());
	}

	private void setSelected(
			CyNetwork network,
			Collection<? extends CyIdentifiable> objects,
			boolean select,
			CyTable table
	) {
		// Don't autobox
		final Boolean value;
		
		if (select)
			value = Boolean.TRUE;
		else
			value = Boolean.FALSE;

		// Disable all events from our table
		var eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.silenceEventSource(table);

		// Create the RowSetRecord collection
		var rowsChanged = new ArrayList<RowSetRecord>();

		// The list of objects will be all nodes or all edges
		for (var nodeOrEdge : objects) {
			var row = nodeOrEdge != null ? table.getRow(nodeOrEdge.getSUID()) : null;

			if (row != null) {
				row.set(CyNetwork.SELECTED, value);
				rowsChanged.add(new RowSetRecord(row, CyNetwork.SELECTED, value, value));
			}
		}

		// Enable all events from our table
		eventHelper.unsilenceEventSource(table);

		var event = new RowsSetEvent(table, rowsChanged);
		eventHelper.fireEvent(event);
	}
	
	public void setVisible(CyNetworkView networkView, Collection<CyNode> selectedNodes, Collection<CyEdge> selectedEdges) {
		var network = networkView.getModel();
		var hideFactory = serviceRegistrar.getService(HideTaskFactory.class);
		var hideTasks = hideFactory.createTaskIterator(networkView, network.getNodeList(), network.getEdgeList());
		
		var unhideFactory = serviceRegistrar.getService(UnHideTaskFactory.class);
		var unhideTasks = unhideFactory.createTaskIterator(networkView, selectedNodes, selectedEdges);
		
		var taskIterator = new TaskIterator();
		taskIterator.append(hideTasks);
		taskIterator.append(unhideTasks);
		
		var taskManager = serviceRegistrar.getService(SynchronousTaskManager.class);
		taskManager.execute(taskIterator);
	}
}
