package org.cytoscape.browser.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.IconManager.ICON_COG;
import static org.cytoscape.util.swing.IconManager.ICON_TRASH_O;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.application.swing.CyColumnSelector;
import org.cytoscape.browser.internal.util.IconUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.destroy.DeleteTableTaskFactory;
import org.cytoscape.task.read.LoadTableFileTaskFactory;
import org.cytoscape.task.write.ExportTableTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.work.swing.DialogTaskManager;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

/**
 * Toolbar for the Browser.  All buttons related to this should be placed here.
 */
@SuppressWarnings("serial")
public class TableBrowserToolBar extends JPanel implements PopupMenuListener {
	
	public static final float ICON_FONT_SIZE = 22.0f;

	private TableRenderer tableRenderer;
	
	/* GUI components */
	private JPopupMenu columnSelectorPopupMenu;
	private CyColumnSelector columnSelector;
	private JPopupMenu createColumnMenu;

	private JToolBar toolBar;
	private SequentialGroup hToolBarGroup;
	private ParallelGroup vToolBarGroup;
	
	private JButton selectionModeButton;
	private JButton showColumnsButton;
	private JButton createColumnButton;
	private JButton deleteColumnsButton;
	private JButton deleteTableButton;
	private JButton fnBuilderButton;
	private JButton importButton;
	private JButton exportButton;
	
	private final JComboBox<CyTable> tableChooser;

//	private AttributeListModel attrListModel;
	
	private final List<JComponent> components;
	
	private final Class<? extends CyIdentifiable> objType;

	private final CyServiceRegistrar serviceRegistrar;
	private final IconManager iconMgr;

	public TableBrowserToolBar(
			final CyServiceRegistrar serviceRegistrar,
			final JComboBox<CyTable> tableChooser,
			final Class<? extends CyIdentifiable> objType
	) {
		this.components = new ArrayList<>();
		this.tableChooser = tableChooser;
//		this.attrListModel = new AttributeListModel(null);
		this.objType = objType;
		this.serviceRegistrar = serviceRegistrar;
		this.iconMgr = serviceRegistrar.getService(IconManager.class);
		
//		serviceRegistrar.registerAllServices(attrListModel, new Properties());

		initializeGUI();
	}

