package org.cytoscape.internal.view;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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


import javax.swing.*;
import java.util.HashMap;
import java.util.Map;


import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.util.swing.JMenuTracker;
import org.cytoscape.util.swing.GravityTracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CytoscapeMenuBar extends JMenuBar {
	private final static long serialVersionUID = 1202339868642259L;
	private final static Logger logger = LoggerFactory.getLogger(CytoscapeMenuBar.class);
	private final Map<Action,JMenuItem> actionMenuItemMap; 
	private final JMenuTracker menuTracker;

	public static final String DEFAULT_MENU_SPECIFIER = "Tools";

	/**
	 * Default constructor.
	 */
	public CytoscapeMenuBar() {
		actionMenuItemMap = new HashMap<Action,JMenuItem>();
		menuTracker = new JMenuTracker(this);

		// Load the first menu, just to please the layouter. Also make sure the
		// menu bar doesn't get too small.
		// "File" is always first
		setMinimumSize(getMenu("File").getPreferredSize());
	}

	/**
	 * If the given Action has a present and false inMenuBar property, return;
	 * otherwise delegate to addAction( String, Action ) with the value of its
	 * preferredMenu property, or null if it does not have that property.
	 */
	public boolean addAction(final CyAction action) {
		if (!action.isInMenuBar())
			return false;

		boolean insertSepBefore = false;
		boolean insertSepAfter = false;
		if (action instanceof AbstractCyAction) {
			insertSepBefore = ((AbstractCyAction)action).insertSeparatorBefore();
			insertSepAfter = ((AbstractCyAction)action).insertSeparatorAfter();
		}

		// At present we allow an Action to be in this menu bar only once.
		if ( actionMenuItemMap.containsKey(action) )
			return false;

		// Actions with no preferredMenu don't show up in any menu.
		String menu_name = action.getPreferredMenu();
		if (menu_name == null || menu_name.isEmpty())
			return false;
		
		final GravityTracker gravityTracker = menuTracker.getGravityTracker(menu_name);
		final JMenuItem menu_item = createMenuItem(action);

		// Add an Accelerator Key, if wanted
		final KeyStroke accelerator = action.getAcceleratorKeyStroke();
		if (accelerator != null)
			menu_item.setAccelerator(accelerator);

		((JMenu) gravityTracker.getMenu()).addMenuListener(action);
		if (insertSepBefore)
			gravityTracker.addMenuSeparator(action.getMenuGravity()-.0001);
		gravityTracker.addMenuItem(menu_item, action.getMenuGravity());
		if (insertSepAfter)
			gravityTracker.addMenuSeparator(action.getMenuGravity()+.0001);
		logger.debug("Inserted action for menu: " + menu_name + " with gravity: " + action.getMenuGravity());
		actionMenuItemMap.put(action, menu_item);

		revalidate();
		repaint();
		
		return true;
	}

	public void addSeparator(String menu_name, final double gravity) {
		if (menu_name == null || menu_name.isEmpty())
			menu_name = DEFAULT_MENU_SPECIFIER;

		final GravityTracker gravityTracker = menuTracker.getGravityTracker(menu_name);
		gravityTracker.addMenuSeparator(gravity);
	}

	/**
	 * If the given Action has a present and false inMenuBar property, return;
	 * otherwise, if there's a menu item for the action, remove it. Its menu is
	 * determined by its preferredMenu property if it is present; otherwise by
	 *  DEFAULT_MENU_SPECIFIER.
	 */
	public boolean removeAction(CyAction action) {
		JMenuItem menu_item = actionMenuItemMap.remove(action);
		if (menu_item == null)
			return false;

		String menu_name = null;
		if (action.isInMenuBar())
			menu_name = action.getPreferredMenu();
		else
			return false;
		if (menu_name == null)
			menu_name =  DEFAULT_MENU_SPECIFIER;

		menuTracker.getGravityTracker(menu_name).removeComponent(menu_item);

		return true;
	}

	public JMenu addMenu(String menu_string, final double gravity) {
		menu_string += "[" + gravity + "]";
		final GravityTracker gravityTracker = menuTracker.getGravityTracker(menu_string);
		return (JMenu)gravityTracker.getMenu();
	}

	/**
	 * @return the menu named in the given String. The String may contain
	 *         multiple menu names, separated by dots ('.'). If any contained
	 *         menu name does not correspond to an existing menu, then that menu
	 *         will be created as a child of the menu preceeding the most recent
	 *         dot or, if there is none, then as a child of this MenuBar.
	 */
	public JMenu getMenu(String menu_string) {
		if (menu_string == null)
			menu_string = DEFAULT_MENU_SPECIFIER;

		final GravityTracker gravityTracker =
			menuTracker.getGravityTracker(menu_string);
		revalidate();
		repaint();
		return (JMenu)gravityTracker.getMenu();
	}

	public JMenuTracker getMenuTracker() {
		return menuTracker;
	}

	private JMenuItem createMenuItem(final CyAction action) {
		JMenuItem ret;
		if (action.useCheckBoxMenuItem())
			ret = new JCheckBoxMenuItem(action);
		else
			ret = new JMenuItem(action);

		return ret;
	}

	public JMenuBar getJMenuBar() {
		return this;
	}
}
