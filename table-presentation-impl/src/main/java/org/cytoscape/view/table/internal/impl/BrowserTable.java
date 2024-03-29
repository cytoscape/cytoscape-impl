package org.cytoscape.view.table.internal.impl;

import static org.cytoscape.util.swing.LookAndFeelUtil.getSmallFontSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isMac;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.*;
import static org.cytoscape.view.table.internal.impl.BrowserTableModel.ViewMode.ALL;
import static org.cytoscape.view.table.internal.impl.BrowserTableModel.ViewMode.AUTO;
import static org.cytoscape.view.table.internal.impl.BrowserTableModel.ViewMode.SELECTED;
import static org.cytoscape.view.table.internal.util.TableBrowserUtil.CELL_BREAK;
import static org.cytoscape.view.table.internal.util.TableBrowserUtil.LINE_BREAK;
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
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
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
import org.cytoscape.view.model.events.TableViewChangedEvent;
import org.cytoscape.view.model.events.ViewChangeRecord;
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

	private int tempFocusedRow = -1;
	private int tempFocusedColumn = -1;

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
    if (row >= getRowCount() || row < 0)
      return false;

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

			fireToolbarUpdateEvent();
			
			return true;
		}

		return false;
	}
	
	// This event will get picked up by the ToolBarEnableUpdater.
	@SuppressWarnings("unchecked")
	private void fireToolbarUpdateEvent() {
		var tableView  = getBrowserTableModel().getTableView();
		var eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		var payload = new ViewChangeRecord<>(tableView, CELL, null);
		eventHelper.addEventPayload(tableView, payload, TableViewChangedEvent.class);
		eventHelper.flushPayloadEvents(tableView);
	}

	@Override
	public void editingCanceled(ChangeEvent e) {
		super.editingCanceled(e);
		fireToolbarUpdateEvent();
	}
	
	@Override
	public void editingStopped(ChangeEvent e) {
		super.editingStopped(e);
		fireToolbarUpdateEvent();
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
		maybeShowPopup(e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e) && (getSelectedRows().length != 0)) {
			int viewColumn = getColumnModel().getColumnIndexAtX(e.getX());
			int viewRow = rowAtPoint(e.getPoint());
			int modelColumn = convertColumnIndexToModel(viewColumn);
			int modelRow = convertRowIndexToModel(viewRow);
			
			var tableModel = (BrowserTableModel) getModel();
			
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
				columnWidthMap.put(getColumnName(index), colWidth);
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
			copyToClipboard();
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
	
	public int getTempFocusedRow() {
		return tempFocusedRow;
	}
	
	public int getTempFocusedColumn() {
		return tempFocusedColumn;
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
				// When a column is dragged or right-clicked, we want to have all cells of that column selected
				// (this is usually the expected behavior and it prevents a few issues, such as an exception when trying
				// to copy the selected cells to the clipboard after creating a gap on the the selection range by
				// moving a selected column out of that initial selection range)
				boolean finished = draggedColumn != null && column == null;
				var colId = draggedColumn != null ? draggedColumn.getIdentifier() : null;
				
				super.setDraggedColumn(column);
					
				if (finished) {
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
	
	/**
	 * Deselects the other columns and selects only the passed column index (all rows).
	 */
	private void setSelectedColumn(int idx) {
		ignoreColumnSelectionEvents = true;
		
		try {
			getColumnModel().getSelectionModel().clearSelection();
			addColumnSelectionInterval(idx, idx);
			
			if (getRowCount() > 0)
				addRowSelectionInterval(0, getRowCount() - 1);
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
			
			if (column >= 0) // Make sure the whole column is selected
				setSelectedColumn(column);
			
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
			popupMenuHelper.createColumnHeaderMenu(cyColumn, tableType, BrowserTable.this, e.getX(), e.getY(), this);
		}
	}
	
	private void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
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
			
			var menu = popupMenuHelper.createTableCellMenu(cyColumn, primaryKeyValue, tableType, this, e.getX(), e.getY(), this);
			menu.addPopupMenuListener(new PopupMenuListener() {
				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
					resetTempFocusedCell();
				}
				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
					resetTempFocusedCell();
				}
				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					// Ignore...
				}
				private void resetTempFocusedCell() {
					tempFocusedRow = -1;
					tempFocusedColumn = -1;
					repaint();
				}
			});
		}
	}
	
	private void selectFocusedCell(MouseEvent e) {
		int row = tempFocusedRow = rowAtPoint(e.getPoint());
		int column = tempFocusedColumn = getColumnModel().getColumnIndexAtX(e.getX());
		
		int[] selectedRows = getSelectedRows();
		boolean isRowselected = Arrays.binarySearch(selectedRows, row) >= 0;
		boolean isColumnSelected = Arrays.binarySearch(getColumnModel().getSelectedColumns(), column) >= 0;
		
		if (isRowselected && isColumnSelected) // Just repaint to highlight the temporarily focused cell
			repaint();
		else // Clicked row not selected: Select only the right-clicked row/cell
			changeSelection(row, column, false, false);
	}
	
	private void copyToClipboard() {
		var data = createCopyString(this);
		var selection = new StringSelection(data);
		var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(selection, selection);
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
	
	private static String createCopyString(BrowserTable table) {
		int[] rows = table.getSelectedRows();
		int[] columns = table.getSelectedColumns();
		int numRows = rows.length;
		int numCols = columns.length;

		if (numCols == 0 || numRows == 0)
			return null;

		var sb = new StringBuffer();
		boolean firstRow = true;
		
		for (int r : rows) {
			if (!firstRow)
				sb.append(LINE_BREAK);
			else
				firstRow = false;
			
			boolean firstColumn = true;

			for (int c : columns) {
				if (!firstColumn)
					sb.append(CELL_BREAK);
				else
					firstColumn = false;

				var object = table.getValueAt(r, c);

				if (object instanceof ValidatedObjectAndEditString) {
					var raw = (ValidatedObjectAndEditString) object;
					var s = TableBrowserUtil.createCopyString(raw);
					sb.append(s);
				} else {
					if (object != null)
						sb.append(object.toString());
				}
			}
		}
		
		return sb.toString();
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
			var data = createCopyString((BrowserTable) source);
			
			return new StringSelection(data);
		}
		
		@Override
		public int getSourceActions(JComponent c) {
			return TransferHandler.COPY;
		}
	}
}
