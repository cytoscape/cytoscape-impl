package org.cytoscape.browser.internal;

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

import static org.cytoscape.browser.internal.IconManager.ICON_CHECK;
import static org.cytoscape.browser.internal.IconManager.ICON_CHECK_EMPTY;
import static org.cytoscape.browser.internal.IconManager.ICON_COLUMNS;
import static org.cytoscape.browser.internal.IconManager.ICON_FILE_ALT;
import static org.cytoscape.browser.internal.IconManager.ICON_REMOVE_SIGN;
import static org.cytoscape.browser.internal.IconManager.ICON_TABLE;
import static org.cytoscape.browser.internal.IconManager.ICON_TRASH;
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
import java.util.HashSet;
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
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
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
import org.cytoscape.util.swing.CheckBoxJList;
import org.cytoscape.work.swing.DialogTaskManager;


/**
 * Toolbar for the Browser.  All buttons related to this should be placed here.
 *
 */
public class AttributeBrowserToolBar extends JPanel implements PopupMenuListener {
	
	private static final long serialVersionUID = -508393701912596399L;

	public static final float ICON_FONT_SIZE = 22.0f;
	
	private BrowserTable browserTable;
	private BrowserTableModel browserTableModel;
	
	private static final Dimension TOOLBAR_SIZE = new Dimension(500, 38);

	/* GUI components */
	private JPopupMenu attributeSelectionPopupMenu;
	private JScrollPane jScrollPane;
	private JPopupMenu createColumnMenu;

	private JToolBar toolBar;
	private SequentialGroup hToolBarGroup;
	private ParallelGroup vToolBarGroup;
	
	private JButton selectButton;
	private CheckBoxJList attributeList;
	private JList attrDeletionList;
	
	private JButton createNewAttributeButton;
	private JButton deleteAttributeButton;
	private JButton deleteTableButton;
	private JButton selectAllAttributesButton;
	private JButton unselectAllAttributesButton;
	private JButton formulaBuilderButton;
	
//	private JButton mapGlobalTableButton;
//	private final MapGlobalToLocalTableTaskFactory mapGlobalTableTaskFactoryService;
	
	private final JComboBox tableChooser;

	private AttributeListModel attrListModel;
	private final EquationCompiler compiler;
	private final DeleteTableTaskFactory deleteTableTaskFactory;
	private final DialogTaskManager guiTaskMgr;
	
	private final JButton selectionModeButton;
	
	private final List<JComponent> components;
	
	private final Class<? extends CyIdentifiable> objType;
	
	private final CyApplicationManager appMgr;
	private final IconManager iconMgr;
	

	public AttributeBrowserToolBar(final CyServiceRegistrar serviceRegistrar,
								   final EquationCompiler compiler,
								   final DeleteTableTaskFactory deleteTableTaskFactory,
								   final DialogTaskManager guiTaskMgr,
								   final JComboBox tableChooser,
								   final Class<? extends CyIdentifiable> objType,
								   final CyApplicationManager appMgr,
								   final IconManager iconMgr) {//, final MapGlobalToLocalTableTaskFactory mapGlobalTableTaskFactoryService) {
		
		this(serviceRegistrar, compiler, deleteTableTaskFactory, guiTaskMgr, tableChooser,
				new JButton(), objType, appMgr, iconMgr);//, mapGlobalTableTaskFactoryService);
		this.selectionModeButton.setVisible(false);
	}
	
