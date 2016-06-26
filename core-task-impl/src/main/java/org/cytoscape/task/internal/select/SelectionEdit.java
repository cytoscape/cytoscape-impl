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


import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.undo.AbstractCyEdit;


/** An undoable edit that will undo and redo selection of nodes and edges. */
final class SelectionEdit extends AbstractCyEdit {
	public enum SelectionFilter {
		NODES_ONLY, EDGES_ONLY, NODES_AND_EDGES;
	}

	private final CyEventHelper eventHelper;
	private final CyNetwork network;
	private final CyNetworkView view;
	private final SelectionFilter filter;
	private List<RowAndSelectionState> nodeRows;
	private List<RowAndSelectionState> edgeRows;

	SelectionEdit(final CyEventHelper eventHelper, final String description,
	              final CyNetwork network, final CyNetworkView view,
	              final SelectionFilter filter)
	{
		super(description);

		this.eventHelper = eventHelper;
		this.network     = network;
		this.view        = view;
		this.filter      = filter;

		saveSelectionState();
	}

	public void redo() {
		;
		saveAndRestoreState();
	}

	public void undo() {
		;
		saveAndRestoreState();
	}

	private void saveAndRestoreState() {
		final List<RowAndSelectionState> oldNodeRows = nodeRows;
		final List<RowAndSelectionState> oldEdgeRows = edgeRows;

		saveSelectionState();

		if (filter == SelectionFilter.NODES_ONLY || filter == SelectionFilter.NODES_AND_EDGES) {
			for (final RowAndSelectionState rowAndState : oldNodeRows)
				rowAndState.getRow().set(CyNetwork.SELECTED, rowAndState.isSelected());
		}

		if (filter == SelectionFilter.EDGES_ONLY || filter == SelectionFilter.NODES_AND_EDGES) {
			for (final RowAndSelectionState rowAndState : oldEdgeRows)
				rowAndState.getRow().set(CyNetwork.SELECTED, rowAndState.isSelected());
		}

		eventHelper.flushPayloadEvents();
		view.updateView();
	}

	private void saveSelectionState() {
		if (filter == SelectionFilter.NODES_ONLY || filter == SelectionFilter.NODES_AND_EDGES) {
			final Collection<CyRow> rows = network.getDefaultNodeTable().getAllRows();
			nodeRows = new ArrayList<RowAndSelectionState>(rows.size());
			for (final CyRow row : rows)
				nodeRows.add(new RowAndSelectionState(row, row.get(CyNetwork.SELECTED, Boolean.class)));
		}

		if (filter == SelectionFilter.EDGES_ONLY || filter == SelectionFilter.NODES_AND_EDGES) {
			final Collection<CyRow> rows = network.getDefaultEdgeTable().getAllRows();
			edgeRows = new ArrayList<RowAndSelectionState>(rows.size());
			for (final CyRow row : rows)
				edgeRows.add(new RowAndSelectionState(row, row.get(CyNetwork.SELECTED, Boolean.class)));
		}
	}
}


final class RowAndSelectionState {
	private final CyRow row;
	private final Boolean selected;

	RowAndSelectionState(final CyRow row, final Boolean selected) {
		this.row      = row;
		this.selected = selected;
	}

	CyRow getRow() { return row; }
	Boolean isSelected() { return selected; }
}