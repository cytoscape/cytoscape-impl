package org.cytoscape.view.vizmap.gui.internal.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.util.swing.GravityTracker;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.MenuGravityTracker;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

public class PropertySheetPanel {

	private JTabbedPane propertiesPn;
	private final Map<Class<? extends CyIdentifiable>, VisualPropertySheet> vpSheetMap;
	
	/** Context menu */
	private JPopupMenu contextPopupMenu;
	private JMenu editSubMenu;
	private MenuGravityTracker editSubMenuGravityTracker;
	private JMenu mapValueGeneratorsSubMenu;
	
	private final Icon networkIcon;
	private final Icon tableIcon;
	
	private static Map<Class<?>, Integer> tabOrder = new HashMap<>();
	static {
		tabOrder.put(CyNode.class, 1);
		tabOrder.put(CyEdge.class, 2);
		tabOrder.put(CyNetwork.class, 3);
		tabOrder.put(CyColumn.class, 4);
	}
	
	public PropertySheetPanel(ServicesUtil servicesUtil) {
		vpSheetMap = new HashMap<>();
		
		var iconManager = servicesUtil.get(IconManager.class);
		var iconFont = iconManager.getIconFont(14.0f);
		networkIcon = new TextIcon(IconManager.ICON_SHARE_ALT_SQUARE, iconFont, 16, 16);
		tableIcon = new TextIcon(IconManager.ICON_TABLE, iconFont, 16, 16);
	}
	
	public JComponent getComponent() {
		return getPropertiesPn();
	}
	
	public Set<VisualPropertySheet> getVisualPropertySheets() {
		return new HashSet<>(vpSheetMap.values());
	}
	
	public VisualPropertySheet getVisualPropertySheet(Class<? extends CyIdentifiable> targetDataType) {
		return vpSheetMap.get(targetDataType);
	}
	
	public void addVisualPropertySheet(VisualPropertySheet sheet) {
		if (sheet == null)
			return;
		
		var type = sheet.getModel().getLexiconType();
		vpSheetMap.put(type, sheet);
		
		// Make sure the tabs are always in the correct order
		getPropertiesPn().removeAll();
		
		var sheets = new ArrayList<VisualPropertySheet>(vpSheetMap.values());
		sheets.sort((s1, s2) -> {
			int o1 = tabOrder.get(s1.getModel().getLexiconType());
			int o2 = tabOrder.get(s2.getModel().getLexiconType());
			return Integer.compare(o1, o2);
		});
		
		for (var s : sheets) {
			var icon = s.getModel().getLexiconType() == CyColumn.class ? tableIcon : null;
			getPropertiesPn().addTab(s.getModel().getTitle(), icon, s);
		}
	}

	public void removeAllVisualPropertySheets() {
		getPropertiesPn().removeAll();
		vpSheetMap.clear();
	}
	
	public VisualPropertySheet getSelectedVisualPropertySheet() {
		return (VisualPropertySheet) getPropertiesPn().getSelectedComponent();
	}
	
	public void setSelectedVisualPropertySheet(VisualPropertySheet sheet) {
		if (sheet != null) {
			int idx = getPropertiesPn().indexOfTab(sheet.getModel().getTitle());
			
			if (idx != -1)
				getPropertiesPn().setSelectedIndex(idx);
		}
	}
	
	JTabbedPane getPropertiesPn() {
		if (propertiesPn == null) {
			propertiesPn = new JTabbedPane(JTabbedPane.BOTTOM, JTabbedPane.WRAP_TAB_LAYOUT);
		}
		
		return propertiesPn;
	}
	
	
	JPopupMenu getContextMenu() {
		if (contextPopupMenu == null) {
			contextPopupMenu = new JPopupMenu();
			contextPopupMenu.add(getEditSubMenu());
			contextPopupMenu.add(getMapValueGeneratorsSubMenu());
			contextPopupMenu.add(new JSeparator());
			
			{
				var mi = new JMenuItem("Hide Selected Visual Properties");
				mi.addActionListener(evt -> hideSelectedItems());
				contextPopupMenu.add(mi);
			}
		}
		
		return contextPopupMenu;
	}
	
	JMenu getEditSubMenu() {
		if (editSubMenu == null) {
			editSubMenu = new JMenu("Edit");
		}
		
		return editSubMenu;
	}
	
	JMenu getMapValueGeneratorsSubMenu() {
		if (mapValueGeneratorsSubMenu == null) {
			mapValueGeneratorsSubMenu = new JMenu("Mapping Value Generators");
		}
		
		return mapValueGeneratorsSubMenu;
	}
	
	public void hideSelectedItems() {
		var vpSheet = getSelectedVisualPropertySheet();
		
		if (vpSheet != null) {
			for (var item : vpSheet.getSelectedItems())
				vpSheet.setVisible(item, false);
		}
	}
	
	private MenuGravityTracker getEditSubMenuGravityTracker() {
		if (editSubMenuGravityTracker == null) {
			editSubMenuGravityTracker = new MenuGravityTracker(getEditSubMenu());
		}
		
		return editSubMenuGravityTracker;
	}
	
	public void addContextMenuItem(JMenuItem menuItem, double gravity, boolean insertSeparatorBefore,
			boolean insertSeparatorAfter) {
		addMenuItem(getEditSubMenuGravityTracker(), menuItem, gravity, insertSeparatorBefore, insertSeparatorAfter);
		
		if (menuItem.getAction() instanceof CyAction)
			getContextMenu().addPopupMenuListener((CyAction)menuItem.getAction());
	}
	
	public void removeContextMenuItem(JMenuItem menuItem) {
		getEditSubMenuGravityTracker().removeComponent(menuItem);
		
		if (menuItem.getAction() instanceof CyAction)
			getContextMenu().removePopupMenuListener((CyAction)menuItem.getAction());
	}
	
	private void addMenuItem(GravityTracker gravityTracker, JMenuItem menuItem, double gravity,
			boolean insertSeparatorBefore, boolean insertSeparatorAfter) {
		if (insertSeparatorBefore)
			gravityTracker.addMenuSeparator(gravity - .0001);
		
		gravityTracker.addMenuItem(menuItem, gravity);
		
		if (insertSeparatorAfter)
			gravityTracker.addMenuSeparator(gravity + .0001);
	}
}