	public AttributeBrowserToolBar(final CyServiceRegistrar serviceRegistrar,
								   final EquationCompiler compiler,
								   final DeleteTableTaskFactory deleteTableTaskFactory,
								   final DialogTaskManager guiTaskMgr,
								   final JComboBox tableChooser,
								   final JButton selectionModeButton,
								   final Class<? extends CyIdentifiable> objType,
								   final CyApplicationManager appMgr,
								   final IconManager iconMgr) {// , final MapGlobalToLocalTableTaskFactory mapGlobalTableTaskFactoryService) {
		this.compiler = compiler;
		this.selectionModeButton = selectionModeButton;
		this.appMgr = appMgr;
//		this.mapGlobalTableTaskFactoryService = mapGlobalTableTaskFactoryService;
		
		this.components = new ArrayList<JComponent>();
		
		this.tableChooser = tableChooser;
		this.deleteTableTaskFactory = deleteTableTaskFactory;
		this.guiTaskMgr = guiTaskMgr;
		this.attrListModel = new AttributeListModel(null);
		this.objType = objType;
		this.iconMgr = iconMgr;
		
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
		// TODO Auto-generated method stub
	}
	
	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		// Update actual table
		try {
			final Object[] selectedValues = getAttributeList().getSelectedValues();
			final Set<String> visibleAttributes = new HashSet<String>();
			for (final Object selectedValue : selectedValues)
				visibleAttributes.add((String)selectedValue);

			browserTable.setVisibleAttributeNames(visibleAttributes);
		} catch (Exception ex) {
			getAttributeList().clearSelection();
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
		
		boolean enabled = browserTableModel != null;
		
		if (enabled) {
			if (comp == deleteTableButton) {
				enabled = browserTableModel.getDataTable().getMutability() == Mutability.MUTABLE;
			} else if (comp == deleteAttributeButton) {
				final CyTable attrs = browserTableModel.getDataTable();
				
				for (final CyColumn column : attrs.getColumns()) {
					enabled = !column.isImmutable();
					
					if (enabled)
						break;
				}
			} else if (comp == formulaBuilderButton) {
				final int row = browserTable.getSelectedRow();
				final int column = browserTable.getSelectedColumn();
				enabled = row >=0 && column >= 0 && browserTableModel.isCellEditable(row, column);
			} else if (comp == tableChooser) {
				enabled = tableChooser.getItemCount() > 0;
			}
		}
		
		comp.setEnabled(enabled);
	}
	
	protected void addComponent(final JComponent component, final ComponentPlacement placement) {
		if (placement != null)
			hToolBarGroup.addPreferredGap(placement);
		
		hToolBarGroup.addComponent(component);
		vToolBarGroup.addComponent(component, Alignment.CENTER, GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE);
		components.add(component);
	}
	
	private void initializeGUI() {
		setLayout(new BorderLayout());
		setOpaque(!isAquaLAF());
		add(getToolBar(), BorderLayout.CENTER);

		// Add buttons
		if (selectionModeButton != null)
			addComponent(selectionModeButton, ComponentPlacement.RELATED);
		
		addComponent(getSelectButton(), ComponentPlacement.RELATED);
		addComponent(getSelectAllButton(), ComponentPlacement.RELATED);
		addComponent(getUnselectAllButton(), ComponentPlacement.RELATED);
		addComponent(getNewButton(), ComponentPlacement.RELATED);
		addComponent(getDeleteButton(), ComponentPlacement.RELATED);
		addComponent(getDeleteTableButton(), ComponentPlacement.RELATED);
		addComponent(getFunctionBuilderButton(), ComponentPlacement.RELATED);
//		addComponent(getMapGlobalTableButton(). ComponentPlacement.RELATED);
		
		if (tableChooser != null)
			addComponent(tableChooser, ComponentPlacement.UNRELATED);
	}

	public String getToBeDeletedAttribute() {
		return attrDeletionList.getSelectedValue().toString();
	}

	static void styleButton(final AbstractButton btn, final Font font) {
		btn.setFont(font);
		btn.setBorder(null);
		btn.setEnabled(false);
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setMinimumSize(new Dimension(32, 32));
	}
	
	/**
	 * This method initializes jPopupMenu
	 *
	 * @return javax.swing.JPopupMenu
	 */
	private JPopupMenu getAttributeSelectionPopupMenu() {
		if (attributeSelectionPopupMenu == null) {
			attributeSelectionPopupMenu = new JPopupMenu();
			attributeSelectionPopupMenu.add(getJScrollPane());
			attributeSelectionPopupMenu.addPopupMenuListener(this);
		}

		return attributeSelectionPopupMenu;
	}

