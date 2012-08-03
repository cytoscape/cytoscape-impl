/*
 Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.browser.internal;


import java.awt.BorderLayout;
import java.awt.Dimension;
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

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.ImageIcon;
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
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.browser.internal.util.ColumnResizer;
import org.cytoscape.equations.EquationCompiler;
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
import org.cytoscape.util.swing.CheckBoxJList;
import org.cytoscape.work.swing.DialogTaskManager;


/**
 * Toolbar for the Browser.  All buttons related to this should be placed here.
 *
 */
public class AttributeBrowserToolBar extends JPanel implements PopupMenuListener {
	
	private static final long serialVersionUID = -508393701912596399L;

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

	
	public AttributeBrowserToolBar(final CyServiceRegistrar serviceRegistrar,
								   final EquationCompiler compiler,
								   final DeleteTableTaskFactory deleteTableTaskFactory,
								   final DialogTaskManager guiTaskMgr,
								   final JComboBox tableChooser,
								   final Class<? extends CyIdentifiable> objType,
								   final CyApplicationManager appMgr) {//, final MapGlobalToLocalTableTaskFactory mapGlobalTableTaskFactoryService) {
		
		this(serviceRegistrar, compiler, deleteTableTaskFactory, guiTaskMgr, tableChooser,
				new JButton(), objType, appMgr);//, mapGlobalTableTaskFactoryService);
		this.selectionModeButton.setVisible(false);
	}
	
	public AttributeBrowserToolBar(final CyServiceRegistrar serviceRegistrar,
								   final EquationCompiler compiler,
								   final DeleteTableTaskFactory deleteTableTaskFactory,
								   final DialogTaskManager guiTaskMgr,
								   final JComboBox tableChooser,
								   final JButton selectionModeButton,
								   final Class<? extends CyIdentifiable> objType,
								   final CyApplicationManager appMgr) {// , final MapGlobalToLocalTableTaskFactory mapGlobalTableTaskFactoryService) {
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
		
		serviceRegistrar.registerAllServices(attrListModel, new Properties());

		selectionModeButton.setEnabled(false);
		initializeGUI();
		
	}

