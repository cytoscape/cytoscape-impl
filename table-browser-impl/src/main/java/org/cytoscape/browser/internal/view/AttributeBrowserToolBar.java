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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.IconManager.ICON_COLUMNS;
import static org.cytoscape.util.swing.IconManager.ICON_PLUS;
import static org.cytoscape.util.swing.IconManager.ICON_TABLE;
import static org.cytoscape.util.swing.IconManager.ICON_TIMES_CIRCLE;
import static org.cytoscape.util.swing.IconManager.ICON_TRASH_O;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTable.Mutability;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.destroy.DeleteTableTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.swing.DialogTaskManager;


/**
 * Toolbar for the Browser.  All buttons related to this should be placed here.
 */
public class AttributeBrowserToolBar extends JPanel implements PopupMenuListener {
	
	private static final long serialVersionUID = -508393701912596399L;

	public static final float ICON_FONT_SIZE = 22.0f;
	
	private BrowserTable browserTable;
	private BrowserTableModel browserTableModel;
	
	private static final Dimension TOOLBAR_SIZE = new Dimension(500, 38);

	/* GUI components */
	private JPopupMenu columnSelectorPopupMenu;
	private ColumnSelector columnSelector;
	private JPopupMenu createColumnMenu;

	private JToolBar toolBar;
	private SequentialGroup hToolBarGroup;
	private ParallelGroup vToolBarGroup;
	
	private JButton selectButton;
	
	private JButton createNewAttributeButton;
	private JButton deleteAttributeButton;
	private JButton deleteTableButton;
	private JButton formulaBuilderButton;
	
	private final JComboBox<CyTable> tableChooser;

	private AttributeListModel attrListModel;
	
	private final JButton selectionModeButton;
	private final List<JComponent> components;
	
	private final Class<? extends CyIdentifiable> objType;

	private final CyServiceRegistrar serviceRegistrar;
	private final IconManager iconMgr;

	public AttributeBrowserToolBar(
			final CyServiceRegistrar serviceRegistrar,
			final JComboBox<CyTable> tableChooser,
			final Class<? extends CyIdentifiable> objType
	) {
		this(serviceRegistrar, tableChooser, new JButton(), objType);
		this.selectionModeButton.setVisible(false);
	}
	
	public AttributeBrowserToolBar(
			final CyServiceRegistrar serviceRegistrar,
			final JComboBox<CyTable> tableChooser,
			final JButton selectionModeButton,
			final Class<? extends CyIdentifiable> objType
	) {
		this.selectionModeButton = selectionModeButton;
		this.components = new ArrayList<>();
		this.tableChooser = tableChooser;
		this.attrListModel = new AttributeListModel(null);
		this.objType = objType;
		this.serviceRegistrar = serviceRegistrar;
		this.iconMgr = serviceRegistrar.getService(IconManager.class);
		
		serviceRegistrar.registerAllServices(attrListModel, new Properties());

		selectionModeButton.setEnabled(false);
		initializeGUI();
	}

