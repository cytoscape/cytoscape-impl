package org.cytoscape.view.vizmap.gui.internal.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.cytoscape.util.swing.MenuGravityTracker;

public class PropertySheetPanel {

	private JTabbedPane propertiesPn;
	private final Map<Class<? extends CyIdentifiable>, VisualPropertySheet> vpSheetMap;
	
	/** Context menu */
	private JPopupMenu contextPopupMenu;
	private JMenu editSubMenu;
	private MenuGravityTracker editSubMenuGravityTracker;
	private JMenu mapValueGeneratorsSubMenu;
	
	private static Map<Class<?>,Integer> tabOrder = new HashMap<>();
	static {
		tabOrder.put(CyNode.class, 1);
		tabOrder.put(CyEdge.class, 2);
		tabOrder.put(CyNetwork.class, 3);
		tabOrder.put(CyColumn.class, 4);
	}
	
	
	public PropertySheetPanel() {
		vpSheetMap = new HashMap<>();
	}
	
	
	public JComponent getComponent() {
		return getPropertiesPn();
	}
	
	
	public Set<VisualPropertySheet> getVisualPropertySheets() {
		return new HashSet<>(vpSheetMap.values());
	}
	
	public VisualPropertySheet getVisualPropertySheet(final Class<? extends CyIdentifiable> targetDataType) {
		return vpSheetMap.get(targetDataType);
	}
	
	public void addVisualPropertySheet(final VisualPropertySheet sheet) {
		if (sheet == null)
			return;
		
		final Class<? extends CyIdentifiable> type = sheet.getModel().getTargetDataType();
		vpSheetMap.put(type, sheet);
		
		// Make sure the tabs are always in the correct order
		getPropertiesPn().removeAll();
		List<VisualPropertySheet> sheets = new ArrayList<>(vpSheetMap.values());
		sheets.sort((s1, s2) -> {
			int o1 = tabOrder.get(s1.getModel().getTargetDataType());
			int o2 = tabOrder.get(s2.getModel().getTargetDataType());
			return Integer.compare(o1, o2);
		});
		
		for(var s : sheets) {
			getPropertiesPn().addTab(s.getModel().getTitle(), s);
		}
	}

	public void removeAllVisualPropertySheets() {
		getPropertiesPn().removeAll();
		vpSheetMap.clear();
	}
	
	public VisualPropertySheet getSelectedVisualPropertySheet() {
		return (VisualPropertySheet) getPropertiesPn().getSelectedComponent();
	}
	
	public void setSelectedVisualPropertySheet(final VisualPropertySheet sheet) {
		if (sheet != null) {
			final int idx = getPropertiesPn().indexOfTab(sheet.getModel().getTitle());
			
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
				final JMenuItem mi = new JMenuItem("Hide Selected Visual Properties");
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
		final VisualPropertySheet vpSheet = getSelectedVisualPropertySheet();
		
		if (vpSheet != null) {
			for (final VisualPropertySheetItem<?> item : vpSheet.getSelectedItems())
				vpSheet.setVisible(item, false);
		}
	}
	
	private MenuGravityTracker getEditSubMenuGravityTracker() {
		if (editSubMenuGravityTracker == null) {
			editSubMenuGravityTracker = new MenuGravityTracker(getEditSubMenu());
		}
		
		return editSubMenuGravityTracker;
	}
	
	public void addContextMenuItem(final JMenuItem menuItem, final double gravity, boolean insertSeparatorBefore,
			boolean insertSeparatorAfter) {
		addMenuItem(getEditSubMenuGravityTracker(), menuItem, gravity, insertSeparatorBefore, insertSeparatorAfter);
		
		if (menuItem.getAction() instanceof CyAction)
			getContextMenu().addPopupMenuListener((CyAction)menuItem.getAction());
	}
	
	public void removeContextMenuItem(final JMenuItem menuItem) {
		getEditSubMenuGravityTracker().removeComponent(menuItem);
		
		if (menuItem.getAction() instanceof CyAction)
			getContextMenu().removePopupMenuListener((CyAction)menuItem.getAction());
	}
	
	private void addMenuItem(final GravityTracker gravityTracker, final JMenuItem menuItem, final double gravity,
			boolean insertSeparatorBefore, boolean insertSeparatorAfter) {
		if (insertSeparatorBefore)
			gravityTracker.addMenuSeparator(gravity - .0001);
		
		gravityTracker.addMenuItem(menuItem, gravity);
		
		if (insertSeparatorAfter)
			gravityTracker.addMenuSeparator(gravity + .0001);
	}
}