	public void setTableRenderer(TableRenderer tableRenderer) {
		this.tableRenderer = tableRenderer;
//		attrListModel.setBrowserTableModel(browserTableModel);
		updateEnableState();
		
		// MKTODO this needs to happen via VisualProperty events
//		if (browserTable != null) {
//			browserTable.getSelectionModel().addListSelectionListener(e -> {
//				if (!e.getValueIsAdjusting())
//					updateEnableState(fnBuilderButton);
//			});
//			browserTable.getColumnModel().getSelectionModel().addListSelectionListener(e -> {
//				if (!e.getValueIsAdjusting())
//					updateEnableState(fnBuilderButton);
//			});
//		}
	}

	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
		// Do nothing
	}
	
	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		// Update actual table
		try {
			final Set<String> visibleAttributes = getColumnSelector().getSelectedColumnNames();
			Collection<View<CyColumn>> columnViews = tableRenderer.getTableView().getColumnViews();
			
			for(View<CyColumn> columnView : columnViews) {
				boolean visible = visibleAttributes.contains(columnView.getModel().getName());
				columnView.setVisualProperty(BasicTableVisualLexicon.COLUMN_VISIBLE, visible);
			}
			
//			browserTable.setVisibleAttributeNames(visibleAttributes);
//			updateEnableState();
		} catch (Exception ex) {
		}
	}

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		// Do nothing
	}
	
	protected void updateEnableState() {
//		for (final JComponent comp : components)
//			updateEnableState(comp);
	}
	
	protected void updateEnableState(final JComponent comp) {
//		if (comp == null)
//			return;
//		
//		boolean enabled = false;
//		
//		if (browserTableModel != null) {
//			final CyTable attrs = browserTableModel.getDataTable();
//			
//			if (comp == deleteTableButton) {
//				enabled = browserTableModel.getDataTable().getMutability() == Mutability.MUTABLE;
//			} else if (comp == deleteColumnsButton) {
//				for (final CyColumn column : attrs.getColumns()) {
//					if (!column.isImmutable()) {
//						enabled = true;
//						break;
//					}
//				}
//			} else if (comp == fnBuilderButton) {
//				final int row = browserTable.getSelectedRow();
//				final int column = browserTable.getSelectedColumn();
//				enabled = row >=0 && column >= 0 && browserTableModel.isCellEditable(row, column);
//			} else if (comp == tableChooser) {
//				enabled = tableChooser.getItemCount() > 0;
//			} else {
//				enabled = true;
//			}
//		}
//		
//		comp.setEnabled(enabled);
//		
//		// Unfortunately this is necessary on Nimbus!
//		if (comp instanceof AbstractButton && LookAndFeelUtil.isNimbusLAF())
//			comp.setForeground(UIManager.getColor(enabled ? "Button.foreground" : "Button.disabledForeground"));
	}
	
	private void initializeGUI() {
		setLayout(new BorderLayout());
		setOpaque(!isAquaLAF());
		add(getToolBar(), BorderLayout.CENTER);

		// Add buttons
		if (objType == CyNode.class || objType == CyEdge.class)
			addComponent(getSelectionModeButton(), ComponentPlacement.RELATED);
		
		addComponent(getShowColumnsButton(), ComponentPlacement.RELATED);
		addComponent(getCreateColumnButton(), ComponentPlacement.RELATED);
		addComponent(getDeleteColumnsButton(), ComponentPlacement.RELATED);
		addComponent(getDeleteTableButton(), ComponentPlacement.RELATED);
		addComponent(getFnBuilderButton(), ComponentPlacement.RELATED);
//		addComponent(getMapGlobalTableButton(). ComponentPlacement.RELATED);
		addComponent(getImportButton(), ComponentPlacement.UNRELATED);
		addComponent(getExportButton(), ComponentPlacement.RELATED);
		
		if (tableChooser != null) {
			hToolBarGroup.addGap(0, 20, Short.MAX_VALUE);
			addComponent(tableChooser, ComponentPlacement.UNRELATED);
		}
		
		updateEnableState();
	}
	
	private void addComponent(final JComponent component, final ComponentPlacement placement) {
		if (placement != null)
			hToolBarGroup.addPreferredGap(placement);
		
		hToolBarGroup.addComponent(component);
		vToolBarGroup.addComponent(component, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
		
		components.add(component);
	}

	protected void styleButton(final AbstractButton btn, final Font font) {
		btn.setFont(font);
		btn.setBorder(null);
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		
		int w = 32, h = 32;
		
		if (tableChooser != null)
			h = Math.max(h, tableChooser.getPreferredSize().height);
		
		btn.setMinimumSize(new Dimension(w, h));
		btn.setPreferredSize(new Dimension(w, h));
	}
	
	private JPopupMenu getColumnSelectorPopupMenu() {
		if (columnSelectorPopupMenu == null) {
			columnSelectorPopupMenu = new JPopupMenu();
			columnSelectorPopupMenu.add(getColumnSelector());
			columnSelectorPopupMenu.addPopupMenuListener(this);
			columnSelectorPopupMenu.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (SwingUtilities.isRightMouseButton(e)) {
						columnSelectorPopupMenu.setVisible(false);
					}
				}
			});
		}

		return columnSelectorPopupMenu;
	}
	
	private CyColumnSelector getColumnSelector() {
		if (columnSelector == null) {
			IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			CyColumnPresentationManager presetationManager = serviceRegistrar.getService(CyColumnPresentationManager.class);
			columnSelector = new CyColumnSelector(iconManager, presetationManager);
		}
		
		return columnSelector;
	}

	private JPopupMenu getCreateColumnMenu() {
		if (createColumnMenu == null) {
			createColumnMenu = new JPopupMenu();
			
			final JMenu columnRegular = new JMenu("New Single Column");
			final JMenu columnList = new JMenu("New List Column");

			columnRegular.add(getJMenuItemIntegerAttribute(false));
			columnRegular.add(getJMenuItemLongIntegerAttribute(false));
			columnRegular.add(getJMenuItemStringAttribute(false));
			columnRegular.add(getJMenuItemFloatingPointAttribute(false));
			columnRegular.add(getJMenuItemBooleanAttribute(false));
			columnList.add(getJMenuItemIntegerListAttribute(false));
			columnList.add(getJMenuItemLongIntegerListAttribute(false));
			columnList.add(getJMenuItemStringListAttribute(false));
			columnList.add(getJMenuItemFloatingPointListAttribute(false));
			columnList.add(getJMenuItemBooleanListAttribute(false));
			
			createColumnMenu.add(columnRegular);
			createColumnMenu.add(columnList);
		}

		return createColumnMenu;
	}

	private JMenuItem getJMenuItemStringAttribute(final boolean isShared) {
		final JMenuItem mi = new JMenuItem();
		mi.setText("String");
		mi.addActionListener(e -> createNewAttribute("String", isShared));

		return mi;
	}

	private JMenuItem getJMenuItemIntegerAttribute(final boolean isShared) {
		final JMenuItem mi = new JMenuItem();
		mi.setText("Integer");
		mi.addActionListener(e -> createNewAttribute("Integer", isShared));

		return mi;
	}

	private JMenuItem getJMenuItemLongIntegerAttribute(final boolean isShared) {
		final JMenuItem mi = new JMenuItem();
		mi.setText("Long Integer");
		mi.addActionListener(e -> createNewAttribute("Long Integer", isShared));
		
		return mi;
	}

	private JMenuItem getJMenuItemFloatingPointAttribute(final boolean isShared) {
		final JMenuItem mi = new JMenuItem();
		mi.setText("Floating Point");
		mi.addActionListener(e -> createNewAttribute("Floating Point", isShared));

		return mi;
	}

	private JMenuItem getJMenuItemBooleanAttribute(final boolean isShared) {
		final JMenuItem mi = new JMenuItem();
		mi.setText("Boolean");
		mi.addActionListener(e -> createNewAttribute("Boolean", isShared));

		return mi;
	}

	private JMenuItem getJMenuItemStringListAttribute(final boolean isShared) {
		final JMenuItem mi = new JMenuItem();
		mi.setText("String");
		mi.addActionListener(e -> createNewAttribute("String List", isShared));

		return mi;
	}

	private JMenuItem getJMenuItemIntegerListAttribute(final boolean isShared) {
		final JMenuItem mi = new JMenuItem();
		mi.setText("Integer");
		mi.addActionListener(e -> createNewAttribute("Integer List", isShared));

		return mi;
	}

	private JMenuItem getJMenuItemLongIntegerListAttribute(final boolean isShared) {
		final JMenuItem mi = new JMenuItem();
		mi.setText("Long Integer");
		mi.addActionListener(e -> createNewAttribute("Long Integer List", isShared));

		return mi;
	}

	private JMenuItem getJMenuItemFloatingPointListAttribute(final boolean isShared) {
		final JMenuItem mi = new JMenuItem();
		mi.setText("Floating Point");
		mi.addActionListener(e -> createNewAttribute("Floating Point List", isShared));

		return mi;
	}

	private JMenuItem getJMenuItemBooleanListAttribute(final boolean isShared) {
		final JMenuItem mi = new JMenuItem();
		mi.setText("Boolean");
		mi.addActionListener(e -> createNewAttribute("Boolean List", isShared));

		return mi;
	}

	private JToolBar getToolBar() {
		if (toolBar == null) {
			toolBar = new JToolBar();
			toolBar.setFloatable(false);
			toolBar.setOrientation(JToolBar.HORIZONTAL);
			toolBar.setOpaque(!isAquaLAF());

			final GroupLayout layout = new GroupLayout(toolBar);
			toolBar.setLayout(layout);
			hToolBarGroup = layout.createSequentialGroup();
			vToolBarGroup = layout.createParallelGroup(Alignment.CENTER, false);
			
			// Layout information.
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(hToolBarGroup));
			layout.setVerticalGroup(vToolBarGroup);
		}

		return toolBar;
	}

	private JButton getShowColumnsButton() {
		if (showColumnsButton == null) {
			showColumnsButton = new JButton(IconUtil.COLUMN_SHOW);
			showColumnsButton.setToolTipText("Show Columns...");
			styleButton(showColumnsButton, iconMgr.getIconFont(IconUtil.CY_FONT_NAME, TableBrowserToolBar.ICON_FONT_SIZE));

			showColumnsButton.addActionListener(e -> {
				if (tableRenderer != null) {
					
					Collection<View<CyColumn>> columnViews = tableRenderer.getTableView().getColumnViews();
					
					List<CyColumn> columns = new ArrayList<>();
					List<String> visibleColumns = new ArrayList<>();
					
					for(View<CyColumn> columnView : columnViews) {
						columns.add(columnView.getModel());
						if(Boolean.TRUE.equals(columnView.getVisualProperty(BasicTableVisualLexicon.COLUMN_VISIBLE))) {
							visibleColumns.add(columnView.getModel().getName());
						}
					}
					
					getColumnSelector().update(columns, visibleColumns);
					getColumnSelectorPopupMenu().pack();
					getColumnSelectorPopupMenu().show(showColumnsButton, 0, showColumnsButton.getHeight());
				}
			});
		}
		
		return showColumnsButton;
	}

	private JButton getFnBuilderButton() {
		if (fnBuilderButton == null) {
			fnBuilderButton = new JButton("f(x)");
			fnBuilderButton.setToolTipText("Function Builder...");
			
			Font iconFont = null;
			
			try {
				iconFont = Font.createFont(Font.TRUETYPE_FONT, 
						getClass().getResourceAsStream("/fonts/jsMath-cmti10.ttf"));
			} catch (Exception e) {
				throw new RuntimeException("Error loading font", e);
			}
			
			styleButton(fnBuilderButton, iconFont.deriveFont(18.0f));

			final JFrame rootFrame = (JFrame) SwingUtilities.getRoot(this);

			// MKTODO
//			fnBuilderButton.addActionListener(new ActionListener() {
//				@Override
//				public void actionPerformed(final ActionEvent e) {
//					// Do not allow opening of the formula builder dialog while a cell is being edited!
//					if (browserTableModel == null || browserTable.getCellEditor() != null)
//						return;
//
//					final int cellRow = browserTable.getSelectedRow();
//					final int cellColumn = browserTable.getSelectedColumn();
//					int colIndex = -1;
//
//					// Map the screen index of column to internal index of the table model
//					if (cellRow >= 0 && cellColumn >= 0) {
//						String colName = browserTable.getColumnName(cellColumn);
//						colIndex = browserTableModel.mapColumnNameToColumnIndex(colName);
//					}
//					
//					if (cellRow == -1 || cellColumn == -1 || !browserTableModel.isCellEditable(cellRow, colIndex)) {
//						JOptionPane.showMessageDialog(rootFrame, "Can't enter a formula w/o a selected cell.",
//								"Information", JOptionPane.INFORMATION_MESSAGE);
//					} else {
//						final String attrName = getColumnName(cellRow, cellColumn);
//						final Map<String, Class<?>> attribNameToTypeMap = new HashMap<>();
//						final CyTable dataTable = browserTableModel.getDataTable();
//						initAttribNameToTypeMap(dataTable, attrName, attribNameToTypeMap);
//						
//						final EquationCompiler compiler = serviceRegistrar.getService(EquationCompiler.class);
//						
//						final FormulaBuilderDialog formulaBuilderDialog = new FormulaBuilderDialog(compiler,
//								browserTable, rootFrame, attrName);
//						formulaBuilderDialog.setLocationRelativeTo(rootFrame);
//						formulaBuilderDialog.setVisible(true);
//					}
//				}
//
//				private void initAttribNameToTypeMap(final CyTable dataTable, final String attrName,
//						final Map<String, Class<?>> attribNameToTypeMap) {
//					for (final CyColumn column : dataTable.getColumns())
//						attribNameToTypeMap.put(column.getName(), column.getType());
//					
//					attribNameToTypeMap.remove(attrName);
//				}
//			});
		}
		
		return fnBuilderButton;
	}

