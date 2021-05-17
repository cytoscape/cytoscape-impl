package org.cytoscape.view.table.internal;

import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.CELL_BACKGROUND_PAINT;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.COLUMN_FORMAT;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.COLUMN_GRAVITY;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.COLUMN_SELECTED;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.COLUMN_TEXT_WRAPPED;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.COLUMN_VISIBLE;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.COLUMN_WIDTH;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.ROW_HEIGHT;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.ROW_SELECTED;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.TABLE_ALTERNATE_ROW_COLORS;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.TABLE_GRID_VISIBLE;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.TABLE_ROW_HEIGHT;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.TABLE_VIEW_MODE;
import static org.cytoscape.view.table.internal.util.ViewUtil.invokeOnEDT;

import java.util.HashSet;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.View;
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
import org.cytoscape.view.table.internal.impl.BrowserTableModel.ViewMode;
import org.cytoscape.view.table.internal.impl.BrowserTableRowHeader;


public class VisualPropertyChangeListener implements TableViewChangedListener {

	private final CyTableView tableView;
	private final BrowserTable browserTable;
	private final BrowserTableRowHeader rowHeader;
	
	public VisualPropertyChangeListener(
			BrowserTable browserTable,
			CyTableView tableView,
			BrowserTableRowHeader rowHeader
	) {
		this.tableView = tableView;
		this.browserTable = browserTable;
		this.rowHeader = rowHeader;
		
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
	@SuppressWarnings("unchecked")
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

				if (vp == COLUMN_GRAVITY)
					reorderCols = true;
			} else if (model instanceof CyRow) {
				var rowView = (View<CyRow>) record.getView();
				updateRowVP(rowView, vp, value);
			} else if (model instanceof CyTable) {
				if (vp == TABLE_VIEW_MODE) {
					changeSelectionMode((TableMode) value);
				} else if (vp == TABLE_ROW_HEIGHT) {
					invokeOnEDT(() -> {
						// Change table's row height
						browserTable.resetRowHeight();
						// Force repaint on the Row Header
						rowHeader.update();
					});
				} else if (vp == TABLE_GRID_VISIBLE) {
					invokeOnEDT(() -> browserTable.setShowGrid(Boolean.TRUE.equals(value)));
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
			boolean visible = Boolean.TRUE.equals(value);
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
		} else if (vp == COLUMN_TEXT_WRAPPED) {
			invokeOnEDT(() -> {
				if (Boolean.TRUE.equals(value))
					browserTable.repaint();
				else
					browserTable.resetRowHeight();
			});
		} else if (vp == COLUMN_GRAVITY) {
			if (value instanceof Number) {
				double gravity = ((Number) value).doubleValue();
				var colModel = (BrowserTableColumnModel) browserTable.getColumnModel();
				var column = colModel.getTableColumn(colView.getSUID());
				
				if (column != null)
					colModel.setColumnGravity(column, gravity);
			}
		} else if (vp == COLUMN_SELECTED) {
			if (value instanceof Boolean) {
				boolean selected = Boolean.TRUE.equals(value);
				var idx = browserTable.getColumnModel().getColumnIndex(colView.getSUID());
				
				if (idx >= 0 && idx < browserTable.getColumnCount() && selected != browserTable.isColumnSelected(idx)) {
					if (selected)
						browserTable.addColumnSelectionInterval(idx, idx);
					else
						browserTable.removeColumnSelectionInterval(idx, idx);
				}
			}
		}
	}
	
	private void updateRowVP(View<CyRow> rowView, VisualProperty<?> vp, Object value) {
		if (vp == ROW_HEIGHT) {
			if (value instanceof Number) {
				int h = ((Number) value).intValue();
				
				if (h > 0) {
					var tableModel = browserTable.getBrowserTableModel();
					var pkName = tableView.getModel().getPrimaryKey().getName();
					var pk = rowView.getModel().getRaw(pkName);
					
					if (pk != null) {
						var idx = tableModel.indexOfRow(rowView.getModel());
						idx = browserTable.convertRowIndexToView(idx);
						
						// Always check the current row height to avoid an infinite loop!
						if (idx >= 0 && idx < browserTable.getRowCount() && h != browserTable.getRowHeight(idx))
							browserTable.setRowHeight(idx, h);
					}
					
					rowHeader.update();
				}
			}
		} else if (vp == ROW_SELECTED) {
			if (value instanceof Boolean) {
				boolean selected = Boolean.TRUE.equals(value);
				
				var tableModel = browserTable.getBrowserTableModel();
				var pkName = tableView.getModel().getPrimaryKey().getName();
				var pk = rowView.getModel().getRaw(pkName);
				
				if (pk != null) {
					var idx = tableModel.indexOfRow(rowView.getModel());
					idx = browserTable.convertRowIndexToView(idx);
					
					if (idx >= 0 && idx < browserTable.getRowCount() && selected != browserTable.isRowSelected(idx)) {
						if (selected)
							browserTable.addRowSelectionInterval(idx, idx);
						else
							browserTable.removeRowSelectionInterval(idx, idx);
					}
				}
			}
		}
	}

	// MKTODO this needs to go in the renderer
	private void changeSelectionMode(TableMode tableMode) {
		var tableModel = browserTable.getBrowserTableModel();
		var viewMode = ViewMode.fromVisualPropertyValue(tableMode);
		tableModel.setViewMode(viewMode);
		tableModel.updateViewMode();
		
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
}
