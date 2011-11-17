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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
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
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.util.swing.CheckBoxJList;
import org.cytoscape.work.swing.DialogTaskManager;


/**
 * Toolbar for the Browser.  All buttons related to this should be placed here.
 *
 */
public class AttributeBrowserToolBar extends JPanel implements PopupMenuListener {
	
	private static final long serialVersionUID = -508393701912596399L;

	private BrowserTableModel browserTableModel = null;
	
	private static final Dimension TOOLBAR_SIZE = new Dimension(500, 38);

	/**
	 *  GUI components
	 */
	private JPopupMenu attributeSelectionPopupMenu = null;
	private JScrollPane jScrollPane = null;
	private JPopupMenu jPopupMenu = null;
	private JMenuItem jMenuItemStringAttribute = null;
	private JMenuItem jMenuItemIntegerAttribute = null;
	private JMenuItem jMenuItemLongIntegerAttribute = null;
	private JMenuItem jMenuItemFloatingPointAttribute = null;
	private JMenuItem jMenuItemBooleanAttribute = null;
	private JMenuItem jMenuItemStringListAttribute = null;
	private JMenuItem jMenuItemIntegerListAttribute = null;
	private JMenuItem jMenuItemLongIntegerListAttribute = null;
	private JMenuItem jMenuItemFloatingPointListAttribute = null;
	private JMenuItem jMenuItemBooleanListAttribute = null;
	private JToolBar browserToolBar = null;
	private JButton selectButton = null;
	private CheckBoxJList attributeList = null;
	private JList attrDeletionList = null;
	private JButton createNewAttributeButton = null;
	private JButton deleteAttributeButton = null;
	private JButton deleteTableButton = null;
	private JButton selectAllAttributesButton = null;
	private JButton unselectAllAttributesButton = null;
	
	private final JComboBox tableChooser;

	private AttributeListModel attrListModel;
	private final EquationCompiler compiler;
	private final TableTaskFactory deleteTableTaskFactoryService;
	private final DialogTaskManager guiTaskManagerServiceRef;
	
	private final JToggleButton selectionModeButton;
	
	private final Class<? extends CyTableEntry> objType;
	
	public AttributeBrowserToolBar(final CyServiceRegistrar serviceRegistrar, final EquationCompiler compiler,
			final TableTaskFactory deleteTableTaskFactoryService, DialogTaskManager guiTaskManagerServiceRef,
			final JComboBox tableChooser, final Class<? extends CyTableEntry> objType) {
		this(serviceRegistrar, compiler, deleteTableTaskFactoryService, guiTaskManagerServiceRef, tableChooser,
				new JToggleButton(), objType);
	}
	
	public AttributeBrowserToolBar(final CyServiceRegistrar serviceRegistrar, final EquationCompiler compiler,
			final TableTaskFactory deleteTableTaskFactoryService, DialogTaskManager guiTaskManagerServiceRef,
			final JComboBox tableChooser, final JToggleButton selectionModeButton, Class<? extends CyTableEntry> objType) {
		this.compiler = compiler;
		this.selectionModeButton = selectionModeButton;

		this.tableChooser = tableChooser;
		this.deleteTableTaskFactoryService = deleteTableTaskFactoryService;
		this.guiTaskManagerServiceRef = guiTaskManagerServiceRef;
		this.attrListModel = new AttributeListModel(null);
		this.objType = objType;
		
		serviceRegistrar.registerAllServices(attrListModel, new Properties());

		selectionModeButton.setEnabled(false);
		initializeGUI();
	}

	public void setBrowserTableModel(final BrowserTableModel browserTableModel) {
		this.browserTableModel = browserTableModel;
		
		attrListModel.setBrowserTableModel(browserTableModel);
		selectButton.setEnabled(browserTableModel != null);
		selectAllAttributesButton.setEnabled(browserTableModel != null);
		unselectAllAttributesButton.setEnabled(browserTableModel != null);
		createNewAttributeButton.setEnabled(browserTableModel != null);
		deleteAttributeButton.setEnabled(browserTableModel != null);
		
		if(browserTableModel != null && objType != null) {
			deleteTableButton.setEnabled(false);
		} else {
			deleteTableButton.setEnabled(browserTableModel != null);
		}
		
		formulaBuilderButton.setEnabled(browserTableModel != null);
		selectionModeButton.setEnabled(browserTableModel != null);
	}

