package org.cytoscape.browser.internal.view;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.browser.internal.util.TableBrowserUtil;
import org.cytoscape.browser.internal.util.ValidatedObjectAndEditString;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.ColumnNameChangedEvent;
import org.cytoscape.model.events.ColumnNameChangedListener;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrowserTable extends JTable implements MouseListener, ActionListener, MouseMotionListener,
													 ColumnCreatedListener, ColumnDeletedListener,
													 ColumnNameChangedListener, RowsSetListener {

	private static final long serialVersionUID = 4415856756184765301L;

	private static final Logger logger = LoggerFactory.getLogger(BrowserTable.class);

	private static final Font BORDER_FONT = new Font("Sans-serif", Font.BOLD, 12);

	private static final TableCellRenderer cellRenderer = new BrowserTableCellRenderer();
	private static final String MAC_OS_ID = "mac";
	private static final String WIN_OS_ID = "Windows";

	private Clipboard systemClipboard;
	private CellEditorRemover editorRemover = null;
	private final HashMap<String, Integer> columnWidthMap = new HashMap<String, Integer>();

	// For right-click menu
	private JPopupMenu rightClickPopupMenu;
	private JPopupMenu rightClickHeaderPopupMenu;
	private JMenuItem openFormulaBuilderMenuItem = null;

	private final EquationCompiler compiler;
	private final PopupMenuHelper popupMenuHelper;
	private final CyServiceRegistrar serviceRegistrar;
	
	private boolean ignoreRowSelectionEvents;
	private boolean ignoreRowSetEvents;

	private JPopupMenu cellMenu;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public BrowserTable(final EquationCompiler compiler, final PopupMenuHelper popupMenuHelper,
			final CyServiceRegistrar serviceRegistrar) {
		this.compiler = compiler;
		this.popupMenuHelper = popupMenuHelper;
		this.serviceRegistrar = serviceRegistrar;
		
		init();
		setAutoCreateColumnsFromModel(false);
		setAutoCreateRowSorter(true);
		setCellSelectionEnabled(true);
		setDefaultEditor(Object.class, new MultiLineTableCellEditor());
		getPopupMenu();
		getHeaderPopupMenu();
		setKeyStroke();
		setTransferHandler(new BrowserTableTransferHandler());
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public void setModel(final TableModel dataModel) {
		super.setModel(dataModel);
		
 		BrowserTableColumnModel columnModel = new BrowserTableColumnModel();
		setColumnModel(columnModel);

		if (dataModel instanceof BrowserTableModel) {
			BrowserTableModel model = (BrowserTableModel) dataModel;
			
			for (int i = 0; i < model.getColumnCount(); i++) {
				TableColumn tableColumn = new TableColumn(i);
				tableColumn.setHeaderValue(model.getColumnName(i));
				columnModel.addColumn(tableColumn);
			}
		}
	}
	
	@Override
	public synchronized void addMouseListener(final MouseListener listener) {
		// Hack to prevent selected rows from being deselected when the user
		// CONTROL-clicks one of those rows on Mac (popup trigger).
		super.addMouseListener(new ProxyMouseListener(listener));
	}
	
	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		return cellRenderer;
	}

	@Override
	public boolean isCellEditable(final int row, final int column) {
		return this.getModel().isCellEditable(convertRowIndexToModel(row), convertColumnIndexToModel(column));
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

		TableCellEditor editor = getCellEditor(row, column);

		// remember the table row, because tableModel will disappear if
		// user click on open space on canvas, so we have to remember it before it is gone
		BrowserTableModel model = (BrowserTableModel) this.getModel();
		CyRow cyRow = model.getCyRow(convertRowIndexToModel(row));
		String columnName = model.getColumnName(convertColumnIndexToModel(column));
		editorRemover.setCellData(cyRow, columnName);

		if ((editor != null) && editor.isCellEditable(e)) {
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

			Rectangle cellRect = getCellRect(row, column, false);

			if (editor instanceof MultiLineTableCellEditor) {
				Dimension prefSize = editorComp.getPreferredSize();
				((JComponent) editorComp).putClientProperty(MultiLineTableCellEditor.UPDATE_BOUNDS, Boolean.TRUE);
				editorComp.setBounds(cellRect.x, cellRect.y, Math.max(cellRect.width, prefSize.width),
						Math.max(cellRect.height, prefSize.height));
				((JComponent) editorComp).putClientProperty(MultiLineTableCellEditor.UPDATE_BOUNDS, Boolean.FALSE);
			} else {
				editorComp.setBounds(cellRect);
			}

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
	
	public void showListContents(int modelRow, int modelColumn, MouseEvent e) {
		final BrowserTableModel model = (BrowserTableModel) getModel();
		final Class<?> columnType = model.getColumn(modelColumn).getType();

		if (columnType == List.class) {
			final ValidatedObjectAndEditString value = (ValidatedObjectAndEditString) model.getValueAt(modelRow, modelColumn);

			if (value != null) {
				final List<?> list = (List<?>) value.getValidatedObject();
				
				if (list != null && !list.isEmpty()) {
					cellMenu = new JPopupMenu();
					getCellContentView(List.class, list, "List Contains:", e);
				}
			}
		}
	}

	/**
	 * This method initializes rightClickPopupMenu
	 * 
	 * @return the inilialised pop-up menu
	 */
	public JPopupMenu getPopupMenu() {
		if (rightClickPopupMenu != null)
			return rightClickPopupMenu;

		rightClickPopupMenu = new JPopupMenu();
		openFormulaBuilderMenuItem = new JMenuItem("Open Formula Builder");

		final JTable table = this;
		
		openFormulaBuilderMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final int cellRow = table.getSelectedRow();
				final int cellColumn = table.getSelectedColumn();
				final BrowserTableModel tableModel = (BrowserTableModel) getModel();
				final JFrame rootFrame = (JFrame) SwingUtilities.getRoot(table);
				
				if (cellRow == -1 || cellColumn == -1 || !tableModel.isCellEditable(cellRow, cellColumn)) {
					JOptionPane.showMessageDialog(rootFrame, "Can't enter a formula w/o a selected cell.",
							"Information", JOptionPane.INFORMATION_MESSAGE);
				} else {
					final String columnName = tableModel.getColumnName(cellColumn);
					FormulaBuilderDialog formulaBuilderDialog = new FormulaBuilderDialog(compiler, BrowserTable.this,
							rootFrame, columnName);
					formulaBuilderDialog.setLocationRelativeTo(rootFrame);
					formulaBuilderDialog.setVisible(true);
				}
			}
		});

		return rightClickPopupMenu;
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
	public void mouseClicked(final MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e) && (getSelectedRows().length != 0)) {
			final int viewColumn = getColumnModel().getColumnIndexAtX(e.getX());
			final int viewRow = e.getY() / getRowHeight();
			final int modelColumn = convertColumnIndexToModel(viewColumn);
			final int modelRow = convertRowIndexToModel(viewRow);
			
			final BrowserTableModel tableModel = (BrowserTableModel) this.getModel();
			
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
	public void mouseEntered(final MouseEvent e) {
	}

	@Override
	public void mouseExited(final MouseEvent e) {
	}
	
	@Override
	public void mouseDragged(final MouseEvent e) {
		// save the column width, if user adjust column width manually
		if (e.getSource() instanceof JTableHeader) {
			final int index = getColumnModel().getColumnIndexAtX(e.getX());
			
			if (index != -1) {
				int colWidth = getColumnModel().getColumn(index).getWidth();
				this.columnWidthMap.put(this.getColumnName(index), new Integer(colWidth));
			}
		}
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		if (event.getActionCommand().compareTo("Copy") == 0)
			copyToClipBoard();
	}

	@Override
	public void paint(Graphics graphics) {
		BrowserTableModel model = (BrowserTableModel) getModel();
		ReadWriteLock lock = model.getLock();
		lock.readLock().lock();
		try {
			if (!model.isDisposed()) {
				super.paint(graphics);
			}
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public void setVisibleAttributeNames(Collection<String> visibleAttributes) {
		BrowserTableModel model = (BrowserTableModel) getModel();
		BrowserTableColumnModel columnModel = (BrowserTableColumnModel) getColumnModel();
		
		for (final String name : model.getAllAttributeNames()) {
			int col = model.mapColumnNameToColumnIndex(name);
			TableColumn column = columnModel.getColumnByModelIndex(col);
			columnModel.setColumnVisible(column, visibleAttributes.contains(name));
		}
		
		// Don't fire this, it will reset all the columns based on model
		// fireTableStructureChanged();
	}

	public List<String> getVisibleAttributeNames() {
		BrowserTableModel model = (BrowserTableModel) getModel();
		final List<String> visibleAttrNames = new ArrayList<String>();
		
		for (final String name : model.getAllAttributeNames()) {
			if (isColumnVisible(name))
				visibleAttrNames.add(name);
		}
		
		return visibleAttrNames;
	}

	public boolean isColumnVisible(String name) {
		BrowserTableModel model = (BrowserTableModel) getModel();
		BrowserTableColumnModel columnModel = (BrowserTableColumnModel) getColumnModel();
		TableColumn column = columnModel.getColumnByModelIndex(model.mapColumnNameToColumnIndex(name));
		
		return columnModel.isColumnVisible(column);
	}

	@Override
	public void handleEvent(final ColumnCreatedEvent e) {
		BrowserTableModel model = (BrowserTableModel) getModel();
		CyTable dataTable = model.getDataTable();
		
		if (e.getSource() != dataTable)
			return;

		model.fireTableStructureChanged();
		
		BrowserTableColumnModel columnModel = (BrowserTableColumnModel) getColumnModel();

		model.addColumn(e.getColumnName());
		
		int colIndex = columnModel.getColumnCount(false);
		TableColumn newCol = new TableColumn(colIndex);
		newCol.setHeaderValue(e.getColumnName());
		addColumn(newCol);
	}

	@Override
	public void handleEvent(final ColumnDeletedEvent e) {
		BrowserTableModel model = (BrowserTableModel) getModel();
		CyTable dataTable = model.getDataTable();
		
		if (e.getSource() != dataTable)
			return;

		model.fireTableStructureChanged();

		BrowserTableColumnModel columnModel = (BrowserTableColumnModel) getColumnModel();
		final String columnName = e.getColumnName();
		boolean columnFound = false;
		int removedColIndex = -1;
		
		List<String> attrNames = model.getAllAttributeNames();
		
		for (int i = 0; i < attrNames.size(); ++i) {
			if (attrNames.get(i).equals(columnName)) {
				removedColIndex = i;
				columnModel.removeColumn (columnModel.getColumn(convertColumnIndexToView(i)));
				columnFound = true;
			} else if (columnFound){ //need to push back the model indexes for all of the columns after this
				
				TableColumn nextCol = columnModel.getColumnByModelIndex(i); 
				nextCol.setModelIndex(i- 1);
			}
		}
		
		if (removedColIndex != -1){ //remove the item after the loop is done
			model.removeColumn(removedColIndex);
		}
	}

	@Override
	public void handleEvent(final ColumnNameChangedEvent e) {
		BrowserTableModel model = (BrowserTableModel) getModel();
		CyTable dataTable = model.getDataTable();

		if (e.getSource() != dataTable)
			return;
		
		renameColumnName(e.getOldColumnName(), e.getNewColumnName());
		if (SwingUtilities.isEventDispatchThread()) {
			tableHeader.repaint();
		} else {
			SwingUtilities.invokeLater (new Runnable () {
				public void run () {
					tableHeader.repaint();
				}
			});
		}
	}
	
	@Override
	public void handleEvent(final RowsSetEvent e) {
		if (ignoreRowSetEvents)
			return;
		
		final BrowserTableModel model = (BrowserTableModel) getModel();
		final CyTable dataTable = model.getDataTable();

		if (e.getSource() != dataTable)
			return;		

		if (model.getViewMode() == BrowserTableModel.ViewMode.SELECTED
				|| model.getViewMode() == BrowserTableModel.ViewMode.AUTO) {
			model.clearSelectedRows();
			boolean foundANonSelectedColumnName = false;
			
			for (final RowSetRecord rowSet : e.getPayloadCollection()) {
				if (!rowSet.getColumn().equals(CyNetwork.SELECTED)) {
					foundANonSelectedColumnName = true;
					break;
				}
			}
			
			if (!foundANonSelectedColumnName) {
				model.fireTableDataChanged();
				return;
			}
		}

		final Collection<RowSetRecord> rows = e.getPayloadCollection();

		synchronized (this) {
			if (model.getViewMode() == BrowserTableModel.ViewMode.SELECTED
					|| model.getViewMode() == BrowserTableModel.ViewMode.AUTO) {
				model.fireTableDataChanged();
			} else {
				final CyTableManager tableManager = serviceRegistrar.getService(CyTableManager.class);
				
				if (!tableManager.getGlobalTables().contains(dataTable))
					bulkUpdate(rows);
			}
		}
	}

	// ==[ PRIVATE METHODS ]============================================================================================
	
	protected void init() {
		final JTableHeader header = getTableHeader();
		header.setOpaque(false);
		header.setDefaultRenderer(new CustomHeaderRenderer());
		header.getColumnModel().setColumnSelectionAllowed(true);
		header.addMouseMotionListener(this);
		header.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				maybeShowHeaderPopup(e);
			}
			@Override
			public void mouseReleased(final MouseEvent e) {
				maybeShowHeaderPopup(e);
			}
		});

		setSelectionModel(new BrowserTableListSelectionModel());
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		addMouseListener(this);
		
		getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(final ListSelectionEvent e) {
				if (!e.getValueIsAdjusting() && !ignoreRowSelectionEvents)
					selectFromTable();
			}
		});
	}
	
	private void setKeyStroke() {
		final KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
		// Identifying the copy KeyStroke user can modify this to copy on some other Key combination.
		this.registerKeyboardAction(this, "Copy", copy, JComponent.WHEN_FOCUSED);
		systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	}
	
	private JPopupMenu getHeaderPopupMenu() {
		if (rightClickHeaderPopupMenu != null)
			return rightClickHeaderPopupMenu;

		rightClickHeaderPopupMenu = new JPopupMenu();

		return rightClickHeaderPopupMenu;
	}
	
	private void getCellContentView(final Class<?> type, final List<?> listItems, final String borderTitle,
			final MouseEvent e) {

		JMenu curItem = null;
		String dispName;

		for (final Object item : listItems) {
			dispName = item.toString();

			if (dispName.length() > 60) {
				dispName = dispName.substring(0, 59) + " ...";
			}

			curItem = new JMenu(dispName);
			curItem.setBackground(Color.white);
			curItem.add(getPopupMenu());

			JMenuItem copyAll = new JMenuItem("Copy all");
			copyAll.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final StringBuilder builder = new StringBuilder();
					for (Object oneEntry : listItems)
						builder.append(oneEntry.toString() + "\t");

					final StringSelection selection = new StringSelection(builder.toString());
					systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					systemClipboard.setContents(selection, selection);
				}
			});
			curItem.add(copyAll);

			JMenuItem copy = new JMenuItem("Copy one entry");
			copy.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final StringSelection selection = new StringSelection(item.toString());
					systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					systemClipboard.setContents(selection, selection);
				}
			});

			curItem.add(copy);
			curItem.add(popupMenuHelper.getOpenLinkMenu(dispName));
			cellMenu.add(curItem);
		}

		final Border popupBorder = BorderFactory.createTitledBorder(null, borderTitle,
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, BORDER_FONT, Color.BLUE);
		cellMenu.setBorder(popupBorder);
		cellMenu.setBackground(Color.WHITE);
		cellMenu.show(e.getComponent(), e.getX(), e.getY());
	}
	
	private void maybeShowHeaderPopup(final MouseEvent e) {
		if (e.isPopupTrigger()) {
			final int column = getColumnModel().getColumnIndexAtX(e.getX());
			final BrowserTableModel tableModel = (BrowserTableModel) getModel();

			// Make sure the column we're clicking on actually exists!
			if (column >= tableModel.getColumnCount() || column < 0)
				return;

			// Ignore clicks on the ID column:
			if (tableModel.isPrimaryKey(convertColumnIndexToModel(column)))
				return;

			final CyColumn cyColumn = tableModel.getColumn(convertColumnIndexToModel(column));
			popupMenuHelper.createColumnHeaderMenu(cyColumn, tableModel.getTableType(), BrowserTable.this,
					e.getX(), e.getY());
		}
	}
	
	private void maybeShowPopup(final MouseEvent e) {
		if (e.isPopupTrigger()) {
			if (isWinPlatform())
				selectFocusedCell(e);
			
			// Show context menu
			final int viewColumn = getColumnModel().getColumnIndexAtX(e.getX());
			final int viewRow = e.getY() / getRowHeight();
			final int modelColumn = convertColumnIndexToModel(viewColumn);
			final int modelRow = convertRowIndexToModel(viewRow);
			
			final BrowserTableModel tableModel = (BrowserTableModel) this.getModel();
			
			// Bail out if we're at the ID column:
			if (tableModel.isPrimaryKey(modelColumn))
				return;

			// Make sure the column and row we're clicking on actually exists!
			if (modelColumn >= tableModel.getColumnCount() || modelRow >= tableModel.getRowCount())
				return;
			
			final CyColumn cyColumn = tableModel.getColumn(modelColumn);
			final Object primaryKeyValue = ((ValidatedObjectAndEditString) tableModel.getValueAt(modelRow,
					tableModel.getDataTable().getPrimaryKey().getName())).getValidatedObject();
			popupMenuHelper.createTableCellMenu(cyColumn, primaryKeyValue, tableModel.getTableType(), this,
					e.getX(), e.getY(), this);
		}
	}
	
	private void selectFocusedCell(final MouseEvent e) {
		final int row = e.getY() / getRowHeight();
		final int column = getColumnModel().getColumnIndexAtX(e.getX());
		
		final int[] selectedRows = this.getSelectedRows();
		int binarySearch = Arrays.binarySearch(selectedRows, row);
		
		// Select clicked cell, if not selected yet
		if (binarySearch < 0) {
			// Clicked row not selected: Select only the right-clicked row/cell
			this.changeSelection(row, column, false, false);
		} else {
			// Row is already selected: Just guarantee the last clicked cell is highlighted properly
			((BrowserTableListSelectionModel) selectionModel).setLastSelectedRow(row);
			this.setColumnSelectionInterval(column, column);
			
			final BrowserTableModel tableModel = (BrowserTableModel) this.getModel();
			tableModel.fireTableRowsUpdated(selectedRows[0], selectedRows[selectedRows.length-1]);
		}
	}
	
	private String copyToClipBoard() {
		final StringBuffer sbf = new StringBuffer();

		/*
		 * Check to ensure we have selected only a contiguous block of cells.
		 */
		final int numcols = this.getSelectedColumnCount();
		final int numrows = this.getSelectedRowCount();

		final int[] rowsselected = this.getSelectedRows();
		final int[] colsselected = this.getSelectedColumns();

		// Return if no cell is selected.
		if (numcols == 0 && numrows == 0)
			return null;

		if (!((numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0] && numrows == rowsselected.length)
				&& (numcols - 1 == colsselected[colsselected.length - 1] - colsselected[0] 
						&& numcols == colsselected.length))) {
			final JFrame rootFrame = (JFrame) SwingUtilities.getRoot(this);
			JOptionPane.showMessageDialog(rootFrame, "Invalid Copy Selection", "Invalid Copy Selection",
					JOptionPane.ERROR_MESSAGE);

			return null;
		}

		for (int i = 0; i < numrows; i++) {
			for (int j = 0; j < numcols; j++) {
				final Object cellValue = this.getValueAt(rowsselected[i], colsselected[j]);
				if (cellValue == null)
					continue;

				final String cellText = ((ValidatedObjectAndEditString) cellValue).getEditString();
				sbf.append(cellText);

				if (j < (numcols - 1))
					sbf.append("\t");
			}

			sbf.append("\n");
		}

		final StringSelection selection = new StringSelection(sbf.toString());
		systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		systemClipboard.setContents(selection, selection);

		return sbf.toString();
	}
	
	private void renameColumnName(final String oldName, final String newName) {
		BrowserTableColumnModel columnModel = (BrowserTableColumnModel) getColumnModel();
		BrowserTableModel model = (BrowserTableModel) getModel();
		
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
	private void bulkUpdate(final Collection<RowSetRecord> rows) {
		final Set<Long> suidSelected = new HashSet<Long>();
		final Set<Long> suidUnselected = new HashSet<Long>();

		for (RowSetRecord rowSetRecord : rows) {
			if (rowSetRecord.getColumn().equals(CyNetwork.SELECTED)) {
				if (((Boolean)rowSetRecord.getValue()) == true)
					suidSelected.add(rowSetRecord.getRow().get(CyIdentifiable.SUID, Long.class));
				else
					suidUnselected.add(rowSetRecord.getRow().get(CyIdentifiable.SUID, Long.class));
			}
		}

		changeRowSelection(suidSelected, suidUnselected);
	}

	protected void changeRowSelection(final Set<Long> suidSelected, final Set<Long> suidUnselected) {
		final BrowserTableModel model = (BrowserTableModel) getModel();
		final CyTable dataTable = model.getDataTable();
		final String pKeyName = dataTable.getPrimaryKey().getName();
		final int rowCount = getRowCount();
		
		try {
			ignoreRowSelectionEvents = true;
			
			for (int i = 0; i < rowCount; i++) {
				// Getting the row from data table solves the problem with hidden or moved SUID column.
				// However, since the rows might be sorted we need to convert the index to model
				int modelRow = convertRowIndexToModel(i);
				final ValidatedObjectAndEditString tableKey =
						(ValidatedObjectAndEditString) model.getValueAt(modelRow, pKeyName);
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
		final TableModel model = this.getModel();
		
		if (model instanceof BrowserTableModel == false)
			return;

		final BrowserTableModel btModel = (BrowserTableModel) model;

		if (btModel.getViewMode() != BrowserTableModel.ViewMode.ALL)
			return;

		final CyTable table = btModel.getDataTable();
		final CyColumn pKey = table.getPrimaryKey();
		final String pKeyName = pKey.getName();

		final int[] rowsSelected = getSelectedRows();
		
		if (rowsSelected.length == 0)
			return;

		final int selectedRowCount = getSelectedRowCount();
		final Set<CyRow> targetRows = new HashSet<CyRow>();
		
		for (int i = 0; i < selectedRowCount; i++) {
			// getting the row from data table solves the problem with hidden or
			// moved SUID column. However, since the rows might be sorted we
			// need to convert the index to model
			final ValidatedObjectAndEditString selected = (ValidatedObjectAndEditString) btModel.getValueAt(
					convertRowIndexToModel(rowsSelected[i]), pKeyName);
			targetRows.add(btModel.getRow(selected.getValidatedObject()));
		}

		// Clear selection for non-global table
		final CyTableManager tableManager = serviceRegistrar.getService(CyTableManager.class);
		
		if (tableManager.getGlobalTables().contains(table) == false) {
			List<CyRow> allRows = btModel.getDataTable().getAllRows();
			
			try {
				ignoreRowSetEvents = true;
				
				for (CyRow row : allRows) {
					final Boolean val = row.get(CyNetwork.SELECTED, Boolean.class);
					
					if (targetRows.contains(row)) {
						row.set(CyNetwork.SELECTED, true);
						continue;
					}
	
					if (val != null && (val == true))
						row.set(CyNetwork.SELECTED, false);
				}
				
				final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
				final CyNetworkView curView = applicationManager.getCurrentNetworkView();
				
				if (curView != null) {
					final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
					eventHelper.flushPayloadEvents();
					curView.updateView();
				}
			} finally {
				ignoreRowSetEvents = false;
			}
		}
	}
	
	/**
	 * Routine which determines if we are running on mac platform
	 */
	private boolean isMacPlatform() {
		return System.getProperty("os.name").regionMatches(true, 0, MAC_OS_ID, 0, MAC_OS_ID.length());
	}
	
	private boolean isWinPlatform() {
		return System.getProperty("os.name").startsWith(WIN_OS_ID);
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class ProxyMouseListener extends MouseAdapter {
		
		private final MouseListener listener;
		
		public ProxyMouseListener(final MouseListener listener) {
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
		
		private static final long serialVersionUID = 7119443698611148406L;
		
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

		public CellEditorRemover(final KeyboardFocusManager fm) {
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
			final List<?> parsedData = TableBrowserUtil.parseCellInput(cyRow.getTable(), columnName,
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
		
		private static final long serialVersionUID = -4096856015305489834L;

		@Override
		protected Transferable createTransferable(JComponent source) {
			// Encode cell data in Excel format so we can copy/paste list attributes as multi-line cells.
			StringBuilder builder = new StringBuilder();
			BrowserTable table = (BrowserTable) source;
			
			for (int rowIndex : table.getSelectedRows()) {
				boolean firstColumn = true;
				
				for (int columnIndex : table.getSelectedColumns()) {
					if (!firstColumn) {
						builder.append("\t");
					} else {
						firstColumn = false;
					}
					
					Object object = table.getValueAt(rowIndex, columnIndex);
					
					if (object instanceof ValidatedObjectAndEditString) {
						ValidatedObjectAndEditString raw = (ValidatedObjectAndEditString) object;
						Object validatedObject = raw.getValidatedObject();
						
						if (validatedObject instanceof Collection) {
							builder.append("\"");
							boolean firstRow = true;
							
							for (Object member : (Collection<?>) validatedObject) {
								if (!firstRow) {
									builder.append("\r");
								} else {
									firstRow = false;
								}
								builder.append(member.toString().replaceAll("\"", "\"\""));
							}
							builder.append("\"");
						} else {
							builder.append(validatedObject.toString());
						}
					} else {
						if (object != null) {
							builder.append(object.toString());
						}
					}
				}
				builder.append("\n");
			}
			
			return new StringSelection(builder.toString());
		}
		
		@Override
		public int getSourceActions(JComponent c) {
			return TransferHandler.COPY;
		}
	}
}
