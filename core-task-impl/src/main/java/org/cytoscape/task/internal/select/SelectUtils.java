package org.cytoscape.task.internal.select;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;

final class SelectUtils {
	final CyEventHelper eventHelper;

	public SelectUtils(CyEventHelper helper) {
		this.eventHelper = helper;
	}

	void setSelectedNodes(final CyNetwork network, final Collection<CyNode> nodes, final boolean select) {
		setSelected(network,nodes, select, network.getDefaultNodeTable());
	}

	void setSelectedEdges(final CyNetwork network, final Collection<CyEdge> edges, final boolean select) {
		setSelected(network,edges, select, network.getDefaultEdgeTable());
	}

	private void setSelected(final CyNetwork network, final Collection<? extends CyIdentifiable> objects, 
	                         final boolean select, final CyTable table) {

		// Don't autobox
		Boolean value;
		if (select)
			value = Boolean.TRUE;
		else
			value = Boolean.FALSE;

		// Disable all events from our table
		eventHelper.silenceEventSource(table);

		// Create the RowSetRecord collection
		List<RowSetRecord> rowsChanged = new ArrayList<RowSetRecord>();

		for (final CyIdentifiable nodeOrEdge : objects) {
			CyRow row = network.getRow(nodeOrEdge);
			row.set(CyNetwork.SELECTED, value);
			rowsChanged.add(new RowSetRecord(row, CyNetwork.SELECTED, value, value));
		}

		// Enable all events from our table
		eventHelper.unsilenceEventSource(table);

		RowsSetEvent event = new RowsSetEvent(table, rowsChanged);
		eventHelper.fireEvent(event);
	}
}