	public void setBrowserTableModel(final BrowserTableModel browserTableModel) {
		this.browserTableModel = browserTableModel;
		attrListModel.setBrowserTableModel(browserTableModel);
		updateEnableState();
	}

	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		// Update actual table
		try {
			if (attributeList != null) {
				final Object[] selectedValues = attributeList.getSelectedValues();
				final Set<String> visibleAttributes = new HashSet<String>();
				for (final Object selectedValue : selectedValues)
					visibleAttributes.add((String)selectedValue);

				browserTableModel.setVisibleAttributeNames(visibleAttributes);
			}
		} catch (Exception ex) {
			attributeList.clearSelection();
		}
	}

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		// Do nothing
	}

	public void updateEnableState() {
		for (final JComponent comp : components) {
			boolean enabled = browserTableModel != null;
			
			if (comp == deleteTableButton /*|| comp == mapGlobalTableButton*/)
				enabled &= objType == null;
			
			comp.setEnabled(enabled);
		}
	}
	
	protected void addComponent(final JComponent component, final ComponentPlacement placement) {
		hToolBarGroup.addPreferredGap(placement).addComponent(component);
		vToolBarGroup.addComponent(component, Alignment.CENTER, GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE);
		components.add(component);
	}
	
	private void initializeGUI() {
		setLayout(new BorderLayout());
		add(getToolBar(), BorderLayout.CENTER);

		getAttributeSelectionPopupMenu();
		getJPopupMenu();
		
		// Add buttons
		if (selectionModeButton != null)
			addComponent(selectionModeButton, ComponentPlacement.RELATED);
		
		addComponent(getSelectButton(), ComponentPlacement.UNRELATED);
		addComponent(getSelectAllButton(), ComponentPlacement.RELATED);
		addComponent(getUnselectAllButton(), ComponentPlacement.RELATED);
		addComponent(getNewButton(), ComponentPlacement.UNRELATED);
		addComponent(getDeleteButton(), ComponentPlacement.RELATED);
		addComponent(getDeleteTableButton(), ComponentPlacement.RELATED);
		addComponent(getFunctionBuilderButton(), ComponentPlacement.UNRELATED);
//		addComponent(getMapGlobalTableButton(). ComponentPlacement.UNRELATED);
		
		if (tableChooser != null)
			addComponent(tableChooser, ComponentPlacement.UNRELATED);
	}

	
	public String getToBeDeletedAttribute() {
		return attrDeletionList.getSelectedValue().toString();
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
			jScrollPane.setViewportView(getSelectedAttributeList());
		}

		return jScrollPane;
	}

	/**
	 * This method initializes jPopupMenu
	 *
	 * @return javax.swing.JPopupMenu
	 */
	private JPopupMenu getJPopupMenu() {
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
				createNewAttribute("Integer List", isShared);
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
			selectButton = new JButton();
			selectButton.setBorder(null);
			selectButton.setMargin(new Insets(0, 0, 0, 0));
			selectButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("images/table-select-column-icon.png")));
			selectButton.setToolTipText("Show Column");
			selectButton.setBorder(null);
			selectButton.setEnabled(false);

			selectButton.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (browserTableModel == null)
						return;
					attributeList.setSelectedItems(browserTableModel.getVisibleAttributeNames());
					attributeSelectionPopupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			});
		}
		
		return selectButton;
	}

	private JButton getFunctionBuilderButton() {
		if (formulaBuilderButton == null) {
			formulaBuilderButton = new JButton();
			formulaBuilderButton.setBorder(null);
			formulaBuilderButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("images/fx.png")));
			formulaBuilderButton.setToolTipText("Function Builder");
			formulaBuilderButton.setMargin(new Insets(1, 1, 1, 1));
			formulaBuilderButton.setBorder(null);
			formulaBuilderButton.setEnabled(false);

			final JFrame rootFrame = (JFrame) SwingUtilities.getRoot(this);

			formulaBuilderButton.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (browserTableModel == null)
						return;

					final JTable table = browserTableModel.getTable();

					// Do not allow opening of the formula builder dialog
					// while a cell is being edited!
					if (table.getCellEditor() != null)
						return;

					final int cellRow = table.getSelectedRow();
					final int cellColumn = table.getSelectedColumn();

					if (cellRow == -1 || cellColumn == -1 || !browserTableModel.isCellEditable(cellRow, cellColumn)) {
						JOptionPane.showMessageDialog(rootFrame, "Can't enter a formula w/o a selected cell.",
								"Information", JOptionPane.INFORMATION_MESSAGE);
					} else {
						final String attrName = getAttribName(cellRow, cellColumn);
						final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
						final CyTable attrs = browserTableModel.getAttributes();
						initAttribNameToTypeMap(attrs, attrName, attribNameToTypeMap);
						final FormulaBuilderDialog formulaBuilderDialog = new FormulaBuilderDialog(compiler,
								browserTableModel, rootFrame, attrName);
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
		return browserTableModel.getColumnName(cellColumn);
	}

	private JButton getDeleteButton() {
		if (deleteAttributeButton == null) {
			deleteAttributeButton = new JButton();
			deleteAttributeButton.setBorder(null);
			deleteAttributeButton.setMargin(new Insets(0, 0, 0, 0));
			deleteAttributeButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("images/stock_delete.png")));
			deleteAttributeButton.setToolTipText("Delete Column...");
			deleteAttributeButton.setBorder(null);
			// Create pop-up window for deletion
			deleteAttributeButton.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (deleteAttributeButton.isEnabled())
						removeAttribute(e);
				}
			});
			deleteAttributeButton.setEnabled(false);
		}

		return deleteAttributeButton;
	}

	private JButton getDeleteTableButton() {
		if (deleteTableButton == null) {
			deleteTableButton = new JButton();
			deleteTableButton.setBorder(null);
			deleteTableButton.setMargin(new Insets(0, 0, 0, 0));
			deleteTableButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("images/table_delete.png")));
			deleteTableButton.setToolTipText("Delete Table...");
			deleteTableButton.setBorder(null);
			// Create pop-up window for deletion
			deleteTableButton.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (deleteTableButton.isEnabled())
						removeTable(e);
				}
			});
			deleteTableButton.setEnabled(false);
		}

		return deleteTableButton;
	}

	
	private JButton getSelectAllButton() {
		if (selectAllAttributesButton == null) {
			selectAllAttributesButton = new JButton();
			selectAllAttributesButton.setBorder(null);
			selectAllAttributesButton.setMargin(new Insets(0, 0, 0, 0));
			selectAllAttributesButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("images/select_all.png")));
			selectAllAttributesButton.setToolTipText("Show All Columns");
			selectAllAttributesButton.setBorder(null);
			selectAllAttributesButton.setEnabled(false);

			selectAllAttributesButton.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					try {
						final CyTable table = browserTableModel.getAttributes();
						final Set<String> allAttrNames = new HashSet<String>();
						for (final CyColumn column : table.getColumns())
							allAttrNames.add(column.getName());
						browserTableModel.setVisibleAttributeNames(allAttrNames);

						// ***DO NOT *** Resize column
						//ColumnResizer.adjustColumnPreferredWidths(browserTableModel.getTable());
					} catch (Exception ex) {
						attributeList.clearSelection();
					}
				}
			});
		}

		return selectAllAttributesButton;
	}

	private JButton getUnselectAllButton() {
		if (unselectAllAttributesButton == null) {
			unselectAllAttributesButton = new JButton();
			unselectAllAttributesButton.setBorder(null);
			unselectAllAttributesButton.setMargin(new Insets(0, 0, 0, 0));
			unselectAllAttributesButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("images/unselect_all.png")));
			unselectAllAttributesButton.setToolTipText("Hide All Columns");
			unselectAllAttributesButton.setBorder(null);
			unselectAllAttributesButton.setEnabled(false);

			unselectAllAttributesButton.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					try {
						browserTableModel.setVisibleAttributeNames(new HashSet<String>());
					} catch (Exception ex) {
						attributeList.clearSelection();
					}
				}
			});
		}

		return unselectAllAttributesButton;
	}

	private void removeAttribute(final MouseEvent e) {
		final String[] attrArray = getAttributeArray();

		final JFrame frame = (JFrame)SwingUtilities.getRoot(this);
		final DeletionDialog dDialog = new DeletionDialog(frame, browserTableModel.getAttributes(), browserTableModel);

		dDialog.pack();
		dDialog.setLocationRelativeTo(toolBar);
		dDialog.setVisible(true);
	}

	private void removeTable(final MouseEvent e) {
		final CyTable table = browserTableModel.getAttributes();

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

	
	private JList getSelectedAttributeList() {
		if (attributeList == null) {
			attributeList = new CheckBoxJList();
			attributeList.setModel(attrListModel);
			attributeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			attributeList.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (SwingUtilities.isRightMouseButton(e)) {
						attributeSelectionPopupMenu.setVisible(false);
					}
				}
			});
		}

		return attributeList;
	}

	private String[] getAttributeArray() {
		final CyTable attrs = browserTableModel.getAttributes();
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
			createNewAttributeButton = new JButton();
			createNewAttributeButton.setBorder(null);

			createNewAttributeButton.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			createNewAttributeButton.setHorizontalTextPosition(SwingConstants.CENTER);
			createNewAttributeButton.setMargin(new Insets(0, 0, 0, 0));
			createNewAttributeButton.setToolTipText("Create New Column");
			createNewAttributeButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("images/stock_new.png")));
			createNewAttributeButton.setBorder(null);
			
			createNewAttributeButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (browserTableModel != null)
						createColumnMenu.show(e.getComponent(), e.getX(), e.getY());
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

			mapGlobalTableButton.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
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
		
		final String[] existingAttrs = getAttributeArray();
		String newAttribName = null;
		do {
			newAttribName = JOptionPane.showInputDialog(this, "Please enter new column name: ",
								    "Create New " + type + " Column",
								    JOptionPane.QUESTION_MESSAGE);
			if (newAttribName == null)
				return;

			if (Arrays.binarySearch(existingAttrs, newAttribName) >= 0) {
				newAttribName = null;
				JOptionPane.showMessageDialog(null,
							      "Column " + newAttribName + " already exists.",
							      "Error.", JOptionPane.ERROR_MESSAGE);
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
			attrs = browserTableModel.getAttributes();
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
}
