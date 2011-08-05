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

	public void columnAdded(TableColumnModelEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void columnMarginChanged(ChangeEvent e) {
		vizMapPropertySheetBuilder.updateTableView();
	}

	public void columnMoved(TableColumnModelEvent e) {
		// TODO Auto-generated method stub
	}

	public void columnRemoved(TableColumnModelEvent e) {
		// TODO Auto-generated method stub
	}

	public void columnSelectionChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
	}
}