	private void initializeGUI() {
		this.setLayout(new BorderLayout());

		this.add(getJToolBar(), java.awt.BorderLayout.CENTER);

		getAttributeSelectionPopupMenu();
		getJPopupMenu();
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
			jScrollPane.setPreferredSize(new Dimension(600, 300));
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
		if (jPopupMenu == null) {
			jPopupMenu = new JPopupMenu();
			jPopupMenu.add(getJMenuItemIntegerAttribute());
			jPopupMenu.add(getJMenuItemLongIntegerAttribute());
			jPopupMenu.add(getJMenuItemStringAttribute());
			jPopupMenu.add(getJMenuItemFloatingPointAttribute());
			jPopupMenu.add(getJMenuItemBooleanAttribute());
			jPopupMenu.add(getJMenuItemIntegerListAttribute());
			jPopupMenu.add(getJMenuItemLongIntegerListAttribute());
			jPopupMenu.add(getJMenuItemStringListAttribute());
			jPopupMenu.add(getJMenuItemFloatingPointListAttribute());
			jPopupMenu.add(getJMenuItemBooleanListAttribute());
		}

		return jPopupMenu;
	}

	/**
	 * This method initializes jMenuItemStringAttribute
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemStringAttribute() {
		if (jMenuItemStringAttribute == null) {
			jMenuItemStringAttribute = new JMenuItem();
			jMenuItemStringAttribute.setText("String Attribute");
			jMenuItemStringAttribute.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						createNewAttribute("String");
					}
				});
		}

		return jMenuItemStringAttribute;
	}

	/**
	 * This method initializes jMenuItemIntegerAttribute
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemIntegerAttribute() {
		if (jMenuItemIntegerAttribute == null) {
			jMenuItemIntegerAttribute = new JMenuItem();
			jMenuItemIntegerAttribute.setText("Integer Attribute");
			jMenuItemIntegerAttribute.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						createNewAttribute("Integer");
					}
				});
		}

		return jMenuItemIntegerAttribute;
	}

	/**
	 * This method initializes jMenuItemLongIntegerAttribute
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemLongIntegerAttribute() {
		if (jMenuItemLongIntegerAttribute == null) {
			jMenuItemLongIntegerAttribute = new JMenuItem();
			jMenuItemLongIntegerAttribute.setText("Long Integer Attribute");
			jMenuItemLongIntegerAttribute.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						createNewAttribute("Long Integer");
					}
				});
		}

		return jMenuItemLongIntegerAttribute;
	}

	/**
	 * This method initializes jMenuItemFloatingPointAttribute
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemFloatingPointAttribute() {
		if (jMenuItemFloatingPointAttribute == null) {
			jMenuItemFloatingPointAttribute = new JMenuItem();
			jMenuItemFloatingPointAttribute.setText("Floating Point Attribute");
			jMenuItemFloatingPointAttribute.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						createNewAttribute("Floating Point");
					}
				});
		}

		return jMenuItemFloatingPointAttribute;
	}

	/**
	 * This method initializes jMenuItemBooleanAttribute
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemBooleanAttribute() {
		if (jMenuItemBooleanAttribute == null) {
			jMenuItemBooleanAttribute = new JMenuItem();
			jMenuItemBooleanAttribute.setText("Boolean Attribute");
			jMenuItemBooleanAttribute.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						createNewAttribute("Boolean");
					}
				});
		}

		return jMenuItemBooleanAttribute;
	}

	/**
	 * This method initializes jMenuItemStringListAttribute
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemStringListAttribute() {
		if (jMenuItemStringListAttribute == null) {
			jMenuItemStringListAttribute = new JMenuItem();
			jMenuItemStringListAttribute.setText("String List Attribute");
			jMenuItemStringListAttribute.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						createNewAttribute("String List");
					}
				});
		}

		return jMenuItemStringListAttribute;
	}

	/**
	 * This method initializes jMenuItemIntegerListAttribute
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemIntegerListAttribute() {
		if (jMenuItemIntegerListAttribute == null) {
			jMenuItemIntegerListAttribute = new JMenuItem();
			jMenuItemIntegerListAttribute.setText("Integer List Attribute");
			jMenuItemIntegerListAttribute.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						createNewAttribute("Integer List");
					}
				});
		}

		return jMenuItemIntegerListAttribute;
	}

	/**
	 * This method initializes jMenuItemLongIntegerListAttribute
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemLongIntegerListAttribute() {
		if (jMenuItemLongIntegerListAttribute == null) {
			jMenuItemLongIntegerListAttribute = new JMenuItem();
			jMenuItemLongIntegerListAttribute.setText("Long Integer List Attribute");
			jMenuItemLongIntegerListAttribute.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						createNewAttribute("Integer List");
					}
				});
		}

		return jMenuItemLongIntegerListAttribute;
	}

	/**
	 * This method initializes jMenuItemFloatingPointListAttribute
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemFloatingPointListAttribute() {
		if (jMenuItemFloatingPointListAttribute == null) {
			jMenuItemFloatingPointListAttribute = new JMenuItem();
			jMenuItemFloatingPointListAttribute.setText("Floating Point List Attribute");
			jMenuItemFloatingPointListAttribute.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						createNewAttribute("Floating Point List");
					}
				});
		}

		return jMenuItemFloatingPointListAttribute;
	}

	/**
	 * This method initializes jMenuItemBooleanListAttribute
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemBooleanListAttribute() {
		if (jMenuItemBooleanListAttribute == null) {
			jMenuItemBooleanListAttribute = new JMenuItem();
			jMenuItemBooleanListAttribute.setText("Boolean List Attribute");
			jMenuItemBooleanListAttribute.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						createNewAttribute("Boolean List");
					}
				});
		}

		return jMenuItemBooleanListAttribute;
	}

	/**
	 * This method initializes jToolBar
	 *
	 * @return javax.swing.JToolBar
	 */
	private JToolBar getJToolBar() {
		if (browserToolBar == null) {
			browserToolBar = new JToolBar();
			browserToolBar.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
//					AttributeBrowser.getPropertyChangeSupport().firePropertyChange(AttributeBrowser.CLEAR_INTERNAL_SELECTION, null, objectType);
				}
			});
			browserToolBar.setMargin(new java.awt.Insets(0, 0, 3, 0));
			browserToolBar.setPreferredSize(TOOLBAR_SIZE);
			browserToolBar.setSize(TOOLBAR_SIZE);
			browserToolBar.setFloatable(false);
			browserToolBar.setOrientation(JToolBar.HORIZONTAL);

			final GroupLayout buttonBarLayout = new GroupLayout(browserToolBar);
			browserToolBar.setLayout(buttonBarLayout);

			// Layout information.
			buttonBarLayout.setHorizontalGroup(buttonBarLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
							   .addGroup(buttonBarLayout.createSequentialGroup()
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(selectionModeButton)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(getSelectButton())
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(getSelectAllButton())
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(getUnselectAllButton())
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(getNewButton())
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(getDeleteButton())
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(getDeleteTableButton())
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(getFunctionBuilderButton())
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(this.tableChooser)));
			buttonBarLayout.setVerticalGroup(buttonBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
							.addComponent(selectionModeButton, javax.swing.GroupLayout.Alignment.CENTER,
									javax.swing.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
							.addComponent(selectButton,
								       javax.swing.GroupLayout.Alignment.CENTER,
								       javax.swing.GroupLayout.PREFERRED_SIZE,
								       27,
								       Short.MAX_VALUE)
							 .addComponent(selectAllAttributesButton,
								       javax.swing.GroupLayout.Alignment.CENTER,
								       javax.swing.GroupLayout.DEFAULT_SIZE,
								       27, Short.MAX_VALUE)
							 .addComponent(unselectAllAttributesButton,
								       javax.swing.GroupLayout.Alignment.CENTER,
								       javax.swing.GroupLayout.DEFAULT_SIZE,
								       27, Short.MAX_VALUE)
							.addComponent(createNewAttributeButton,
								       javax.swing.GroupLayout.Alignment.CENTER,
								       javax.swing.GroupLayout.DEFAULT_SIZE,
								       27, Short.MAX_VALUE)
							 .addComponent(deleteAttributeButton,
								       javax.swing.GroupLayout.Alignment.CENTER,
								       javax.swing.GroupLayout.DEFAULT_SIZE,
								       27, Short.MAX_VALUE)
							 .addComponent(deleteTableButton,
								       javax.swing.GroupLayout.Alignment.CENTER,
								       javax.swing.GroupLayout.DEFAULT_SIZE,
								       27, Short.MAX_VALUE)
							.addComponent(formulaBuilderButton,
									javax.swing.GroupLayout.Alignment.CENTER,
								       javax.swing.GroupLayout.DEFAULT_SIZE,
								       27, Short.MAX_VALUE)
							.addComponent(tableChooser,
									javax.swing.GroupLayout.Alignment.CENTER,
								       javax.swing.GroupLayout.DEFAULT_SIZE,
								       27, Short.MAX_VALUE));
		}

		return browserToolBar;
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
			selectButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
			selectButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("images/stock_select-row.png")));
			selectButton.setToolTipText("Select Attributes");
			selectButton.setBorder(null);

			selectButton.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (browserTableModel == null)
							return;
						attributeList.setSelectedItems(browserTableModel.getVisibleAttributeNames());
						attributeSelectionPopupMenu.show(e.getComponent(), e.getX(), e.getY());
					}
				});
		}
		selectButton.setEnabled(false);

		return selectButton;
	}

	private JButton formulaBuilderButton = null;

	private JButton getFunctionBuilderButton() {
		if (formulaBuilderButton == null) {
			formulaBuilderButton = new JButton();
			formulaBuilderButton.setBorder(null);
			formulaBuilderButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("images/fx.png")));
			formulaBuilderButton.setToolTipText("Function Builder");
			formulaBuilderButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
			formulaBuilderButton.setBorder(null);

			final JFrame rootFrame = (JFrame)SwingUtilities.getRoot(this);

			formulaBuilderButton.addMouseListener(new java.awt.event.MouseAdapter() {
					public void mouseClicked(java.awt.event.MouseEvent e) {
						final JTable table = browserTableModel.getTable();

						// Do not allow opening of the formula builder dialog
						// while a cell is being edited!
						if (table.getCellEditor() != null)
							return;

						final int cellRow = table.getSelectedRow();
						final int cellColumn = table.getSelectedColumn();
						if (cellRow == -1 || cellColumn == -1
						    || !browserTableModel.isCellEditable(cellRow, cellColumn))
							JOptionPane.showMessageDialog(
								rootFrame,
							        "Can't enter a formula w/o a selected cell!",
							        "Information", JOptionPane.INFORMATION_MESSAGE);
						else {
							final String attrName = getAttribName(cellRow, cellColumn);
							final Map<String, Class<?>> attribNameToTypeMap
								= new HashMap<String, Class<?>>();
							final CyTable attrs =
								browserTableModel.getAttributes();
							initAttribNameToTypeMap(attrs, attrName, attribNameToTypeMap);
							final FormulaBuilderDialog formulaBuilderDialog =
								new FormulaBuilderDialog(compiler, browserTableModel,
											 rootFrame, attrName);
							formulaBuilderDialog.setLocationRelativeTo(rootFrame);
							formulaBuilderDialog.setVisible(true);
						}
					}

					private void initAttribNameToTypeMap(
						final CyTable attrs, final String attrName,
						final Map<String, Class<?>> attribNameToTypeMap)
					{
						for (final CyColumn column : attrs.getColumns())
							attribNameToTypeMap.put(column.getName(),
										column.getType());
						attribNameToTypeMap.remove(attrName);
					}
				});
		}
		formulaBuilderButton.setEnabled(false);

		return formulaBuilderButton;
	}

	private String getAttribName(final int cellRow, final int cellColumn) {
		return browserTableModel.getColumnName(cellColumn);
	}

	private JButton getDeleteButton() {
		if (deleteAttributeButton == null) {
			deleteAttributeButton = new JButton();
			deleteAttributeButton.setBorder(null);
			deleteAttributeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
			deleteAttributeButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("images/stock_delete.png")));
			deleteAttributeButton.setToolTipText("Delete Attributes...");
			deleteAttributeButton.setBorder(null);

			// Create pop-up window for deletion
			deleteAttributeButton.addMouseListener(new java.awt.event.MouseAdapter() {
					public void mouseClicked(java.awt.event.MouseEvent e) {
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
			deleteTableButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
			deleteTableButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("images/table_delete.png")));
			deleteTableButton.setToolTipText("Delete Table...");
			deleteTableButton.setBorder(null);
			
			// Create pop-up window for deletion
			deleteTableButton.addMouseListener(new java.awt.event.MouseAdapter() {
					public void mouseClicked(java.awt.event.MouseEvent e) {
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
			selectAllAttributesButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
			selectAllAttributesButton.setIcon(new javax.swing.ImageIcon(getClass().getClassLoader().getResource("images/select_all.png")));
			selectAllAttributesButton.setToolTipText("Select All Attributes");
			selectAllAttributesButton.setBorder(null);

			selectAllAttributesButton.addMouseListener(new java.awt.event.MouseAdapter() {
					public void mouseClicked(java.awt.event.MouseEvent e) {
						try {
							final CyTable table = browserTableModel.getAttributes();
							final Set<String> allAttrNames = new HashSet<String>();
							for (final CyColumn column : table.getColumns())
								allAttrNames.add(column.getName());
							browserTableModel.setVisibleAttributeNames(allAttrNames);
						} catch (Exception ex) {
							attributeList.clearSelection();
						}
					}
				});
		}
		selectAllAttributesButton.setEnabled(false);

		return selectAllAttributesButton;
	}

	private JButton getUnselectAllButton() {
		if (unselectAllAttributesButton == null) {
			unselectAllAttributesButton = new JButton();
			unselectAllAttributesButton.setBorder(null);
			unselectAllAttributesButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
			unselectAllAttributesButton.setIcon(new javax.swing.ImageIcon(getClass().getClassLoader().getResource("images/unselect_all.png")));
			unselectAllAttributesButton.setToolTipText("Unselect All Attributes");
			unselectAllAttributesButton.setBorder(null);

			unselectAllAttributesButton.addMouseListener(new java.awt.event.MouseAdapter() {
					public void mouseClicked(java.awt.event.MouseEvent e) {
						try {
							browserTableModel.setVisibleAttributeNames(new HashSet<String>());
						} catch (Exception ex) {
							attributeList.clearSelection();
						}
					}
				});
		}
		unselectAllAttributesButton.setEnabled(false);

		return unselectAllAttributesButton;
	}

	private void removeAttribute(final MouseEvent e) {
		final String[] attrArray = getAttributeArray();

		final JFrame frame = (JFrame)SwingUtilities.getRoot(this);
		final DeletionDialog dDialog = new DeletionDialog(frame, browserTableModel.getAttributes());

		dDialog.pack();
		dDialog.setLocationRelativeTo(browserToolBar);
		dDialog.setVisible(true);
	}

	private void removeTable(final MouseEvent e) {
				
		final CyTable table = browserTableModel.getAttributes();
				
		if (table.getMutability() == CyTable.Mutability.MUTABLE){
			String title = "Please confirm this action";
			String msg = "Are yoy sure you want to delete this table?";
		    int _confirmValue = JOptionPane.showConfirmDialog(this, msg, title, JOptionPane.YES_NO_OPTION, 
		    		JOptionPane.QUESTION_MESSAGE);
		    
			// if user selects yes delete the table
			if (_confirmValue == JOptionPane.OK_OPTION)
			{
				deleteTableTaskFactoryService.setTable(table);
				guiTaskManagerServiceRef.execute(deleteTableTaskFactoryService);
				
				//this.tableManager.deleteTable(table.getSUID());
			}						
		}
		else if (table.getMutability() == CyTable.Mutability.PERMANENTLY_IMMUTABLE){
			String title = "Error";
			String msg = "Can not delete this table, it is PERMANENTLY_IMMUTABLE";
			JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
		}
		else if(table.getMutability() == CyTable.Mutability.IMMUTABLE_DUE_TO_VIRT_COLUMN_REFERENCES){
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
			createNewAttributeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
			createNewAttributeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
			createNewAttributeButton.setToolTipText("Create New Attribute");
			createNewAttributeButton.setIcon(new javax.swing.ImageIcon(getClass().getClassLoader().getResource("images/stock_new.png")));
			createNewAttributeButton.setBorder(null);
			
			createNewAttributeButton.addMouseListener(new java.awt.event.MouseAdapter() {
					public void mouseClicked(java.awt.event.MouseEvent e) {
						if (browserTableModel != null)
							jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
					}
				});
			createNewAttributeButton.setEnabled(false);
		}

		return createNewAttributeButton;
	}

	// Create a whole new attribute and set a default value.
	//
	private void createNewAttribute(final String type) {
		final String[] existingAttrs = getAttributeArray();
		String newAttribName = null;
		do {
			newAttribName = JOptionPane.showInputDialog(this, "Please enter new attribute name: ",
								    "Create New " + type + " Attribute",
								    JOptionPane.QUESTION_MESSAGE);
			if (newAttribName == null)
				return;

			if (Arrays.binarySearch(existingAttrs, newAttribName) >= 0) {
				newAttribName = null;
				JOptionPane.showMessageDialog(null,
							      "Attribute " + newAttribName + " already exists.",
							      "Error!", JOptionPane.ERROR_MESSAGE);
			}
		} while (newAttribName == null);

		final CyTable attrs = browserTableModel.getAttributes();
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
			throw new IllegalArgumentException("unknown attribute type \"" + type + "\"!");
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
}