//	private String getColumnName(final int cellRow, final int cellColumn) {
//		int colIndexModel = browserTable.convertColumnIndexToModel(cellColumn);
//		return browserTableModel.getColumnName( colIndexModel);
//	}

	private JButton getDeleteColumnsButton() {
		if (deleteColumnsButton == null) {
			deleteColumnsButton = new JButton(IconUtil.COLUMN_REMOVE);
			deleteColumnsButton.setToolTipText("Delete Columns...");
			styleButton(deleteColumnsButton, iconMgr.getIconFont(IconUtil.CY_FONT_NAME, TableBrowserToolBar.ICON_FONT_SIZE));
			
			// Create pop-up window for deletion
			deleteColumnsButton.addActionListener(e -> {
				showColumnDeletionDialog();
				updateEnableState();
			});
		}

		return deleteColumnsButton;
	}

	private JButton getDeleteTableButton() {
		if (deleteTableButton == null) {
			deleteTableButton = new JButton(ICON_TRASH_O);
			deleteTableButton.setToolTipText("Delete Table...");
			styleButton(deleteTableButton, iconMgr.getIconFont(ICON_FONT_SIZE));
			
			// Create pop-up window for deletion
			deleteTableButton.addActionListener(e -> deleteTable());
		}

		return deleteTableButton;
	}
	
	private void showColumnDeletionDialog() {
		final JFrame frame = (JFrame) SwingUtilities.getRoot(this);
		final DeletionDialog dDialog = new DeletionDialog(frame, tableRenderer.getDataTable());

		dDialog.pack();
		dDialog.setLocationRelativeTo(toolBar);
		dDialog.setVisible(true);
	}

	private void deleteTable() {
		final CyTable table = tableRenderer.getDataTable();

		if (table.getMutability() == CyTable.Mutability.MUTABLE) {
			String title = "Please confirm this action";
			String msg = "Are you sure you want to delete this table?";
			int confirmValue = JOptionPane.showConfirmDialog(this, msg, title, JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);

			// if user selects yes delete the table
			if (confirmValue == JOptionPane.OK_OPTION) {
				final DialogTaskManager taskMgr = serviceRegistrar.getService(DialogTaskManager.class);
				final DeleteTableTaskFactory deleteTableTaskFactory =
						serviceRegistrar.getService(DeleteTableTaskFactory.class);
				
				taskMgr.execute(deleteTableTaskFactory.createTaskIterator(table));
			}
		} else if (table.getMutability() == CyTable.Mutability.PERMANENTLY_IMMUTABLE) {
			String title = "Error";
			String msg = "Can not delete this table, it is PERMANENTLY_IMMUTABLE";
			JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
		} else if (table.getMutability() == CyTable.Mutability.IMMUTABLE_DUE_TO_VIRT_COLUMN_REFERENCES) {
			String title = "Error";
			String msg = "Can not delete this table, it is IMMUTABLE_DUE_TO_VIRT_COLUMN_REFERENCES";
			JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
		}
	}

	private String[] getAttributeArray() {
		final CyTable attrs = tableRenderer.getDataTable();
		final Collection<CyColumn> columns = attrs.getColumns();
		final String[] attributeArray = new String[columns.size() - 1];
		int index = 0;
		for (final CyColumn column : columns) {
			if (!column.isPrimaryKey())
				attributeArray[index++] = column.getName();
		}
		Arrays.sort(attributeArray);

		return attributeArray;
	}

	protected JButton getSelectionModeButton() {
		if (selectionModeButton == null) {
			selectionModeButton = new JButton(ICON_COG);
			selectionModeButton.setToolTipText("Change Table Mode...");
			styleButton(selectionModeButton, iconMgr.getIconFont(TableBrowserToolBar.ICON_FONT_SIZE * 4/5));
		}
		
		return selectionModeButton;
	}
	
	private JButton getCreateColumnButton() {
		if (createColumnButton == null) {
			createColumnButton = new JButton(IconUtil.COLUMN_ADD);
			createColumnButton.setToolTipText("Create New Column...");
			styleButton(createColumnButton, iconMgr.getIconFont(IconUtil.CY_FONT_NAME, TableBrowserToolBar.ICON_FONT_SIZE));
			
			createColumnButton.addActionListener(e -> {
				if (tableRenderer != null)
					getCreateColumnMenu().show(createColumnButton, 0, createColumnButton.getHeight());
			});
			
			createColumnButton.setEnabled(false);
		}

		return createColumnButton;
	}
	
	/*
	private JButton getMapGlobalTableButton() {
		if (mapGlobalTableButton == null) {
			mapGlobalTableButton = new JButton();
			mapGlobalTableButton.setBorder(null);
			mapGlobalTableButton.setMargin(new Insets(0, 0, 0, 0));
			mapGlobalTableButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("images/table_map.png")));
			mapGlobalTableButton.setToolTipText("Link Table to Attributes");
			mapGlobalTableButton.setBorder(null);

			mapGlobalTableButton.addActionListener(e -> {
				if (mapGlobalTableButton.isEnabled())
					guiTaskManagerServiceRef.execute(mapGlobalTableTaskFactoryService.createTaskIterator( browserTableModel.getAttributes() ));
			});
		}
		mapGlobalTableButton.setEnabled(false);

		return mapGlobalTableButton;
	}
	*/
	
	private JButton getImportButton() {
		if (importButton == null) {
			importButton = new JButton(IconUtil.FILE_IMPORT);
			importButton.setToolTipText("Import Table from File...");
			styleButton(importButton, iconMgr.getIconFont(IconUtil.CY_FONT_NAME, TableBrowserToolBar.ICON_FONT_SIZE));
			
			importButton.addActionListener(e -> {
				LoadTableFileTaskFactory factory = serviceRegistrar.getService(LoadTableFileTaskFactory.class);
				DialogTaskManager taskManager = serviceRegistrar.getService(DialogTaskManager.class);
				taskManager.execute(factory.createTaskIterator());
			});
		}
		
		return importButton;
	}
	
	private JButton getExportButton() {
		if (exportButton == null) {
			exportButton = new JButton(IconUtil.FILE_EXPORT);
			exportButton.setToolTipText("Export Table to File...");
			styleButton(exportButton, iconMgr.getIconFont(IconUtil.CY_FONT_NAME, TableBrowserToolBar.ICON_FONT_SIZE));
			
			exportButton.addActionListener(e -> {
				ExportTableTaskFactory factory = serviceRegistrar.getService(ExportTableTaskFactory.class);
				DialogTaskManager taskManager = serviceRegistrar.getService(DialogTaskManager.class);
				taskManager.execute(factory.createTaskIterator(tableRenderer.getDataTable()));
			});
		}
		
		return exportButton;
	}
	
	private void createNewAttribute(final String type, boolean isShared) {
		try {
			final String[] existingAttrs = getAttributeArray();
			String newAttribName = null;
			
			do {
				newAttribName = JOptionPane.showInputDialog(this, "Column Name: ",
									    "Create New " + type + " Column",
									    JOptionPane.QUESTION_MESSAGE);
				
				if (newAttribName == null)
					return;
				
				newAttribName = newAttribName.trim();
				
				if (newAttribName.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Column name must not be blank.",
						      "Error", JOptionPane.ERROR_MESSAGE);
					newAttribName = null;
				} else if (Arrays.binarySearch(existingAttrs, newAttribName) >= 0) {
					JOptionPane.showMessageDialog(null,
								      "Column " + newAttribName + " already exists.",
								      "Error", JOptionPane.ERROR_MESSAGE);
					newAttribName = null;
				}
			} while (newAttribName == null);
	
			final CyTable attrs;
			
			if (isShared) {
				final CyNetwork network = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();
							
				if (network instanceof CySubNetwork) {
					final CyRootNetwork rootNetwork = ((CySubNetwork) network).getRootNetwork();
					CyTable sharedTable = null;
					
					if (this.objType == CyNode.class)
						sharedTable = rootNetwork.getSharedNodeTable();
					else if (this.objType == CyEdge.class)
						sharedTable = rootNetwork.getSharedEdgeTable();
					else if (this.objType == CyNetwork.class)
						sharedTable = rootNetwork.getSharedNetworkTable();
					else
						throw new IllegalStateException("Object type is not valid.  This should not happen.");
					
					attrs = sharedTable;
				} else {
					throw new IllegalArgumentException("This is not a CySubNetwork and there is no shared table.");
				}
			} else {
				attrs = tableRenderer.getDataTable();
			}
		
			if (type.equals("String"))
				attrs.createColumn(newAttribName, String.class, false);
			else if (type.equals("Floating Point"))
				attrs.createColumn(newAttribName, Double.class, false);
			else if (type.equals("Integer"))
				attrs.createColumn(newAttribName, Integer.class, false);
			else if (type.equals("Long Integer"))
				attrs.createColumn(newAttribName, Long.class, false);
			else if (type.equals("Boolean"))
				attrs.createColumn(newAttribName, Boolean.class, false);
			else if (type.equals("String List"))
				attrs.createListColumn(newAttribName, String.class, false);
			else if (type.equals("Floating Point List"))
				attrs.createListColumn(newAttribName, Double.class, false);
			else if (type.equals("Integer List"))
				attrs.createListColumn(newAttribName, Integer.class, false);
			else if (type.equals("Long Integer List"))
				attrs.createListColumn(newAttribName, Long.class, false);
			else if (type.equals("Boolean List"))
				attrs.createListColumn(newAttribName, Boolean.class, false);
			else
				throw new IllegalArgumentException("unknown column type \"" + type + "\".");
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