	/**
	 * This method initializes jScrollPane
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setPreferredSize(new Dimension(250, 200));
			jScrollPane.setViewportView(getAttributeList());
		}

		return jScrollPane;
	}

	/**
	 * This method initializes jPopupMenu
	 *
	 * @return javax.swing.JPopupMenu
	 */
	private JPopupMenu getCreateColumnMenu() {
		if (createColumnMenu == null) {
			createColumnMenu = new JPopupMenu();
			//final JMenu column = new JMenu("New Column");
			
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
			
			//column.add(columnRegular);
			//column.add(columnList);
			
			createColumnMenu.add(columnRegular);
			createColumnMenu.add(columnList);
			
			/*
			// This is not valid for Global Table.
			if (objType != null) {
				final JMenu shared = new JMenu("New Shared Column");
				
				final JMenu sharedRegular = new JMenu("Single");
				final JMenu sharedList = new JMenu("List");
				
				sharedRegular.add(getJMenuItemIntegerAttribute(true));
				sharedRegular.add(getJMenuItemLongIntegerAttribute(true));
				sharedRegular.add(getJMenuItemStringAttribute(true));
				sharedRegular.add(getJMenuItemFloatingPointAttribute(true));
				sharedRegular.add(getJMenuItemBooleanAttribute(true));
				sharedList.add(getJMenuItemIntegerListAttribute(true));
				sharedList.add(getJMenuItemLongIntegerListAttribute(true));
				sharedList.add(getJMenuItemStringListAttribute(true));
				sharedList.add(getJMenuItemFloatingPointListAttribute(true));
				sharedList.add(getJMenuItemBooleanListAttribute(true));
				
				shared.add(sharedRegular);
				shared.add(sharedList);
				
				createColumnMenu.add(shared);
			
			}
			*/
		}

		return createColumnMenu;
	}

	/**
	 * This method initializes jMenuItemStringAttribute
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemStringAttribute(final boolean isShared) {

		final JMenuItem jMenuItemStringAttribute = new JMenuItem();
		jMenuItemStringAttribute.setText("String");
		jMenuItemStringAttribute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createNewAttribute("String", isShared);
			}
		});

		return jMenuItemStringAttribute;
	}

	/**
	 * This method initializes jMenuItemIntegerAttribute
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemIntegerAttribute(final boolean isShared) {
		final JMenuItem jMenuItemIntegerAttribute = new JMenuItem();
		jMenuItemIntegerAttribute.setText("Integer");
		jMenuItemIntegerAttribute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createNewAttribute("Integer", isShared);
			}
		});

		return jMenuItemIntegerAttribute;
	}

	/**
	 * This method initializes jMenuItemLongIntegerAttribute
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemLongIntegerAttribute(final boolean isShared) {
		final JMenuItem jMenuItemLongIntegerAttribute = new JMenuItem();
		jMenuItemLongIntegerAttribute.setText("Long Integer");
		jMenuItemLongIntegerAttribute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createNewAttribute("Long Integer", isShared);
			}
		});
		return jMenuItemLongIntegerAttribute;
	}

	/**
	 * This method initializes jMenuItemFloatingPointAttribute
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemFloatingPointAttribute(final boolean isShared) {
		final JMenuItem jMenuItemFloatingPointAttribute = new JMenuItem();
		jMenuItemFloatingPointAttribute.setText("Floating Point");
		jMenuItemFloatingPointAttribute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createNewAttribute("Floating Point", isShared);
			}
		});

		return jMenuItemFloatingPointAttribute;
	}

	/**
	 * This method initializes jMenuItemBooleanAttribute
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemBooleanAttribute(final boolean isShared) {
		final JMenuItem jMenuItemBooleanAttribute = new JMenuItem();
		jMenuItemBooleanAttribute.setText("Boolean");
		jMenuItemBooleanAttribute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createNewAttribute("Boolean", isShared);
			}
		});

		return jMenuItemBooleanAttribute;
	}

	/**
	 * This method initializes jMenuItemStringListAttribute
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemStringListAttribute(final boolean isShared) {
		final JMenuItem jMenuItemStringListAttribute = new JMenuItem();
		jMenuItemStringListAttribute.setText("String");
		jMenuItemStringListAttribute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createNewAttribute("String List", isShared);
			}
		});

		return jMenuItemStringListAttribute;
	}

	/**
	 * This method initializes jMenuItemIntegerListAttribute
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemIntegerListAttribute(final boolean isShared) {
		final JMenuItem jMenuItemIntegerListAttribute = new JMenuItem();
		jMenuItemIntegerListAttribute.setText("Integer");
		jMenuItemIntegerListAttribute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createNewAttribute("Integer List", isShared);
			}
		});

		return jMenuItemIntegerListAttribute;
	}

	/**
	 * This method initializes jMenuItemLongIntegerListAttribute
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemLongIntegerListAttribute(final boolean isShared) {
		final JMenuItem jMenuItemLongIntegerListAttribute = new JMenuItem();
		jMenuItemLongIntegerListAttribute.setText("Long Integer");
		jMenuItemLongIntegerListAttribute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createNewAttribute("Long Integer List", isShared);
			}
		});

		return jMenuItemLongIntegerListAttribute;
	}

	/**
	 * This method initializes jMenuItemFloatingPointListAttribute
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemFloatingPointListAttribute(final boolean isShared) {
		final JMenuItem jMenuItemFloatingPointListAttribute = new JMenuItem();
		jMenuItemFloatingPointListAttribute.setText("Floating Point");
		jMenuItemFloatingPointListAttribute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createNewAttribute("Floating Point List", isShared);
			}
		});

		return jMenuItemFloatingPointListAttribute;
	}

	/**
	 * This method initializes jMenuItemBooleanListAttribute
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemBooleanListAttribute(final boolean isShared) {
		final JMenuItem jMenuItemBooleanListAttribute = new JMenuItem();
		jMenuItemBooleanListAttribute.setText("Boolean");
		jMenuItemBooleanListAttribute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createNewAttribute("Boolean List", isShared);
			}
		});

		return jMenuItemBooleanListAttribute;
	}

	/**
	 * This method initializes  the toolBar
	 *
	 * @return javax.swing.JToolBar
	 */
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
			vToolBarGroup = buttonBarLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
			
