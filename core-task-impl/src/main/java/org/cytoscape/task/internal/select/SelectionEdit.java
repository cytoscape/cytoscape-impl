package org.cytoscape.task.internal.select;


import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.util.swing.AbstractCyEdit;


/** An undoable edit that will undo and redo selection of nodes and edges. */
final class SelectionEdit extends AbstractCyEdit {
	public static enum SelectionFilter {
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
		super.redo();
		saveAndRestoreState();
	}

	public void undo() {
		super.undo();
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