	public void setBrowserTable(final BrowserTable browserTable) {
		this.browserTable = browserTable;
		browserTableModel = browserTable != null ? (BrowserTableModel) browserTable.getModel() : null;
		attrListModel.setBrowserTableModel(browserTableModel);
		updateEnableState();
		
		if (browserTable != null) {
			browserTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(final ListSelectionEvent e) {
					if (!e.getValueIsAdjusting())
						updateEnableState(formulaBuilderButton);
				}
			});
			browserTable.getColumnModel().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(final ListSelectionEvent e) {
					if (!e.getValueIsAdjusting())
						updateEnableState(formulaBuilderButton);
				}
			});
		}
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
			browserTable.setVisibleAttributeNames(visibleAttributes);
			updateEnableState();
		} catch (Exception ex) {
		}
	}

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		// Do nothing
	}

	protected void updateEnableState() {
		for (final JComponent comp : components)
			updateEnableState(comp);
	}
	
	protected void updateEnableState(final JComponent comp) {
		if (comp == null)
			return;
		
		boolean enabled = false;
		
		if (browserTableModel != null) {
			final CyTable attrs = browserTableModel.getDataTable();
			
			if (comp == deleteTableButton) {
				enabled = browserTableModel.getDataTable().getMutability() == Mutability.MUTABLE;
			} else if (comp == deleteAttributeButton) {
				for (final CyColumn column : attrs.getColumns()) {
					if (!column.isImmutable()) {
						enabled = true;
						break;
					}
				}
			} else if (comp == formulaBuilderButton) {
				final int row = browserTable.getSelectedRow();
				final int column = browserTable.getSelectedColumn();
				enabled = row >=0 && column >= 0 && browserTableModel.isCellEditable(row, column);
			} else if (comp == tableChooser) {
				enabled = tableChooser.getItemCount() > 0;
			} else {
				enabled = true;
			}
		}
		
		comp.setEnabled(enabled);
		
		// Unfortunately this is necessary on Nimbus!
		if (comp instanceof AbstractButton && LookAndFeelUtil.isNimbusLAF())
			comp.setForeground(UIManager.getColor(enabled ? "Button.foreground" : "Button.disabledForeground"));
	}
	
	private void initializeGUI() {
		setLayout(new BorderLayout());
		setOpaque(!isAquaLAF());
		add(getToolBar(), BorderLayout.CENTER);

		// Add buttons
		if (selectionModeButton != null)
			addComponent(selectionModeButton, ComponentPlacement.RELATED);
		
		addComponent(getSelectButton(), ComponentPlacement.RELATED);
		addComponent(getNewButton(), ComponentPlacement.RELATED);
		addComponent(getDeleteButton(), ComponentPlacement.RELATED);
		addComponent(getDeleteTableButton(), ComponentPlacement.RELATED);
		addComponent(getFunctionBuilderButton(), ComponentPlacement.RELATED);
//		addComponent(getMapGlobalTableButton(). ComponentPlacement.RELATED);
		
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

	static void styleButton(final AbstractButton btn, final Font font) {
		btn.setFont(font);
		btn.setBorder(null);
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setMinimumSize(new Dimension(32, 32));
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
	
	private ColumnSelector getColumnSelector() {
		if (columnSelector == null) {
			columnSelector = new ColumnSelector(serviceRegistrar);
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

		final JMenuItem jMenuItemStringAttribute = new JMenuItem();
		jMenuItemStringAttribute.setText("String");
		jMenuItemStringAttribute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createNewAttribute("String", isShared);
			}
		});

		return jMenuItemStringAttribute;
	}

	private JMenuItem getJMenuItemIntegerAttribute(final boolean isShared) {
		final JMenuItem jMenuItemIntegerAttribute = new JMenuItem();
		jMenuItemIntegerAttribute.setText("Integer");
		jMenuItemIntegerAttribute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createNewAttribute("Integer", isShared);
			}
		});

		return jMenuItemIntegerAttribute;
	}

	private JMenuItem getJMenuItemLongIntegerAttribute(final boolean isShared) {
		final JMenuItem jMenuItemLongIntegerAttribute = new JMenuItem();
		jMenuItemLongIntegerAttribute.setText("Long Integer");
		jMenuItemLongIntegerAttribute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createNewAttribute("Long Integer", isShared);
			}
		});
		return jMenuItemLongIntegerAttribute;
	}

	private JMenuItem getJMenuItemFloatingPointAttribute(final boolean isShared) {
		final JMenuItem jMenuItemFloatingPointAttribute = new JMenuItem();
		jMenuItemFloatingPointAttribute.setText("Floating Point");
		jMenuItemFloatingPointAttribute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createNewAttribute("Floating Point", isShared);
			}
		});

		return jMenuItemFloatingPointAttribute;
	}

	private JMenuItem getJMenuItemBooleanAttribute(final boolean isShared) {
		final JMenuItem jMenuItemBooleanAttribute = new JMenuItem();
		jMenuItemBooleanAttribute.setText("Boolean");
		jMenuItemBooleanAttribute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createNewAttribute("Boolean", isShared);
			}
		});

		return jMenuItemBooleanAttribute;
	}

	private JMenuItem getJMenuItemStringListAttribute(final boolean isShared) {
		final JMenuItem jMenuItemStringListAttribute = new JMenuItem();
		jMenuItemStringListAttribute.setText("String");
		jMenuItemStringListAttribute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createNewAttribute("String List", isShared);
			}
		});

		return jMenuItemStringListAttribute;
	}

	private JMenuItem getJMenuItemIntegerListAttribute(final boolean isShared) {
		final JMenuItem jMenuItemIntegerListAttribute = new JMenuItem();
		jMenuItemIntegerListAttribute.setText("Integer");
		jMenuItemIntegerListAttribute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createNewAttribute("Integer List", isShared);
			}
		});

		return jMenuItemIntegerListAttribute;
	}

	private JMenuItem getJMenuItemLongIntegerListAttribute(final boolean isShared) {
		final JMenuItem jMenuItemLongIntegerListAttribute = new JMenuItem();
		jMenuItemLongIntegerListAttribute.setText("Long Integer");
		jMenuItemLongIntegerListAttribute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createNewAttribute("Long Integer List", isShared);
			}
		});

		return jMenuItemLongIntegerListAttribute;
	}

	private JMenuItem getJMenuItemFloatingPointListAttribute(final boolean isShared) {
		final JMenuItem jMenuItemFloatingPointListAttribute = new JMenuItem();
		jMenuItemFloatingPointListAttribute.setText("Floating Point");
		jMenuItemFloatingPointListAttribute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createNewAttribute("Floating Point List", isShared);
			}
		});

		return jMenuItemFloatingPointListAttribute;
	}

	private JMenuItem getJMenuItemBooleanListAttribute(final boolean isShared) {
		final JMenuItem jMenuItemBooleanListAttribute = new JMenuItem();
		jMenuItemBooleanListAttribute.setText("Boolean");
		jMenuItemBooleanListAttribute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createNewAttribute("Boolean List", isShared);
			}
		});

		return jMenuItemBooleanListAttribute;
	}

	private JToolBar getToolBar() {
		if (toolBar == null) {
			toolBar = new JToolBar();
			toolBar.setMargin(new Insets(0, 0, 3, 0));
			toolBar.setPreferredSize(TOOLBAR_SIZE);
			toolBar.setSize(TOOLBAR_SIZE);
			toolBar.setFloatable(false);
			toolBar.setOrientation(JToolBar.HORIZONTAL);
			toolBar.setOpaque(!isAquaLAF());

			final GroupLayout buttonBarLayout = new GroupLayout(toolBar);
			toolBar.setLayout(buttonBarLayout);
			hToolBarGroup = buttonBarLayout.createSequentialGroup();
			vToolBarGroup = buttonBarLayout.createParallelGroup(Alignment.CENTER, false);
			
			// Layout information.
			buttonBarLayout.setHorizontalGroup(buttonBarLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(hToolBarGroup));
			buttonBarLayout.setVerticalGroup(vToolBarGroup);

		}

		return toolBar;
	}

	private JButton getSelectButton() {
		if (selectButton == null) {
			selectButton = new JButton(ICON_COLUMNS);
			selectButton.setToolTipText("Show Columns");
			styleButton(selectButton, iconMgr.getIconFont(ICON_FONT_SIZE));

			selectButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (browserTableModel != null) {
						getColumnSelector().update(browserTableModel.getDataTable().getColumns(),
								browserTable.getVisibleAttributeNames());
						getColumnSelectorPopupMenu().pack();
						getColumnSelectorPopupMenu().show(selectButton, 0, selectButton.getHeight());
					}
				}
			});
		}
		
		return selectButton;
	}

	private JButton getFunctionBuilderButton() {
		if (formulaBuilderButton == null) {
			formulaBuilderButton = new JButton("f(x)");
			formulaBuilderButton.setToolTipText("Function Builder");
			
			Font iconFont = null;
			
			try {
				iconFont = Font.createFont(Font.TRUETYPE_FONT, 
						getClass().getResourceAsStream("/fonts/jsMath-cmti10.ttf"));
			} catch (Exception e) {
				throw new RuntimeException("Error loading font", e);
			}
			
			styleButton(formulaBuilderButton, iconFont.deriveFont(18.0f));

			final JFrame rootFrame = (JFrame) SwingUtilities.getRoot(this);

			formulaBuilderButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					// Do not allow opening of the formula builder dialog while a cell is being edited!
					if (browserTableModel == null || browserTable.getCellEditor() != null)
						return;

					final int cellRow = browserTable.getSelectedRow();
					final int cellColumn = browserTable.getSelectedColumn();
					int colIndex = -1;

					// Map the screen index of column to internal index of the table model
					if (cellRow >= 0 && cellColumn >= 0) {
						String colName = browserTable.getColumnName(cellColumn);
						colIndex = browserTableModel.mapColumnNameToColumnIndex(colName);
					}
					
					if (cellRow == -1 || cellColumn == -1 || !browserTableModel.isCellEditable(cellRow, colIndex)) {
						JOptionPane.showMessageDialog(rootFrame, "Can't enter a formula w/o a selected cell.",
								"Information", JOptionPane.INFORMATION_MESSAGE);
					} else {
						final String attrName = getAttribName(cellRow, cellColumn);
						final Map<String, Class<?>> attribNameToTypeMap = new HashMap<>();
						final CyTable dataTable = browserTableModel.getDataTable();
						initAttribNameToTypeMap(dataTable, attrName, attribNameToTypeMap);
						
						final EquationCompiler compiler = serviceRegistrar.getService(EquationCompiler.class);
						
						final FormulaBuilderDialog formulaBuilderDialog = new FormulaBuilderDialog(compiler,
								browserTable, rootFrame, attrName);
						formulaBuilderDialog.setLocationRelativeTo(rootFrame);
						formulaBuilderDialog.setVisible(true);
					}
				}

				private void initAttribNameToTypeMap(final CyTable dataTable, final String attrName,
						final Map<String, Class<?>> attribNameToTypeMap) {
					for (final CyColumn column : dataTable.getColumns())
						attribNameToTypeMap.put(column.getName(), column.getType());
					
					attribNameToTypeMap.remove(attrName);
				}
			});
		}
		
		return formulaBuilderButton;
	}

	private String getAttribName(final int cellRow, final int cellColumn) {
		int colIndexModel = browserTable.convertColumnIndexToModel(cellColumn);
		return browserTableModel.getColumnName( colIndexModel);
	}

	private JButton getDeleteButton() {
		if (deleteAttributeButton == null) {
			deleteAttributeButton = new JButton(ICON_TRASH_O);
			deleteAttributeButton.setToolTipText("Delete Columns...");
			styleButton(deleteAttributeButton, iconMgr.getIconFont(ICON_FONT_SIZE));
			
			// Create pop-up window for deletion
			deleteAttributeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					removeAttribute();
					updateEnableState();
				}
			});
		}

		return deleteAttributeButton;
	}

	private JButton getDeleteTableButton() {
		if (deleteTableButton == null) {
			deleteTableButton = new JButton(ICON_TABLE + "" + ICON_TIMES_CIRCLE);
			deleteTableButton.setToolTipText("Delete Table...");
			styleButton(deleteTableButton, iconMgr.getIconFont(ICON_FONT_SIZE / 2.0f));
			
			// Create pop-up window for deletion
			deleteTableButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					removeTable();
				}
			});
		}

		return deleteTableButton;
	}
	
	private void removeAttribute() {
		final JFrame frame = (JFrame)SwingUtilities.getRoot(this);
		final DeletionDialog dDialog = new DeletionDialog(frame, browserTableModel.getDataTable());

		dDialog.pack();
		dDialog.setLocationRelativeTo(toolBar);
		dDialog.setVisible(true);
	}

	private void removeTable() {
		final CyTable table = browserTableModel.getDataTable();

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
		final CyTable attrs = browserTableModel.getDataTable();
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

	private JButton getNewButton() {
		if (createNewAttributeButton == null) {
			createNewAttributeButton = new JButton(ICON_PLUS);
			createNewAttributeButton.setToolTipText("Create New Column");
			styleButton(createNewAttributeButton, iconMgr.getIconFont(ICON_FONT_SIZE));
			
			createNewAttributeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (browserTableModel != null)
						getCreateColumnMenu().show(createNewAttributeButton, 0, createNewAttributeButton.getHeight());
				}
			});
			
			createNewAttributeButton.setEnabled(false);
		}

		return createNewAttributeButton;
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

			mapGlobalTableButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (mapGlobalTableButton.isEnabled())
						guiTaskManagerServiceRef.execute(mapGlobalTableTaskFactoryService.createTaskIterator( browserTableModel.getAttributes() ));
				}
			});
		}
		mapGlobalTableButton.setEnabled(false);

		return mapGlobalTableButton;
	}
	*/
	
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
				final CyApplicationManager applicationMgr = serviceRegistrar.getService(CyApplicationManager.class);
				final CyNetwork network = applicationMgr.getCurrentNetwork();
							
				if (network instanceof CySubNetwork) {
					final CyRootNetwork rootNetwork = ((CySubNetwork) network).getRootNetwork();
					CyTable sharedTable = null;
					if(this.objType == CyNode.class)
						sharedTable = rootNetwork.getSharedNodeTable();
					else if(this.objType == CyEdge.class)
						sharedTable = rootNetwork.getSharedEdgeTable();
					else if(this.objType == CyNetwork.class)
						sharedTable = rootNetwork.getSharedNetworkTable();
					else {
						throw new IllegalStateException("Object type is not valid.  This should not happen.");
					}
					attrs = sharedTable;
				} else {
					throw new IllegalArgumentException("This is not a CySubNetwork and there is no shared table.");
				}
			} else {
				attrs = browserTableModel.getDataTable();
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
