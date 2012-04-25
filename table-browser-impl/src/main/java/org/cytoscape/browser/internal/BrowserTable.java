package org.cytoscape.browser.internal;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
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
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.browser.internal.util.TableBrowserUtil;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BrowserTable extends JTable implements MouseListener, ActionListener, MouseMotionListener {
	
	
	private static final long serialVersionUID = 4415856756184765301L;

	private static final Logger logger = LoggerFactory.getLogger(BrowserTable.class);
	
	private static final Font BORDER_FONT = new Font("Sans-serif", Font.BOLD, 12);
	private static final TableCellRenderer cellRenderer = new BrowserTableCellRenderer();
	private static final String MAC_OS_ID = "mac";
	
	private Clipboard systemClipboard;
	private CellEditorRemover editorRemover = null;
	private Map<String, Map<String, String>> linkoutProps;
	private final HashMap<String, Integer> columnWidthMap = new HashMap<String, Integer>();

	// For right-click menu
	private JPopupMenu rightClickPopupMenu;
	private JPopupMenu rightClickHeaderPopupMenu;
	private JPopupMenu cellMenu;
	private JMenuItem openFormulaBuilderMenuItem = null;

	private final OpenBrowser openBrowser;
	private final EquationCompiler compiler;
	private final PopupMenuHelper popupMenuHelper;
	private boolean updateColumnComparators;
	
	private final CyApplicationManager applicationManager;
	private final CyEventHelper eventHelper;
	private final CyTableManager tableManager;

	public BrowserTable(final OpenBrowser openBrowser, final EquationCompiler compiler,
			final PopupMenuHelper popupMenuHelper, final CyApplicationManager applicationManager,
			final CyEventHelper eventHelper, final CyTableManager tableManager) {
		this.openBrowser     = openBrowser;
		this.compiler        = compiler;
		this.popupMenuHelper = popupMenuHelper;
		this.updateColumnComparators = false;
		this.applicationManager = applicationManager;
		this.eventHelper = eventHelper;
		this.tableManager = tableManager;

		initHeader();
		setCellSelectionEnabled(true);
		setDefaultEditor(Object.class, new MultiLineTableCellEditor());
		getPopupMenu();
		getHeaderPopupMenu();
		setKeyStroke();
	}

	public void setUpdateComparators(final boolean updateColumnComparators) {
		this.updateColumnComparators = updateColumnComparators;
	}

	/**
	 * Routine which determines if we are running on mac platform
	 */
	private boolean isMacPlatform() {
		String os = System.getProperty("os.name");

		return os.regionMatches(true, 0, MAC_OS_ID, 0, MAC_OS_ID.length());
	}

	protected void initHeader() {
		this.setBackground(Color.white);
		
		final JTableHeader header = getTableHeader();
		header.addMouseMotionListener(this);
		header.setBackground(Color.white);
		header.setOpaque(false);
		header.setDefaultRenderer(new CustomHeaderRenderer());
		header.addMouseListener(this);
		header.getColumnModel().setColumnSelectionAllowed(true);

		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		final BrowserTable table = this;

		// Event handler. Define actions when mouse is clicked.
		addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {					
					final int column = getColumnModel().getColumnIndexAtX(e.getX());
					final int row = e.getY() / getRowHeight();

					// Bail out if we're at the ID column:
					if (column == 0)
						return;

					final BrowserTableModel tableModel = (BrowserTableModel)table.getModel();
					// Make sure the column and row we're clicking on actually exists!
					if (column >= tableModel.getColumnCount() || row >= tableModel.getRowCount())
						return;

					final ValidatedObjectAndEditString objectAndEditString =
						(ValidatedObjectAndEditString)getValueAt(row, column);
//					getSelected();

					// If action is right click, then show edit pop-up menu
					if ((SwingUtilities.isRightMouseButton(e))
					    || (isMacPlatform() && e.isControlDown()))
					{
						final CyColumn cyColumn =
							tableModel.getColumn(column);
						final Object primaryKeyValue =
							((ValidatedObjectAndEditString)tableModel.getValueAt(row, 0))
							.getValidatedObject();
						popupMenuHelper.createTableCellMenu(cyColumn,
										    primaryKeyValue,
										    table, e.getX(),
										    e.getY());
					} else if (SwingUtilities.isLeftMouseButton(e) && (getSelectedRows().length != 0)) {
						
						showListContents(e);

						if (row >= getRowCount() || row < 0 || column >= getColumnCount() || column < 0)
							return;

						if (objectAndEditString != null && objectAndEditString.getValidatedObject() != null
						    && objectAndEditString.getValidatedObject().getClass() == String.class)
						{
							URL url = null;
							try {
								url = new URL((String)objectAndEditString.getValidatedObject());
							} catch (final MalformedURLException e1) {
								// If invalid, just ignore.
							}

							if (url != null)
								openBrowser.openURL(url.toString());
						}

					}
					
				} // mouseClicked

				@Override
				public void mouseReleased(MouseEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							selectFromTable();
						}
					});
				}
			});
	}
	
	
	private void selectFromTable() {

		final TableModel model = this.getModel();
		if(model instanceof BrowserTableModel == false)
			return;
		
		final BrowserTableModel btModel = (BrowserTableModel) model;
		
		if(btModel.isShowAll() == false)
			return;
		
		final CyTable table = btModel.getDataTable();
		final CyColumn pKey = table.getPrimaryKey();
		final String pKeyName = pKey.getName();
		
		final int[] rowsSelected = getSelectedRows();
		if (rowsSelected.length == 0)
			return;
		
		final int selectedRowCount = getSelectedRowCount();
		
		//TODO: performance tuning
		final int columnCount = this.getColumnCount();
		int targetColIdx;
		for(targetColIdx = 0; targetColIdx<columnCount; targetColIdx++) {
			final String colName = this.getColumnName(targetColIdx);
			if(colName.equals(pKeyName))
				break;
		}
		
		final Set<CyRow> targetRows = new HashSet<CyRow>();
		for(int i=0; i<selectedRowCount; i++) {
			final ValidatedObjectAndEditString selected = (ValidatedObjectAndEditString) this.getValueAt(rowsSelected[i], targetColIdx);
			targetRows.add(btModel.getRow(selected.getValidatedObject()));
		}

		// Clear selection for non-global table
		if (tableManager.getGlobalTables().contains(table) == false) {
			List<CyRow> allRows = btModel.getDataTable().getAllRows();
			for (CyRow row : allRows) {
				final Boolean val = row.get(CyNetwork.SELECTED, Boolean.class);
				if (targetRows.contains(row)) {
					row.set(CyNetwork.SELECTED, true);
					continue;
				}
				
				if (val != null && (val == true))
					row.set(CyNetwork.SELECTED, false);

				final CyNetworkView curView = applicationManager.getCurrentNetworkView();
				if (curView != null) {
					eventHelper.flushPayloadEvents();
					curView.updateView();
				}
			}
		}
	}

	private void setKeyStroke() {
		final KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
		// Identifying the copy KeyStroke user can modify this
		// to copy on some other Key combination.
		this.registerKeyboardAction(this, "Copy", copy, JComponent.WHEN_FOCUSED);
		systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		return cellRenderer;
	}

	@Override
	public boolean isCellEditable(final int row, final int column) {
		return column != 0;
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
		editorRemover.setCellData(model.getCellData(row, column));
				
		if ((editor != null) && editor.isCellEditable(e)) {
			// Do this first so that the bounds of the JTextArea editor
			// will be correct.
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
				((JComponent) editorComp).putClientProperty(MultiLineTableCellEditor.UPDATE_BOUNDS,
				                                            Boolean.TRUE);
				editorComp.setBounds(cellRect.x, cellRect.y,
				                     Math.max(cellRect.width, prefSize.width),
				                     Math.max(cellRect.height, prefSize.height));
				((JComponent) editorComp).putClientProperty(MultiLineTableCellEditor.UPDATE_BOUNDS,
				                                            Boolean.FALSE);				
			} else
				editorComp.setBounds(cellRect);

			add(editorComp);
			editorComp.validate();
			
			
			return true;
		}

		return false;
	}


	/**
	 *  Display elements in the list objects.
	 */
	public void showListContents(MouseEvent e) {
		
		final int column = this.getSelectedColumn();
		final int row = this.getSelectedRow();

		final BrowserTableModel model = (BrowserTableModel)getModel();
		final Class<?> columnType = model.getColumn(column).getType();

		if (columnType == List.class) {
			final ValidatedObjectAndEditString value = (ValidatedObjectAndEditString) model.getValueAt(row, column);

			if (value != null) {
				final List<?> list = (List<?>) value.getValidatedObject();
				
				if (list != null) {
					cellMenu = new JPopupMenu();
					final Object[] listItems = list.toArray();
	
					if (listItems.length != 0) {
						final String idField = model.getAttributes().getPrimaryKey().getName();
						getCellContentView(List.class, listItems, idField, e);
					}
				}
			}
		}
	}

	private void getCellContentView(final Class<?> type, final Object[] listItems, final String idField,
	                                final MouseEvent e)
	{
		final int column = getColumnModel().getColumnIndexAtX(e.getX());
		final int row = e.getY() / getRowHeight();

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

			JMenuItem openURL = new JMenuItem("Open as hyper link...");

			openURL.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						openBrowser.openURL(item.toString());
					}
				});
			curItem.add(openURL);

			JMenuItem copyAll = new JMenuItem("Copy all");
			copyAll.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						StringBuilder builder = new StringBuilder();

						for (Object oneEntry : listItems) {
							builder.append(oneEntry.toString() + "\t");
						}

						final StringSelection selection =
							new StringSelection(builder.toString());
						systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						systemClipboard.setContents(selection, selection);
					}
				});
			curItem.add(copyAll);

			JMenuItem copy = new JMenuItem("Copy one entry");
			copy.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						final StringSelection selection =
							new StringSelection(item.toString());
						systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						systemClipboard.setContents(selection, selection);
					}
				});
			curItem.add(copy);

			if (item.toString().startsWith("http://")) {
				curItem.getMenuComponent(1).setEnabled(true);
			} else
				curItem.getMenuComponent(1).setEnabled(false);

			curItem.getMenuComponent(2).setEnabled(true);

			if (type == List.class)
				curItem.add(new HyperLinkOut(item.toString(), linkoutProps, openBrowser));
			else
				curItem.add(new HyperLinkOut(item.toString().split("=")[1], linkoutProps, openBrowser));

			cellMenu.add(curItem);
		}

		final Border popupBorder = BorderFactory.createTitledBorder(null, idField,
		                                                            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
		                                                            javax.swing.border.TitledBorder.DEFAULT_POSITION,
		                                                            BORDER_FONT, Color.BLUE);
		cellMenu.setBorder(popupBorder);
		cellMenu.setBackground(Color.WHITE);
		cellMenu.show(e.getComponent(), e.getX(), e.getY());
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
/*
		copyMenuItem = new JMenuItem("Copy");
		copyToCurrentSelectionMenuItem = new JMenuItem("Copy to Current Selection");
		copyToCurrentSelectionMenuItem.setEnabled(false);
		copyFormulaToCurrentSelectionMenuItem = new JMenuItem("Copy Formula to Current Selection");
		copyFormulaToCurrentSelectionMenuItem.setEnabled(false);
		copyToEntireAttributeMenuItem = new JMenuItem("Copy to Entire Attribute");
		copyToEntireAttributeMenuItem.setEnabled(false);
		copyFormulaToEntireAttributeMenuItem = new JMenuItem("Copy Formula to Entire Attribute");
		copyFormulaToEntireAttributeMenuItem.setEnabled(false);
		newSelectionMenuItem = new JMenuItem("Select from Table");
		exportMenu = new JMenu("Export...");
		exportCellsMenuItem = new JMenuItem("Selected Cells");
		exportTableMenuItem = new JMenuItem("Entire Table");
		selectAllMenuItem = new JMenuItem("Select All");

		coloringMenuItem = new JCheckBoxMenuItem("On/Off Coloring");
*/

		final JTable table = this;
		openFormulaBuilderMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					final int cellRow = table.getSelectedRow();
					final int cellColumn = table.getSelectedColumn();
					final BrowserTableModel tableModel = (BrowserTableModel)getModel();
					final JFrame rootFrame = (JFrame)SwingUtilities.getRoot(table);
					if (cellRow == -1 || cellColumn == -1
					    || !tableModel.isCellEditable(cellRow, cellColumn))
						JOptionPane.showMessageDialog(rootFrame,
						                              "Can't enter a formula w/o a selected cell!",
						                              "Information", JOptionPane.INFORMATION_MESSAGE);
					else {
						final String columnName = tableModel.getColumnName(cellColumn);
						final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
						FormulaBuilderDialog formulaBuilderDialog =
							new FormulaBuilderDialog(compiler, tableModel,
										 rootFrame, columnName);
						formulaBuilderDialog.setLocationRelativeTo(rootFrame);
						formulaBuilderDialog.setVisible(true);
					}
				}
			});

		return rightClickPopupMenu;
	}

	private JPopupMenu getHeaderPopupMenu() {
		if (rightClickHeaderPopupMenu != null)
			return rightClickHeaderPopupMenu;

		rightClickHeaderPopupMenu = new JPopupMenu();
/*
		final JMenuItem openFormulaBuilderMenuItem = new JMenuItem("Open Formula Builder");
		final TableColumnModel columnModel = getColumnModel();
		final JTable table = this;
		openFormulaBuilderMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(final ActionEvent event) {
					final int cellColumn = columnModel.getColumnIndexAtX(mouseX);
					final String columnName = tableModel.getColumnName(cellColumn);
					final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
					initAttribNameToTypeMap(objectType, columnName, attribNameToTypeMap);
					formulaBuilderDialog =
						new FormulaBuilderDialog(tableModel, table, objectType, Cytoscape.getDesktop(),
									 attribNameToTypeMap, columnName);
					formulaBuilderDialog.setLocationRelativeTo(Cytoscape.getDesktop());
					formulaBuilderDialog.setVisible(true);
				}
			});
		rightClickHeaderPopupMenu.add(openFormulaBuilderMenuItem);
*/

		return rightClickHeaderPopupMenu;
	}

	@Override
	public void mouseReleased(MouseEvent event) {		
	}

	@Override
	public void mousePressed(MouseEvent event) {
	}

	@Override
	public void mouseClicked(final MouseEvent event) {
		final int cursorType = getTableHeader().getCursor().getType();
		if (event.getButton() == MouseEvent.BUTTON3) {
			final int column = getColumnModel().getColumnIndexAtX(event.getX());
			final BrowserTableModel tableModel = (BrowserTableModel)getModel();

			// Make sure the column we're clicking on actually exists!
			if (column >= tableModel.getColumnCount() || column < 0)
				return;

			// Ignore clicks on the ID column:
			if (column == 0)
				return;

			final CyColumn cyColumn = tableModel.getColumn(column);
			popupMenuHelper.createColumnHeaderMenu(cyColumn, this, event.getX(), event.getY());
		}
	}

	@Override
	public void mouseEntered(MouseEvent event) {
		
	}

	@Override
	public void mouseExited(MouseEvent event) {
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().compareTo("Copy") == 0)
			copyToClipBoard();
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

		if (!((numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0]
		       && numrows == rowsselected.length)
		      && (numcols - 1 == colsselected[colsselected.length - 1] - colsselected[0]
			  && numcols == colsselected.length))) {
			final JFrame rootFrame = (JFrame)SwingUtilities.getRoot(this);
			JOptionPane.showMessageDialog(rootFrame, "Invalid Copy Selection",
			                              "Invalid Copy Selection", JOptionPane.ERROR_MESSAGE);

			return null;
		}

		for (int i = 0; i < numrows; i++) {
			for (int j = 0; j < numcols; j++) {
				final Object cellValue = this.getValueAt(rowsselected[i], colsselected[j]);
				if (cellValue == null)
					continue;

				final String cellText = ((ValidatedObjectAndEditString)cellValue).getEditString();
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

	@Override
	public void mouseDragged(MouseEvent e) {
		// save the column width, if user adjust column width manually
		if (e.getSource() instanceof JTableHeader){
			final int index = getColumnModel().getColumnIndexAtX(e.getX());
			if (index != -1){
				int colWidth = getColumnModel().getColumn(index).getWidth();
				this.columnWidthMap.put(this.getColumnName(index), new Integer(colWidth));				
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	private class CellEditorRemover implements PropertyChangeListener {
		private final KeyboardFocusManager focusManager;
		private BrowserTableModel model;
		private int row =-1, column = -1;
		private Vector cellVect = null;

		public CellEditorRemover(final KeyboardFocusManager fm) {
			this.focusManager = fm;
		}

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
						}
						catch (Exception e){
							getCellEditor().cancelCellEditing();
							//Update the cell data based on the remembered value
							updateAttributeAfterCellLostFocus();
						}
					}

					break;
				}

				c = c.getParent();
			}
		}
		
		// Cell data passed from previous TableModel, because tableModel will disappear if 
		// user click on open space on canvas, so we have to remember it before it is gone
		public void setCellData(Vector cellVect){
			this.cellVect = cellVect;
		}
		
		private void updateAttributeAfterCellLostFocus(){
			
			CyRow rowObj = (CyRow) cellVect.get(0);
			String columnName = (String)cellVect.get(1);
			
			ArrayList parsedData = TableBrowserUtil.parseCellInput(rowObj.getTable(), columnName, MultiLineTableCellEditor.lastValueUserEntered);
			
			if (parsedData.get(0) != null){
				rowObj.set(columnName, MultiLineTableCellEditor.lastValueUserEntered);									
			}
			else {
				// Error
				// discard the change
			}
		}	
	}
		
	
	public void addColumn(final TableColumn aColumn) {
		super.addColumn(aColumn);

		if (!updateColumnComparators)
			return;

		final TableRowSorter rowSorter = (TableRowSorter)getRowSorter();
		if (rowSorter == null)
			return;

		final BrowserTableModel tableModel = (BrowserTableModel)getModel();
		final Class<?> rowDataType = tableModel.getColumn(aColumn.getModelIndex()).getType();
		rowSorter.setComparator(aColumn.getModelIndex(),
					new ValidatedObjectAndEditStringComparator(rowDataType));
	}
	


}

