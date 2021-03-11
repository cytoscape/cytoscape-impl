package org.cytoscape.view.table.internal;

import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.CELL_BACKGROUND_PAINT;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.COLUMN_FORMAT;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.COLUMN_GRAVITY;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.COLUMN_VISIBLE;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.COLUMN_WIDTH;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.ROW_HEIGHT;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.TABLE_ALTERNATE_ROW_COLORS;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.TABLE_GRID_VISIBLE;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.TABLE_VIEW_MODE;
import static org.cytoscape.view.table.internal.util.ViewUtil.invokeOnEDT;

import java.util.HashSet;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.TableViewChangedEvent;
import org.cytoscape.view.model.events.TableViewChangedListener;
import org.cytoscape.view.model.table.CyColumnView;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.presentation.property.table.TableMode;
import org.cytoscape.view.table.internal.impl.BrowserTable;
import org.cytoscape.view.table.internal.impl.BrowserTableColumnModel;
import org.cytoscape.view.table.internal.impl.BrowserTableColumnModelGravityEvent;
import org.cytoscape.view.table.internal.impl.BrowserTableColumnModelListener;
import org.cytoscape.view.table.internal.impl.BrowserTableModel;
import org.cytoscape.view.table.internal.impl.BrowserTableModel.ViewMode;

public class VisualPropertyChangeListener implements TableViewChangedListener {

	private final CyTableView tableView;
	private final BrowserTable browserTable;
	
	public VisualPropertyChangeListener(BrowserTable browserTable, CyTableView tableView) {
		this.tableView = tableView;
		this.browserTable = browserTable;
		handleTableColumnReorder();
	}
	
	private void handleTableColumnReorder() {	
		var colModel = (BrowserTableColumnModel) browserTable.getColumnModel();
		 
		colModel.addBrowserTableColumnModelListener(new BrowserTableColumnModelListener() {
			@Override
			public void columnGravityChanged(BrowserTableColumnModelGravityEvent event) {
				// Called when the user manually reorders columns by dragging.
				var colView1 = tableView.getColumnView(event.getColumn1Suid());
				var colView2 = tableView.getColumnView(event.getColumn2Suid());
				var colGrav1 = event.getColumn1Gravity();
				var colGrav2 = event.getColumn2Gravity();
				
				colView1.setLockedValue(COLUMN_GRAVITY, colGrav1);
				colView2.setLockedValue(COLUMN_GRAVITY, colGrav2);
			}
		});
	}
	
	@Override
	public void handleEvent(TableViewChangedEvent<?> e) {
		if (e.getSource() != tableView)
			return;

		boolean reorderCols = false;

		for (var record : e.getPayloadCollection()) {
			var vp = record.getVisualProperty();
			var value = record.getValue();
			var model = record.getView().getModel();

			if (model instanceof CyColumn) {
				var colView = (CyColumnView) record.getView();
				updateColumnVP(colView, vp, value);

				if (vp == COLUMN_GRAVITY) {
					reorderCols = true;
				}
			} else if (model instanceof CyTable) {
				if (vp == TABLE_VIEW_MODE) {
					changeSelectionMode((TableMode) value);
				} else if (vp == ROW_HEIGHT) {
					if (value instanceof Number)
						changeRowHeight(((Number) value).intValue());
				} else if (vp == TABLE_GRID_VISIBLE) {
					invokeOnEDT(() -> browserTable.setShowGrid(value == Boolean.TRUE));
				} else if (vp == TABLE_ALTERNATE_ROW_COLORS) {
					invokeOnEDT(() -> browserTable.repaint());
				}
			}
		}

		if (reorderCols) {
			var colModel = (BrowserTableColumnModel) browserTable.getColumnModel();
			colModel.reorderColumnsToRespectGravity();
		}
	}
	
	private void updateColumnVP(CyColumnView colView, VisualProperty<?> vp, Object value) {
		if (vp == COLUMN_VISIBLE) {
			boolean visible = !Boolean.FALSE.equals(value);
			var colModel = (BrowserTableColumnModel) browserTable.getColumnModel();
			var col = colModel.getTableColumn(colView.getSUID());
			colModel.setColumnVisible(col, visible);
		} else if (vp == CELL_BACKGROUND_PAINT) {
			browserTable.repaint();
		} else if (vp == COLUMN_FORMAT) {
			browserTable.repaint();
		} else if (vp == COLUMN_WIDTH) {
			if (value instanceof Number) {
				int width = ((Number) value).intValue();
				
				if (width > 0) {
					var colModel = (BrowserTableColumnModel) browserTable.getColumnModel();
					var col = colModel.getTableColumn(colView.getSUID());
					col.setPreferredWidth(width);
					col.setWidth(width);
				}
			}
		} else if (vp == COLUMN_GRAVITY) {
			if (value instanceof Number) {
				double gravity = ((Number) value).doubleValue();
				var colModel = (BrowserTableColumnModel) browserTable.getColumnModel();
				var column = colModel.getTableColumn(colView.getSUID());
				colModel.setColumnGravity(column, gravity);
			}
		}
	}

	// MKTODO this needs to go in the renderer
	private void changeSelectionMode(TableMode tableMode) {
		var model = (BrowserTableModel) browserTable.getModel();
		var viewMode = ViewMode.fromVisualPropertyValue(tableMode);
		model.setViewMode(viewMode);
		model.updateViewMode();
		
		if (viewMode == ViewMode.ALL && tableView.getModel().getColumn(CyNetwork.SELECTED) != null) {
			// Show the current selected rows
			var suidSelected = new HashSet<Long>();
			var suidUnselected = new HashSet<Long>();
			var selectedRows = tableView.getModel().getMatchingRows(CyNetwork.SELECTED, Boolean.TRUE);

			for (var row : selectedRows) {
				suidSelected.add(row.get(CyIdentifiable.SUID, Long.class));
			}

			if (!suidSelected.isEmpty())
				browserTable.changeRowSelection(suidSelected, suidUnselected);
		}
	}
	
	private void changeRowHeight(int height) {
		// TODO: calculate h based on the presence of sparklines, wrapped text, etc
		int h = height > 0 ? height : 16;
		
		invokeOnEDT(() -> browserTable.setRowHeight(h));
	}
}
