package org.cytoscape.view.table.internal.impl;

import static org.cytoscape.util.swing.LookAndFeelUtil.getSmallFontSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isMac;
import static org.cytoscape.util.swing.LookAndFeelUtil.isWindows;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.COLUMN_EDITABLE;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.COLUMN_GRAVITY;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.COLUMN_SELECTED;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.COLUMN_VISIBLE;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.COLUMN_WIDTH;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.ROW_HEIGHT;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.ROW_SELECTED;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.TABLE_GRID_VISIBLE;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.TABLE_ROW_HEIGHT;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.TABLE_VIEW_MODE;
import static org.cytoscape.view.table.internal.impl.BrowserTableModel.ViewMode.ALL;
import static org.cytoscape.view.table.internal.impl.BrowserTableModel.ViewMode.AUTO;
import static org.cytoscape.view.table.internal.impl.BrowserTableModel.ViewMode.SELECTED;
import static org.cytoscape.view.table.internal.util.ViewUtil.invokeOnEDT;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.ColumnNameChangedEvent;
import org.cytoscape.model.events.ColumnNameChangedListener;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.ColumnResizer;
import org.cytoscape.util.swing.TextWrapToolTip;
import org.cytoscape.view.model.events.AboutToRemoveColumnViewEvent;
import org.cytoscape.view.model.events.AboutToRemoveColumnViewListener;
import org.cytoscape.view.model.events.AddedColumnViewEvent;
import org.cytoscape.view.model.events.AddedColumnViewListener;
import org.cytoscape.view.table.internal.impl.BrowserTableModel.ViewMode;
import org.cytoscape.view.table.internal.util.TableBrowserUtil;
import org.cytoscape.view.table.internal.util.ValidatedObjectAndEditString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Table Presentation Impl (table-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class BrowserTable extends JTable
		implements MouseListener, ActionListener, MouseMotionListener, AboutToRemoveColumnViewListener,
		AddedColumnViewListener, ColumnNameChangedListener, RowsSetListener {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	private static final String LINE_BREAK = "\n";
	private static final String CELL_BREAK = "\t";

	private static final Font BORDER_FONT = UIManager.getFont("Label.font").deriveFont(getSmallFontSize());
	
	private CellEditorRemover editorRemover;
	private final TableCellRenderer cellRenderer;
	private final HashMap<String, Integer> columnWidthMap = new HashMap<>();

	// For right-click menu
	private JPopupMenu popupMenu;
	private JMenuItem openFormulaBuilderMenuItem;

	private MultiLineTableCellEditor multiLineCellEditor;
	
	private final PopupMenuHelper popupMenuHelper;
	private final CyServiceRegistrar serviceRegistrar;
	
	private boolean columnWidthChanged;
	
	private boolean ignoreColumnSelectionEvents;
	private boolean ignoreRowSelectionEvents;
	private boolean ignoreRowSetEvents;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public BrowserTable(PopupMenuHelper popupMenuHelper, CyServiceRegistrar serviceRegistrar) {
		this.popupMenuHelper = popupMenuHelper;
		this.serviceRegistrar = serviceRegistrar;
		
		cellRenderer = new BrowserTableCellRenderer(serviceRegistrar);
		multiLineCellEditor = new MultiLineTableCellEditor();
		
		init();
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public void setModel(TableModel dataModel) {
		super.setModel(dataModel);
		
		var columnModel = new BrowserTableColumnModel();
		setColumnModel(columnModel);

		if (dataModel instanceof BrowserTableModel) {
			var model = (BrowserTableModel) dataModel;
			var tableView = model.getTableView();
			
			// Only apply the row height value from the visual property if it has been explicitly set,
			// because it depends on "Table.font" property set to the current LAF,
			// which means the default from the BasicTableVisualLexicon could be too small and crop the cell text
			if (tableView.isSet(TABLE_ROW_HEIGHT))
				setRowHeight(tableView.getVisualProperty(TABLE_ROW_HEIGHT));
			
			setShowGrid(Boolean.TRUE.equals(tableView.getVisualProperty(TABLE_GRID_VISIBLE)));
			
			for (int i = 0; i < model.getColumnCount(); i++) {
				var name = model.getColumnName(i);
				var view = tableView.getColumnView(name);
				boolean visible = view.getVisualProperty(COLUMN_VISIBLE);
				double gravity  = view.getVisualProperty(COLUMN_GRAVITY);
				
				var tableColumn = new TableColumn(i);
				tableColumn.setHeaderValue(name);
				tableColumn.setHeaderRenderer(new BrowserTableHeaderRenderer(serviceRegistrar));
				columnModel.addColumn(tableColumn, view.getSUID(), visible, gravity);
			}
			
			var view = tableView.getColumnView(CyNetwork.SELECTED);
			
			if (view != null && !view.isSet(COLUMN_EDITABLE))
				view.setLockedValue(COLUMN_EDITABLE, false);
			
			var viewMode = ViewMode.fromVisualPropertyValue(tableView.getVisualProperty(TABLE_VIEW_MODE));
			model.setViewMode(viewMode);
			model.updateViewMode();
		}
		
		columnModel.reorderColumnsToRespectGravity();
	}
	
	@Override
	public void setColumnModel(TableColumnModel columnModel) {
		if (columnModel != getColumnModel()) {
			super.setColumnModel(columnModel);
			
			// Update the COLUMN_SELECTED Visual Property
			columnModel.getSelectionModel().addListSelectionListener(e -> {
				if (!e.getValueIsAdjusting() && !ignoreColumnSelectionEvents)
					setColumnSelectedVP(e.getFirstIndex(), e.getLastIndex());
			});
		}
	}
	
	@Override
	public void setShowGrid(boolean showGrid) {
		if (showGrid) {
			var color = UIManager.getColor("Table.gridColor");
			
			if (color == null) // Just in case "Table.gridColor" is not set for the current LAF
				color = UIManager.getColor("Separator.foreground");
			
			// Cytoscape had to start with a 100% transparency for the UIManager property "Table.gridColor",
			// otherwise the Swing implementations would not respect the property "Table.showGrid" with a 'false' value.
			// We can now set the "correct" transparency of the LAF color and set it again.
			if (color.getAlpha() == 0)
				color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
			
			setGridColor(color);
		}
		
		super.setShowGrid(showGrid);
	}

	/**
	 * Returns null if the table model is not an instance of {@link BrowserTableModel} yet.
	 */
	public BrowserTableModel getBrowserTableModel() {
		return getModel() instanceof BrowserTableModel ? (BrowserTableModel) getModel() : null;
	}
	
	@Override
	public void selectAll() {
		super.selectAll();
		
		// Also make sure all columns and rows really are selected
		int columnCount = getColumnModel().getColumnCount();
		int rowCount = getRowCount();
		
		if (columnCount > 0)
			getColumnModel().getSelectionModel().addSelectionInterval(0, columnCount - 1);
		if (rowCount > 0)
			getSelectionModel().addSelectionInterval(0, rowCount - 1);
	}
	
	@Override
	public synchronized void addMouseListener(MouseListener listener) {
		// Hack to prevent selected rows from being deselected when the user
		// CONTROL-clicks one of those rows on Mac (popup trigger).
		super.addMouseListener(new ProxyMouseListener(listener));
	}
	
	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		return cellRenderer;
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		if (super.isCellEditable(row, column)) {
			var tableModel = getBrowserTableModel();
			
			// Also check the visual property...
			if (tableModel != null) {
				var tableView = tableModel.getTableView();
				var name = getColumnName(column);
				var view = tableView.getColumnView(name);
				
				return Boolean.TRUE.equals(view.getVisualProperty(COLUMN_EDITABLE));
			} else {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean editCellAt(int row, int column, EventObject e) {
		if (cellEditor != null && !cellEditor.stopCellEditing())
			return false;

		if (row < 0 || row >= getRowCount() || column < 0 || column >= getColumnCount())
			return false;

		if (!isCellEditable(row, column))
			return false;

		if (editorRemover == null) {
			KeyboardFocusManager fm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
			editorRemover = new CellEditorRemover(fm);
			fm.addPropertyChangeListener("permanentFocusOwner", editorRemover);
		}

		var editor = getCellEditor(row, column);

		// remember the table row, because tableModel will disappear if
		// user click on open space on canvas, so we have to remember it before it is gone
		var tableModel = getBrowserTableModel();
		
		if (tableModel == null)
			return false;
		
		var cyRow = tableModel.getCyRow(convertRowIndexToModel(row));
		var columnName = tableModel.getColumnName(convertColumnIndexToModel(column));
		editorRemover.setCellData(cyRow, columnName);

		if (editor != null && editor.isCellEditable(e)) {
			// Do this first so that the bounds of the JTextArea editor will be correct.
			setEditingRow(row);
			setEditingColumn(column);
			setCellEditor(editor);
			editor.addCellEditorListener(this);

			editorComp = prepareEditor(editor, row, column);

			if (editorComp == null) {
				removeEditor();
				return false;
			}

			var cellRect = getCellRect(row, column, false);

			if (editor instanceof MultiLineTableCellEditor) {
				var prefSize = editorComp.getPreferredSize();
				((JComponent) editorComp).putClientProperty(MultiLineTableCellEditor.UPDATE_BOUNDS, Boolean.TRUE);
				editorComp.setBounds(cellRect.x, cellRect.y, Math.max(cellRect.width, prefSize.width),
						Math.max(cellRect.height, prefSize.height));
				((JComponent) editorComp).putClientProperty(MultiLineTableCellEditor.UPDATE_BOUNDS, Boolean.FALSE);
			} else {
				editorComp.setBounds(cellRect);
			}
			
			editorComp.addKeyListener(new KeyListener() {

				private void deselect(KeyEvent e) {
					e.consume();
					Component comp = e.getComponent();
					comp.removeKeyListener(this);
					editor.stopCellEditing();
					
				}

				@Override
				public void keyTyped(KeyEvent e) {
					// Ignore...
				}

				@Override
				public void keyReleased(KeyEvent e) {
					// Ignore...
				}

				@Override
				public void keyPressed(KeyEvent event) {
					int newRow = row, newColumn = column;
					int modifiers = event.getModifiersEx();

					if (event.getKeyCode() == KeyEvent.VK_ENTER) {
						if (modifiers == 0) {
							newRow += 1;
						} else if (modifiers == KeyEvent.VK_SHIFT) {
							newRow -= 1;
						} else {
							return;
						}
					} else if (event.getKeyCode() == KeyEvent.VK_TAB) {
						if (modifiers == 0) {
							newColumn += 1;
						} else if (modifiers == KeyEvent.VK_SHIFT) {
							newColumn -= 1;
						} else {
							return;
						}
					} else {
						return;
					}
					
					deselect(event);
					changeSelection(newRow, newColumn, false, false);
				}
			});

			add(editorComp);
			editorComp.validate();

			return true;
		}

		return false;
	}

	@Override
	public void addRowSelectionInterval(int index0, int index1) {
		super.addRowSelectionInterval(index0, index1);
		// Make sure this is set, so the selected cell is correctly rendered when there is more than one selected row
		((BrowserTableListSelectionModel) selectionModel).setLastSelectedRow(index1);
	}
	
	@Override
	public int getSelectedRow() {
		// Try the last selected row first
		int row = ((BrowserTableListSelectionModel) selectionModel).getLastSelectedRow();
		
		if (row < 0 || !isRowSelected(row))
			row = selectionModel.getMinSelectionIndex();
		
		return row;
	}
	
	@Override
	public void columnAdded(TableColumnModelEvent e) {
		super.columnAdded(e);
		
		if (getParent() == null)
			return; // Updating the column width here does not work
		
		// Update the COLUMN_WIDTH visual property value.
		var colIdx = e.getToIndex();
		syncColumnWidth(colIdx);
		resizeAndRepaint();
		
		// Scroll to the new column
		scrollRectToVisible(getCellRect(0, colIdx, true));
	}
	
	@Override
	public void columnRemoved(TableColumnModelEvent e) {
		super.columnRemoved(e);
		
		// The removed (or hidden) column might have COLUMN_TEXT_WRAPPED set to true,
		// which affected the row height. So we need to reset it.
		resetRowHeight();
	}
	
	@Override
	public void addNotify() {
		super.addNotify();
		
		// Try to set the COLUMN_WIDTH visual property value to all columns
		int columnCount = getColumnCount();
		
		for (int i = 0; i < columnCount; i++)
			syncColumnWidth(i);
		
		resizeAndRepaint();
	}
	
	@Override
	public void setRowHeight(int row, int rowHeight) {
		int old = getRowHeight(row);
		
		if (rowHeight == old)
			return;
		
		super.setRowHeight(row, rowHeight);
		
		// Update the ROW_HEIGHT visual property
		var tableModel = getBrowserTableModel();
		
		if (tableModel == null)
			return;
		
		var tableView = tableModel.getTableView();
		var cyRow = tableModel.getCyRow(convertRowIndexToModel(row));
		var rowView = tableView.getRowView(cyRow);

		if (!rowView.isSet(ROW_HEIGHT) || rowHeight != rowView.getVisualProperty(ROW_HEIGHT)) {
			rowView.setLockedValue(ROW_HEIGHT, rowHeight);
			// If we don't force the event to be fired now, the correct value for this rowView can get out of sync,
			// which will almost certainly cause an infinite loop!
			serviceRegistrar.getService(CyEventHelper.class).flushPayloadEvents();
		}
		
		fireRowHeightChange(old, rowHeight);
	}
	
	@Override
	public JToolTip createToolTip() {
		var tip = new TextWrapToolTip();
		tip.setMaximumSize(new Dimension(480, 320));
		tip.setComponent(this);
		
		return tip;
	}
	
	public void showListContents(int modelRow, int modelColumn, MouseEvent e) {
		var tableModel = getBrowserTableModel();
		
		if (tableModel == null)
			return;
		
		var columnType = modelColumn >= 0 && modelColumn < tableModel.getColumnCount()
				? tableModel.getCyColumn(modelColumn).getType()
				: null;

		if (columnType == List.class) {
			var value = (ValidatedObjectAndEditString) tableModel.getValueAt(modelRow, modelColumn);

			if (value != null) {
				var list = (List<?>) value.getValidatedObject();
				
				if (list != null && !list.isEmpty())
					showCellMenu(List.class, list, "Entries", e);
			}
		}
	}
	
	/**
	 * This method initializes popupMenu
	 * 
	 * @return the inilialised pop-up menu
	 */
	public JPopupMenu getPopupMenu() {
		if (popupMenu != null)
			return popupMenu;

		popupMenu = new JPopupMenu();
		openFormulaBuilderMenuItem = new JMenuItem("Open Formula Builder");

		var table = this;
		
		openFormulaBuilderMenuItem.addActionListener(evt -> {
			int cellRow = table.getSelectedRow();
			int cellColumn = table.getSelectedColumn();
			var tableModel = getBrowserTableModel();
			
			if (tableModel == null)
				return;
			
			var rootFrame = (JFrame) SwingUtilities.getRoot(table);
			
			if (cellRow == -1 || cellColumn == -1 || !tableModel.isCellEditable(cellRow, cellColumn)) {
				JOptionPane.showMessageDialog(rootFrame, "Can't enter a formula w/o a selected cell.",
						"Information", JOptionPane.INFORMATION_MESSAGE);
			} else {
				// MKTODO
//				String columnName = tableModel.getColumnName(cellColumn);
//				FormulaBuilderDialog formulaBuilderDialog = new FormulaBuilderDialog(compiler, BrowserTable.this,
//						rootFrame, columnName);
//				formulaBuilderDialog.setLocationRelativeTo(rootFrame);
//				formulaBuilderDialog.setVisible(true);
			}
		});

		return popupMenu;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		selectFocusedCell(e);
		maybeShowPopup(e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e) && (getSelectedRows().length != 0)) {
			int viewColumn = getColumnModel().getColumnIndexAtX(e.getX());
			int viewRow = rowAtPoint(e.getPoint());
			int modelColumn = convertColumnIndexToModel(viewColumn);
			int modelRow = convertRowIndexToModel(viewRow);
			
			var tableModel = (BrowserTableModel) this.getModel();
			
			// Bail out if we're at the ID column:
			if (tableModel.isPrimaryKey(modelColumn))
				return;

			// Make sure the column and row we're clicking on actually exists!
			if (modelColumn >= tableModel.getColumnCount() || modelRow >= tableModel.getRowCount())
				return;
			
			// Display List menu.
			showListContents(modelRow, modelColumn, e);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// Ignore...
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// Ignore...
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		// save the column width, if user adjust column width manually
		if (e.getSource() instanceof JTableHeader) {
			 int index = getColumnModel().getColumnIndexAtX(e.getX());
			
			if (index != -1) {
				int colWidth = getColumnModel().getColumn(index).getWidth();
				this.columnWidthMap.put(this.getColumnName(index), colWidth);
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// Ignore...
	}
	
	@Override
	public void columnMarginChanged(ChangeEvent e) {
		super.columnMarginChanged(e);
		
		// columnMarginChanged is called continuously as the column width is changed by dragging.
		// Therefore, execute code below ONLY if we are not already aware of the column width having changed.
		if (!isColumnWidthChanged()) {
			// The condition below will NOT be true if the column width is being changed by code.
			if (getTableHeader().getResizingColumn() != null) // User must have dragged column and changed width
				setColumnWidthChanged(true);
		}
	}
	
    public boolean isColumnWidthChanged() {
        return columnWidthChanged;
    }

    public void setColumnWidthChanged(boolean b) {
        columnWidthChanged = b;
    }

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().compareTo("Copy") == 0)
			copyToClipBoard();
	}

	@Override
	public void paint(Graphics g) {
		var tableModel = getBrowserTableModel();
		
		if (tableModel == null) {
			super.paint(g);
			return;
		}
		
		var lock = tableModel.getLock();
		lock.readLock().lock();
		
		try {
			if (!tableModel.isDisposed())
				super.paint(g);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void handleEvent(AddedColumnViewEvent e) {
		var tableModel = getBrowserTableModel();
		
		if (tableModel == null)
			return;
		
		var tableView = tableModel.getTableView();

		if (e.getSource() != tableView)
			return;

		var columnModel = (BrowserTableColumnModel) getColumnModel();

		var col = e.getColumnView().getModel();
		tableModel.addColumn(col.getName());

		int colIndex = columnModel.getColumnCount(false);

		var name = col.getName();
		var view = tableModel.getTableView().getColumnView(name);
		boolean visible = view.getVisualProperty(COLUMN_VISIBLE);
		double gravity = view.getVisualProperty(COLUMN_GRAVITY);

		var tableColumn = new TableColumn(colIndex);
		tableColumn.setHeaderValue(name);
		tableColumn.setHeaderRenderer(new BrowserTableHeaderRenderer(serviceRegistrar));
		columnModel.addColumn(tableColumn, view.getSUID(), visible, gravity);

		columnModel.reorderColumnsToRespectGravity();
	}

	@Override
	public void handleEvent(AboutToRemoveColumnViewEvent e) {
		var tableModel = getBrowserTableModel();
		
		if (tableModel == null)
			return;
		
		var tableView = tableModel.getTableView();
		
		if (e.getSource() != tableView)
			return;

		tableModel.fireTableStructureChanged();

		var columnModel = (BrowserTableColumnModel) getColumnModel();
		var columnName = e.getColumnView().getModel().getName();
		boolean columnFound = false;
		int removedColIndex = -1;
		
		var attrNames = tableModel.getAllAttributeNames();
		
		for (int i = 0; i < attrNames.size(); ++i) {
			if (attrNames.get(i).equals(columnName)) {
				removedColIndex = i;
				columnModel.deleteColumn(columnModel.getColumn(convertColumnIndexToView(i)));
				columnFound = true;
			} else if (columnFound){ //need to push back the model indexes for all of the columns after this
				TableColumn nextCol = columnModel.getColumnByModelIndex(i); 
				nextCol.setModelIndex(i- 1);
			}
		}
		
		if (removedColIndex != -1) // remove the item after the loop is done
			tableModel.removeColumn(removedColIndex);
	}

	@Override
	public void handleEvent(ColumnNameChangedEvent e) {
		var tableModel = getBrowserTableModel();
		
		if (tableModel == null)
			return;
		
		var dataTable = tableModel.getDataTable();

		if (e.getSource() != dataTable)
			return;
		
		renameColumnName(e.getOldColumnName(), e.getNewColumnName());
		invokeOnEDT(() -> tableHeader.repaint());
	}
	
	@Override
	public void handleEvent(RowsSetEvent e) {
		if (isEditing())
			getCellEditor().stopCellEditing();
		
		if (ignoreRowSetEvents)
			return;
		
		var tableModel = getBrowserTableModel();
		
		if (tableModel == null)
			return;
		
		var dataTable = tableModel.getDataTable();

		if (e.getSource() != dataTable)
			return;

		if (tableModel.getViewMode() == SELECTED || tableModel.getViewMode() == AUTO) {
			tableModel.clearSelectedRows();
			boolean foundANonSelectedColumnName = false;

			for (var column : e.getColumns()) {
				if (!CyNetwork.SELECTED.equals(column)) {
					foundANonSelectedColumnName = true;
					break;
				}
			}

			if (!foundANonSelectedColumnName) {
				tableModel.fireTableDataChanged();
				return;
			}
		}

		var rows = e.getPayloadCollection();

		synchronized (this) {
			tableModel.fireTableDataChanged();

			if (tableModel.getViewMode() == ALL) {
				var tableManager = serviceRegistrar.getService(CyTableManager.class);
				
				if (!tableManager.getGlobalTables().contains(dataTable))
					bulkUpdate(rows);
			}
		}
	}
	
	public void resetRowHeight() {
		var tableModel = getBrowserTableModel();
		
		if (tableModel == null)
			return;
		
		var tableView = tableModel.getTableView();
		int h = tableView.getVisualProperty(TABLE_ROW_HEIGHT);
		
		if (h > 0) {
			setRowHeight(h);
		
			// Remember that the cell renderer might set a different height to each row
			// if COLUMN_TEXT_WRAPPED is true for any visible column
			resizeAndRepaint();
			fireRowHeightChange(0, h);
		}
	}

	public void fireRowHeightChange(int oldValue, int newValue) {
		// Notify any listeners that the row height may have changed, but let's use our 'rowHeightChanged' key,
		// because we don't want to interfere with any internal Swing listeners by using the standard 'rowHeight'.
		firePropertyChange("rowHeightChanged", oldValue, newValue);
	}

	// ==[ PRIVATE METHODS ]============================================================================================
	
	protected void init() {
		setAutoCreateColumnsFromModel(false);
		setAutoCreateRowSorter(true);
		setCellSelectionEnabled(true);
		setShowGrid(TABLE_GRID_VISIBLE.getDefault());
		setDefaultEditor(Object.class, multiLineCellEditor);
		getPopupMenu();
		setKeyStroke();
		setTransferHandler(new BrowserTableTransferHandler());
		
		var header = new JTableHeader() {
			@Override
			public void setDraggedColumn(TableColumn column) {
				// When a column is dragged, that column is usually selected, but the selection listener
				// we added to the column model does not receive a notification for that,
				// so we need to update the column selection (visual property) ourselves right after the dragging ends.
				boolean finished = draggedColumn != null && column == null;
				var colId = draggedColumn != null ? draggedColumn.getIdentifier() : null;
				
				super.setDraggedColumn(column);
					
				if (finished && getSelectedRowCount() > 0) {
					var idx = getColumnModel().getColumnIndex(colId);
					
					if (idx >= 0)
						setSelectedColumn(idx);
				}
			}
		};
		header.setOpaque(false);
		header.getColumnModel().setColumnSelectionAllowed(true);
		header.addMouseMotionListener(this);
		header.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				maybeShowHeaderPopup(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					maybeShowHeaderPopup(e);
				} else if (isColumnWidthChanged()) {
					// Reset the flag
					setColumnWidthChanged(false);
					
					// Update the COLUMN_WIDTH visual property value.
					// getColumnModel().getColumnIndexAtX(e.getX()) does NOT work, because the mouse may have been
					// released over another column, not the one being changed.
					// So let's make it simple and update the WIDTH values of all columns.
					var tableModel = getBrowserTableModel();
					
					if (tableModel == null)
						return;
					
					var columnModel = (BrowserTableColumnModel) getColumnModel();
					var tableView = tableModel.getTableView();

					for (int i = 0; i < getColumnCount(); i++) {
						var column = columnModel.getColumn(i);
						var newWidth = column.getWidth();
						
						if (newWidth > 0) {
							var name = tableModel.getColumnName(convertColumnIndexToModel(i));
							var view = tableView.getColumnView(name);
							var oldWidth = view.getVisualProperty(COLUMN_WIDTH);

							if (oldWidth == null || newWidth != oldWidth)
								view.setLockedValue(COLUMN_WIDTH, newWidth);
						}
					}
				}
			}
		});
		setTableHeader(header);
		
		setSelectionModel(new BrowserTableListSelectionModel());
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		addMouseListener(this);
		
		// Update the ROW_SELECTED Visual Property
		getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting() && !ignoreRowSelectionEvents) {
				selectFromTable();
				
				var tableModel = getBrowserTableModel();
				
				if (tableModel != null) {
					var tableView = tableModel.getTableView();
					var changed = false;
					
					for (int idx = e.getFirstIndex(); idx <= e.getLastIndex(); idx++) {
						// There can be ArrayIndexOutOfBoundsExceptions here when changing TABLE_VIEW_MODE
						if (idx >= getRowCount())
							continue;
						
						var cyRow = tableModel.getCyRow(convertRowIndexToModel(idx));
						var rowView = tableView.getRowView(cyRow);
						var selected = isRowSelected(idx);
						
						if (selected != rowView.getVisualProperty(ROW_SELECTED)) {
							rowView.setLockedValue(ROW_SELECTED, selected);
							changed = true;
						}
					}
					
					// Flush events here to prevent ROW_SELECTED events from being captured later when
					// the selection may have been changed again, which could cause infinite loops
					if (changed)
						serviceRegistrar.getService(CyEventHelper.class).flushPayloadEvents();
				}
			}
		});
	}
	
	private void setSelectedColumn(int idx) {
		ignoreColumnSelectionEvents = true;
		
		try {
			getColumnModel().getSelectionModel().clearSelection();
			addColumnSelectionInterval(idx, idx);
		} finally {
			ignoreColumnSelectionEvents = false;
		}
		
		// Update the COLUMN_SELECTED visual property for all columns
		setColumnSelectedVP(0, getColumnCount() - 1);
	}
	
	private void setColumnSelectedVP(int firstIndex, int lastIndex) {
		if (lastIndex < firstIndex)
			throw new IllegalArgumentException("'lastIndex' must not be greater than 'firstIndex'.");
		
		var tableModel = getBrowserTableModel();
		
		if (tableModel != null) {
			var tableView = tableModel.getTableView();
			var changed = false;
			
			for (int idx = firstIndex; idx <= lastIndex; idx++) {
				if (idx >= getColumnCount())
					continue;
				
				var cyColumn = tableModel.getCyColumn(convertColumnIndexToModel(idx));
				
				if (cyColumn == null)
					return; // This column might have been deleted!
				
				var columnView = tableView.getColumnView(cyColumn);
				var selected = isColumnSelected(idx);
				
				if (selected != columnView.getVisualProperty(COLUMN_SELECTED)) {
					columnView.setLockedValue(COLUMN_SELECTED, selected);
					changed = true;
				}
			}
			
			// Flush events here to prevent COLUMN_SELECTED events from being captured later when
			// the selection may have been changed again, which could cause infinite loops
			if (changed)
				serviceRegistrar.getService(CyEventHelper.class).flushPayloadEvents();
		}
	}
	
	/**
	 * Applies the COLUMN_WIDTH visual property value to the column or updates the COLUMN_WIDTH visual property
	 * with the current column width.
	 * @param idx the column index
	 */
	private void syncColumnWidth(int idx) {
		if (idx < 0)
			return;
		
		var tableModel = getBrowserTableModel();
		
		if (tableModel == null)
			return;
		
		var columnModel = (BrowserTableColumnModel) getColumnModel();
		var column = columnModel.getColumn(idx);
		var tableView = tableModel.getTableView();
		var name = tableModel.getColumnName(convertColumnIndexToModel(idx));
		var columnView = tableView.getColumnView(name);
		
		if (columnView.isSet(COLUMN_WIDTH)) {
			// Just apply the value from the visual property
			var oldWidth = column.getWidth();
			var newWidth = columnView.getVisualProperty(COLUMN_WIDTH);

			if (newWidth != null && newWidth > 0 && newWidth != oldWidth) {
				column.setPreferredWidth(newWidth);
				column.setWidth(newWidth);
			}
		} else {
			// Calculate the best width to fit the column name and icons and update the visual property
			ColumnResizer.adjustColumnPreferredWidth(this, idx, false);
			columnView.setLockedValue(COLUMN_WIDTH, column.getWidth());
		}
	}
	
	private void setKeyStroke() {
		int modifiers = isMac() ? ActionEvent.META_MASK : ActionEvent.CTRL_MASK;
		var copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, modifiers, false);
		// Identifying the copy KeyStroke user can modify this to copy on some other Key combination.
		registerKeyboardAction(this, "Copy", copy, JComponent.WHEN_FOCUSED);
	}
	
	private void showCellMenu(Class<?> type, List<?> listItems, String borderTitle, MouseEvent e) {
		var cellMenu = new JPopupMenu();
		var popupBorder = BorderFactory.createTitledBorder(null, borderTitle,
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, BORDER_FONT);
		cellMenu.setBorder(popupBorder);
		
		JMenu curItem = null;
		String dispName = null;

		for (var item : listItems) {
			dispName = item.toString();

			if (dispName.length() > 60)
				dispName = dispName.substring(0, 59) + " ...";

			curItem = new JMenu(dispName);
			curItem.add(getPopupMenu());

			var copyAll = new JMenuItem("Copy all entries");
			copyAll.addActionListener(evt -> {
				var builder = new StringBuilder();
				
				for (var oneEntry : listItems)
					builder.append(oneEntry.toString() + CELL_BREAK);

				var selection = new StringSelection(builder.toString());
				var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(selection, selection);
			});
			curItem.add(copyAll);

			var copy = new JMenuItem("Copy this entry");
			copy.addActionListener(evt -> {
				var selection = new StringSelection(item.toString());
				var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(selection, selection);
			});

			curItem.add(copy);
			
			if (dispName != null && (dispName.startsWith("http:") || dispName.startsWith("https:")))
				curItem.add(popupMenuHelper.getOpenLinkMenu(dispName));
			
			cellMenu.add(curItem);
		}

		cellMenu.show(e.getComponent(), e.getX(), e.getY());
	}
	
	private void maybeShowHeaderPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			int column = getColumnModel().getColumnIndexAtX(e.getX());
			var tableModel = getBrowserTableModel();

			// Make sure the column we're clicking on actually exists!
			if (tableModel == null || column >= tableModel.getColumnCount() || column < 0)
				return;

			// Ignore clicks on the ID column:
			if (tableModel.isPrimaryKey(convertColumnIndexToModel(column)))
				return;

			var networkTableManager = serviceRegistrar.getService(CyNetworkTableManager.class);
			var tableType = networkTableManager.getTableType(tableModel.getDataTable());
			
			var cyColumn = tableModel.getCyColumn(convertColumnIndexToModel(column));
			popupMenuHelper.createColumnHeaderMenu(cyColumn, tableType, BrowserTable.this, e.getX(), e.getY());
		}
	}
	
	private void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			if (isWindows())
				selectFocusedCell(e);
			
			// Show context menu
			int viewColumn = getColumnModel().getColumnIndexAtX(e.getX());
			int viewRow = rowAtPoint(e.getPoint());
			int modelColumn = convertColumnIndexToModel(viewColumn);
			int modelRow = convertRowIndexToModel(viewRow);
			
			var tableModel = getBrowserTableModel();
			
			// Bail out if we're at the ID column:
			if (tableModel == null || tableModel.isPrimaryKey(modelColumn))
				return;

			// Make sure the column and row we're clicking on actually exists!
			if (modelColumn >= tableModel.getColumnCount() || modelRow >= tableModel.getRowCount())
				return;
			
			var cyColumn = tableModel.getCyColumn(modelColumn);
			var primaryKeyValue = ((ValidatedObjectAndEditString) tableModel.getValueAt(modelRow,
					tableModel.getDataTable().getPrimaryKey().getName())).getValidatedObject();
			
			var networkTableManager = serviceRegistrar.getService(CyNetworkTableManager.class);
			var tableType = networkTableManager.getTableType(tableModel.getDataTable());
			
			popupMenuHelper.createTableCellMenu(cyColumn, primaryKeyValue, tableType, this, e.getX(), e.getY(), this);
		}
	}
	
	private void selectFocusedCell(MouseEvent e) {
		int row = rowAtPoint(e.getPoint());
		int column = getColumnModel().getColumnIndexAtX(e.getX());
		
		int[] selectedRows = this.getSelectedRows();
		int binarySearch = Arrays.binarySearch(selectedRows, row);
		
		// Select clicked cell, if not selected yet
		if (binarySearch < 0) {
			// Clicked row not selected: Select only the right-clicked row/cell
			this.changeSelection(row, column, false, false);
		} else {
			// Row is already selected: Just guarantee the last clicked cell is highlighted properly
			((BrowserTableListSelectionModel) selectionModel).setLastSelectedRow(row);
			setColumnSelectionInterval(column, column);
			
			var tableModel = (BrowserTableModel) this.getModel();
			tableModel.fireTableRowsUpdated(selectedRows[0], selectedRows[selectedRows.length-1]);
		}
	}
	
	private String copyToClipBoard() {
		var sbf = new StringBuffer();

		/*
		 * Check to ensure we have selected only a contiguous block of cells.
		 */
		int numcols = this.getSelectedColumnCount();
		int numrows = this.getSelectedRowCount();

		int[] rowsselected = this.getSelectedRows();
		int[] colsselected = this.getSelectedColumns();

		// Return if no cell is selected.
		if (numcols == 0 && numrows == 0)
			return null;

		if (!((numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0] && numrows == rowsselected.length)
				&& (numcols - 1 == colsselected[colsselected.length - 1] - colsselected[0] 
						&& numcols == colsselected.length))) {
			var rootFrame = (JFrame) SwingUtilities.getRoot(this);
			JOptionPane.showMessageDialog(rootFrame, "Invalid Copy Selection", "Invalid Copy Selection",
					JOptionPane.ERROR_MESSAGE);

			return null;
		}

		for (int i = 0; i < numrows; i++) {
			for (int j = 0; j < numcols; j++) {
				var cellValue = this.getValueAt(rowsselected[i], colsselected[j]);
				var cellText = cellValue instanceof ValidatedObjectAndEditString ?
						((ValidatedObjectAndEditString) cellValue).getEditString() : null;
				
				sbf.append(cellText != null ? escape(cellText) : "");

				if (j < numcols - 1)
					sbf.append(CELL_BREAK);
			}

			sbf.append(LINE_BREAK);
		}
		
		var selection = new StringSelection(sbf.toString());
		var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(selection, selection);

		return sbf.toString();
	}
	
	private String escape(String cellValue) {
		return cellValue.replace(LINE_BREAK, " ").replace(CELL_BREAK, " ");
	}
	
	private void renameColumnName(String oldName, String newName) {
		var columnModel = (BrowserTableColumnModel) getColumnModel();
		var model = getBrowserTableModel();
		
		int index = model.mapColumnNameToColumnIndex(oldName);
		
		if (index >= 0){
			model.setColumnName(index, newName);
			columnModel.getColumn(convertColumnIndexToView(index)).setHeaderValue(newName);
			return;
		}
	
		throw new IllegalStateException("The specified column " + oldName +" does not exist in the model.");
	}

	/**
	 * Select rows in the table when something selected in the network view.
	 * @param rows
	 */
	private void bulkUpdate(Collection<RowSetRecord> rows) {
		var suidSelected = new HashSet<Long>();
		var suidUnselected = new HashSet<Long>();

		for (var rowSetRecord : rows) {
			if (rowSetRecord.getColumn().equals(CyNetwork.SELECTED)) {
				if (((Boolean) rowSetRecord.getValue()) == true)
					suidSelected.add(rowSetRecord.getRow().get(CyIdentifiable.SUID, Long.class));
				else
					suidUnselected.add(rowSetRecord.getRow().get(CyIdentifiable.SUID, Long.class));
			}
		}

		changeRowSelection(suidSelected, suidUnselected);
	}

	public void changeRowSelection(Set<Long> suidSelected, Set<Long> suidUnselected) {
		var tableModel = getBrowserTableModel();
		
		if (tableModel == null)
			return;
		
		var dataTable = tableModel.getDataTable();
		var pKeyName = dataTable.getPrimaryKey().getName();
		int rowCount = getRowCount();
		
		try {
			ignoreRowSelectionEvents = true;
			
			for (int i = 0; i < rowCount; i++) {
				// Getting the row from data table solves the problem with hidden or moved SUID column.
				// However, since the rows might be sorted we need to convert the index to model
				int modelRow = convertRowIndexToModel(i);
				var tableKey = (ValidatedObjectAndEditString) tableModel.getValueAt(modelRow, pKeyName);
				Long pk = null;
				
				try {
					// TODO: Temp fix: is it a requirement that all CyTables have a Long SUID column as PK?
					pk = Long.parseLong(tableKey.getEditString());
				} catch (NumberFormatException nfe) {
					logger.error("Error parsing long from table " + getName(), nfe);
				}
				
				if (pk != null) {
					if (suidSelected.contains(pk))
						addRowSelectionInterval(i, i);
					else if (suidUnselected.contains(pk))
						removeRowSelectionInterval(i, i);
				}
			}
		} finally {
			ignoreRowSelectionEvents = false;
		}
	}

	private void selectFromTable() {
		var model = getModel();
		
		if (model instanceof BrowserTableModel == false)
			return;

		var btModel = (BrowserTableModel) model;

		if (btModel.getViewMode() != BrowserTableModel.ViewMode.ALL)
			return;

		var table = btModel.getDataTable();
		var pKey = table.getPrimaryKey();
		var pKeyName = pKey.getName();
		
		if (pKey.getType() != Long.class || table.getColumn(CyNetwork.SELECTED) == null)
			return; // This is not a standard Node or Edge table!

		var rowsSelected = getSelectedRows();
		
		if (rowsSelected.length == 0)
			return;

		int selectedRowCount = getSelectedRowCount();
		var targetRows = new HashSet<CyRow>();
		
		for (int i = 0; i < selectedRowCount; i++) {
			// getting the row from data table solves the problem with hidden or
			// moved SUID column. However, since the rows might be sorted we
			// need to convert the index to model
			var selected = (ValidatedObjectAndEditString) btModel.getValueAt(
					convertRowIndexToModel(rowsSelected[i]), pKeyName);
			targetRows.add(btModel.getCyRow(selected.getValidatedObject()));
		}

		// Clear selection for non-global table
		var tableManager = serviceRegistrar.getService(CyTableManager.class);
		
		if (tableManager.getGlobalTables().contains(table) == false) {
			var allRows = table.getAllRows();
			
			try {
				ignoreRowSetEvents = true;
				
				for (var row : allRows) {
					var val = row.get(CyNetwork.SELECTED, Boolean.class);
					
					if (targetRows.contains(row)) {
						row.set(CyNetwork.SELECTED, true);
						continue;
					}
	
					if (val != null && (val == true))
						row.set(CyNetwork.SELECTED, false);
				}
				
				var applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
				var curView = applicationManager.getCurrentNetworkView();
				
				if (curView != null) {
					serviceRegistrar.getService(CyEventHelper.class).flushPayloadEvents();
					curView.updateView();
				}
			} finally {
				ignoreRowSetEvents = false;
			}
			
			repaint();
		}
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class ProxyMouseListener extends MouseAdapter {
		
		private final MouseListener listener;
		
		public ProxyMouseListener(MouseListener listener) {
			this.listener = listener;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			// In order to prevent selected rows from being deselected when the user
			// CONTROL-clicks one of those rows on Mac.
			if (listener instanceof BrowserTable || !e.isPopupTrigger())
				listener.mouseClicked(e);
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			listener.mouseEntered(e);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			listener.mouseExited(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// In order to prevent selected rows from being deselected when the user
			// CONTROL-clicks one of those rows on Mac.
			if (listener instanceof BrowserTable || !e.isPopupTrigger())
				listener.mousePressed(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			listener.mouseReleased(e);
		}
	}
	
	private class BrowserTableListSelectionModel extends DefaultListSelectionModel {
		
		private int lastSelectedRow = -1;
		
		int getLastSelectedRow() {
			return lastSelectedRow;
		}
		
		public void setLastSelectedRow(int lastSelectedRow) {
			this.lastSelectedRow = lastSelectedRow;
		}
	}
	
	private class CellEditorRemover implements PropertyChangeListener {
		
		private final KeyboardFocusManager focusManager;
		private CyRow cyRow;
		private String columnName;

		public CellEditorRemover(KeyboardFocusManager fm) {
			this.focusManager = fm;
		}

		@Override
		public void propertyChange(PropertyChangeEvent ev) {
			if (!isEditing()) {
				return;
			}

			Component c = focusManager.getPermanentFocusOwner();

			while (c != null) {
				if (c == BrowserTable.this) {
					// focus remains inside the table
					return;
				} else if (c instanceof Window) {
					if (c == SwingUtilities.getRoot(BrowserTable.this)) {

						try {
							getCellEditor().stopCellEditing();
						} catch (Exception e) {
							getCellEditor().cancelCellEditing();
							// Update the cell data based on the remembered
							// value
							updateAttributeAfterCellLostFocus();
						}
					}

					break;
				}

				c = c.getParent();
			}
		}

		/**
		 *  Cell data passed from previous TableModel, because tableModel will disappear if
		 *  user click on open space on canvas, so we have to remember it before it is gone.
		 * @param row
		 * @param columnName
		 */
		public void setCellData(CyRow row, String columnName) {
			this.cyRow = row;
			this.columnName = columnName;
		}

		private void updateAttributeAfterCellLostFocus() {
			var parsedData = TableBrowserUtil.parseCellInput(cyRow.getTable(), columnName,
					MultiLineTableCellEditor.lastValueUserEntered);

			if (parsedData.get(0) != null) {
				cyRow.set(columnName, MultiLineTableCellEditor.lastValueUserEntered);
			} else {
				// Error
				// discard the change
			}
		}
	}
	
	private static class BrowserTableTransferHandler extends TransferHandler {
		
		@Override
		protected Transferable createTransferable(JComponent source) {
			// Encode cell data in Excel format so we can copy/paste list attributes as multi-line cells.
			var builder = new StringBuilder();
			var table = (BrowserTable) source;
			
			for (int rowIndex : table.getSelectedRows()) {
				boolean firstColumn = true;
				
				for (int columnIndex : table.getSelectedColumns()) {
					if (!firstColumn) {
						builder.append(CELL_BREAK);
					} else {
						firstColumn = false;
					}
					
					var object = table.getValueAt(rowIndex, columnIndex);
					
					if (object instanceof ValidatedObjectAndEditString) {
						var raw = (ValidatedObjectAndEditString) object;
						var validatedObject = raw.getValidatedObject();
						
						if (validatedObject instanceof Collection) {
							builder.append("\"");
							boolean firstRow = true;
							
							for (var member : (Collection<?>) validatedObject) {
								if (!firstRow)
									builder.append("\r");
								else
									firstRow = false;
								
								builder.append(member.toString().replaceAll("\"", "\"\""));
							}
							
							builder.append("\"");
						} else {
							builder.append(validatedObject.toString());
						}
					} else {
						if (object != null)
							builder.append(object.toString());
					}
				}
				builder.append(LINE_BREAK);
			}
			
			return new StringSelection(builder.toString());
		}
		
		@Override
		public int getSourceActions(JComponent c) {
			return TransferHandler.COPY;
		}
	}
}
