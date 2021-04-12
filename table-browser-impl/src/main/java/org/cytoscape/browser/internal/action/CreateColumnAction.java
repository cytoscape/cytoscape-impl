package org.cytoscape.browser.internal.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.browser.internal.view.TableBrowserMediator;
import org.cytoscape.browser.internal.view.TableRenderer;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
public class CreateColumnAction extends AbstractCyAction {

	private static String TITLE = "Create New Column...";
	
	private final TableBrowserMediator mediator;
	private final CyServiceRegistrar serviceRegistrar;

	public CreateColumnAction(
			Icon icon,
			float toolbarGravity,
			TableBrowserMediator mediator,
			CyServiceRegistrar serviceRegistrar
	) {
		super(TITLE);
		this.mediator = mediator;
		this.serviceRegistrar = serviceRegistrar;
		
		putValue(SHORT_DESCRIPTION, TITLE);
		putValue(LARGE_ICON_KEY, icon);
		setIsInNodeTableToolBar(true);
		setIsInEdgeTableToolBar(true);
		setIsInNetworkTableToolBar(true);
		setIsInUnassignedTableToolBar(true);
		setToolbarGravity(toolbarGravity);
		insertSeparatorBefore = true;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		var source = evt.getSource();
		
		if (source instanceof Component)
			showCreateColumnPopup((Component) source);
	}
	
	private void showCreateColumnPopup(Component invoker) {
		var popup = new JPopupMenu();
			
		var columnRegular = new JMenu("New Single Column");
		var columnList = new JMenu("New List Column");

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
		
		popup.add(columnRegular);
		popup.add(columnList);
		
		popup.pack();
		popup.show(invoker, 0, invoker.getHeight());
	}
	
	private JMenuItem getJMenuItemStringAttribute(boolean isShared) {
		var mi = new JMenuItem();
		mi.setText("String");
		mi.addActionListener(e -> createNewAttribute("String", isShared));

		return mi;
	}

	private JMenuItem getJMenuItemIntegerAttribute(boolean isShared) {
		var mi = new JMenuItem();
		mi.setText("Integer");
		mi.addActionListener(e -> createNewAttribute("Integer", isShared));

		return mi;
	}

	private JMenuItem getJMenuItemLongIntegerAttribute(boolean isShared) {
		var mi = new JMenuItem();
		mi.setText("Long Integer");
		mi.addActionListener(e -> createNewAttribute("Long Integer", isShared));
		
		return mi;
	}

	private JMenuItem getJMenuItemFloatingPointAttribute(boolean isShared) {
		var mi = new JMenuItem();
		mi.setText("Floating Point");
		mi.addActionListener(e -> createNewAttribute("Floating Point", isShared));

		return mi;
	}

	private JMenuItem getJMenuItemBooleanAttribute(boolean isShared) {
		var mi = new JMenuItem();
		mi.setText("Boolean");
		mi.addActionListener(e -> createNewAttribute("Boolean", isShared));

		return mi;
	}

	private JMenuItem getJMenuItemStringListAttribute(boolean isShared) {
		var mi = new JMenuItem();
		mi.setText("String");
		mi.addActionListener(e -> createNewAttribute("String List", isShared));

		return mi;
	}

	private JMenuItem getJMenuItemIntegerListAttribute(boolean isShared) {
		var mi = new JMenuItem();
		mi.setText("Integer");
		mi.addActionListener(e -> createNewAttribute("Integer List", isShared));

		return mi;
	}

	private JMenuItem getJMenuItemLongIntegerListAttribute(boolean isShared) {
		var mi = new JMenuItem();
		mi.setText("Long Integer");
		mi.addActionListener(e -> createNewAttribute("Long Integer List", isShared));

		return mi;
	}

	private JMenuItem getJMenuItemFloatingPointListAttribute(boolean isShared) {
		var mi = new JMenuItem();
		mi.setText("Floating Point");
		mi.addActionListener(e -> createNewAttribute("Floating Point List", isShared));

		return mi;
	}

	private JMenuItem getJMenuItemBooleanListAttribute(boolean isShared) {
		var mi = new JMenuItem();
		mi.setText("Boolean");
		mi.addActionListener(e -> createNewAttribute("Boolean List", isShared));

		return mi;
	}
	
	private void createNewAttribute(String type, boolean isShared) {
		try {
			var renderer = mediator.getCurrentTableRenderer();
			var existingAttrs = getAttributeArray(renderer);
			String newAttribName = null;
			
			do {
				newAttribName = JOptionPane.showInputDialog(
						renderer.getComponent(),
						"Column Name: ",
						"Create New " + type + " Column",
						JOptionPane.QUESTION_MESSAGE
				);
				
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
	
			CyTable attrs = null;
			
			if (isShared) {
				var network = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();
							
				if (network instanceof CySubNetwork) {
					var rootNetwork = ((CySubNetwork) network).getRootNetwork();
					var objType = mediator.getTableBrowser(renderer).getObjectType();
					
					final CyTable sharedTable;
					
					if (objType == CyNode.class)
						sharedTable = rootNetwork.getSharedNodeTable();
					else if (objType == CyEdge.class)
						sharedTable = rootNetwork.getSharedEdgeTable();
					else if (objType == CyNetwork.class)
						sharedTable = rootNetwork.getSharedNetworkTable();
					else
						throw new IllegalStateException("Object type is not valid.  This should not happen.");
					
					attrs = sharedTable;
				} else {
					throw new IllegalArgumentException("This is not a CySubNetwork and there is no shared table.");
				}
			} else {
				attrs = renderer != null ? renderer.getDataTable() : null;
			}
		
			if (attrs != null) {
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
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private String[] getAttributeArray(TableRenderer renderer) {
		if (renderer == null)
			return new String[0];
		
		var attrs = renderer.getDataTable();
		var columns = attrs.getColumns();
		var attributeArray = new String[columns.size() - 1];
		int index = 0;
		
		for (var col : columns) {
			if (!col.isPrimaryKey())
				attributeArray[index++] = col.getName();
		}
		
		Arrays.sort(attributeArray);

		return attributeArray;
	}
}