			// Layout information.
			buttonBarLayout.setHorizontalGroup(buttonBarLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addGroup(hToolBarGroup));
			buttonBarLayout.setVerticalGroup(vToolBarGroup);

		}

		return toolBar;
	}

	/**
	 * This method initializes jButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getSelectButton() {
		if (selectButton == null) {
			selectButton = new JButton(ICON_COLUMNS);
			selectButton.setToolTipText("Show Column");
			styleButton(selectButton, iconMgr.getIconFont(ICON_FONT_SIZE));

			selectButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (browserTableModel == null)
						return;
					getAttributeList().setSelectedItems(browserTable.getVisibleAttributeNames());
					getAttributeSelectionPopupMenu().show(selectButton, 0, selectButton.getHeight());
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
					if (cellRow >=0 && cellColumn >=0) {
						String colName = browserTable.getColumnName(cellColumn);
						colIndex = browserTableModel.mapColumnNameToColumnIndex(colName);
					}
					
					if (cellRow == -1 || cellColumn == -1 || !browserTableModel.isCellEditable(cellRow, colIndex)) {
						JOptionPane.showMessageDialog(rootFrame, "Can't enter a formula w/o a selected cell.",
								"Information", JOptionPane.INFORMATION_MESSAGE);
					} else {
						final String attrName = getAttribName(cellRow, cellColumn);
						final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
						final CyTable attrs = browserTableModel.getDataTable();
						initAttribNameToTypeMap(attrs, attrName, attribNameToTypeMap);
						final FormulaBuilderDialog formulaBuilderDialog = new FormulaBuilderDialog(compiler,
								browserTable, rootFrame, attrName);
						formulaBuilderDialog.setLocationRelativeTo(rootFrame);
						formulaBuilderDialog.setVisible(true);
					}
				}

				private void initAttribNameToTypeMap(final CyTable attrs, final String attrName,
						final Map<String, Class<?>> attribNameToTypeMap) {
					for (final CyColumn column : attrs.getColumns())
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
			deleteAttributeButton = new JButton(ICON_TRASH);
			deleteAttributeButton.setToolTipText("Delete Columns...");
			styleButton(deleteAttributeButton, iconMgr.getIconFont(ICON_FONT_SIZE));
			
			// Create pop-up window for deletion
			deleteAttributeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					removeAttribute();
				}
			});
			deleteAttributeButton.setEnabled(false);
		}

		return deleteAttributeButton;
	}

	private JButton getDeleteTableButton() {
		if (deleteTableButton == null) {
			deleteTableButton = new JButton(ICON_TABLE + "" + ICON_REMOVE_SIGN);
			deleteTableButton.setToolTipText("Delete Table...");
			styleButton(deleteTableButton, iconMgr.getIconFont(ICON_FONT_SIZE / 2.0f));
			
			// Create pop-up window for deletion
			deleteTableButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					removeTable();
				}
			});
			deleteTableButton.setEnabled(false);
		}

		return deleteTableButton;
	}

	
	private JButton getSelectAllButton() {
		if (selectAllAttributesButton == null) {
			selectAllAttributesButton = new JButton(ICON_CHECK + " " + ICON_CHECK);
			selectAllAttributesButton.setToolTipText("Show All Columns");
			styleButton(selectAllAttributesButton, iconMgr.getIconFont(ICON_FONT_SIZE / 2.0f));

			selectAllAttributesButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					try {
						final CyTable table = browserTableModel.getDataTable();
						final Set<String> allAttrNames = new HashSet<String>();
						for (final CyColumn column : table.getColumns())
							allAttrNames.add(column.getName());
						browserTable.setVisibleAttributeNames(allAttrNames);

						// ***DO NOT *** Resize column
						//ColumnResizer.adjustColumnPreferredWidths(browserTableModel.getTable());
					} catch (Exception ex) {
						getAttributeList().clearSelection();
					}
				}
			});
		}

		return selectAllAttributesButton;
	}

	private JButton getUnselectAllButton() {
		if (unselectAllAttributesButton == null) {
			unselectAllAttributesButton = new JButton(ICON_CHECK_EMPTY + " " + ICON_CHECK_EMPTY);
			unselectAllAttributesButton.setToolTipText("Hide All Columns");
			styleButton(unselectAllAttributesButton, iconMgr.getIconFont(ICON_FONT_SIZE / 2.0f));

			unselectAllAttributesButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					try {
						browserTable.setVisibleAttributeNames(new HashSet<String>());
					} catch (Exception ex) {
						getAttributeList().clearSelection();
					}
				}
			});
		}

		return unselectAllAttributesButton;
	}

	private void removeAttribute() {
		final JFrame frame = (JFrame)SwingUtilities.getRoot(this);
		final DeletionDialog dDialog = new DeletionDialog(frame, browserTableModel.getDataTable(), browserTable);

		dDialog.pack();
		dDialog.setLocationRelativeTo(toolBar);
		dDialog.setVisible(true);
	}

	private void removeTable() {
		final CyTable table = browserTableModel.getDataTable();

		if (table.getMutability() == CyTable.Mutability.MUTABLE) {
			String title = "Please confirm this action";
			String msg = "Are you sure you want to delete this table?";
			int _confirmValue = JOptionPane.showConfirmDialog(this, msg, title, JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);

			// if user selects yes delete the table
			if (_confirmValue == JOptionPane.OK_OPTION)
				guiTaskMgr.execute(deleteTableTaskFactory.createTaskIterator(table));
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

	
	private CheckBoxJList getAttributeList() {
		if (attributeList == null) {
			attributeList = new CheckBoxJList();
			attributeList.setModel(attrListModel);
			attributeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			attributeList.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (SwingUtilities.isRightMouseButton(e)) {
						getAttributeSelectionPopupMenu().setVisible(false);
					}
				}
			});
		}

		return attributeList;
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

	/**
	 * This method initializes createNewAttributeButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getNewButton() {
		if (createNewAttributeButton == null) {
			createNewAttributeButton = new JButton(ICON_FILE_ALT);
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
				newAttribName = JOptionPane.showInputDialog(this, "Please enter new column name: ",
									    "Create New " + type + " Column",
									    JOptionPane.QUESTION_MESSAGE);
				if (newAttribName == null)
					return;
	
				if (Arrays.binarySearch(existingAttrs, newAttribName) >= 0) {
					JOptionPane.showMessageDialog(null,
								      "Column " + newAttribName + " already exists.",
								      "Error", JOptionPane.ERROR_MESSAGE);
					newAttribName = null;
				}
			} while (newAttribName == null);
	
			final CyTable attrs;
			if(isShared) {
				final CyNetwork network = appMgr.getCurrentNetwork();
							
				if(network instanceof CySubNetwork) {
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
		}
		catch(IllegalArgumentException e) {
			JOptionPane.showMessageDialog(null,
				      e.getMessage(),
				      "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
