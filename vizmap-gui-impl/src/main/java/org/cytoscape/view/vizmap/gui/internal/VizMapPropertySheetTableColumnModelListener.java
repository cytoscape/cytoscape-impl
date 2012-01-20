package org.cytoscape.view.vizmap.gui.internal;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

public final class VizMapPropertySheetTableColumnModelListener implements
		TableColumnModelListener {

	private VizMapPropertySheetBuilder vizMapPropertySheetBuilder;

	public VizMapPropertySheetTableColumnModelListener(VizMapPropertySheetBuilder vizMapPropertySheetBuilder) {
		this.vizMapPropertySheetBuilder = vizMapPropertySheetBuilder;
	}

	public void columnAdded(TableColumnModelEvent e) {
		vizMapPropertySheetBuilder.updateTableView();
	}

	public void columnMarginChanged(ChangeEvent e) {
		vizMapPropertySheetBuilder.updateTableView();
	}

	public void columnMoved(TableColumnModelEvent e) {
	}

	public void columnRemoved(TableColumnModelEvent e) {
	}

	public void columnSelectionChanged(ListSelectionEvent e) {
	}
}