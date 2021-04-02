package org.cytoscape.internal.view;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.JMenuTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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
public class CytoscapeMenuBar extends JMenuBar {
	
	public static final String DEFAULT_MENU_SPECIFIER = "Tools";
	
	private static final String TABSTR = "\t";
	private final static Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	private final Map<Action, JMenuItem> actionMenuItemMap;
	private final Map<String, String> actionOverrideMap;
	private final JMenuTracker menuTracker;

	private HashSet<String> stopList = new HashSet<>();
	
//	private static final boolean verbose = true;
	private static boolean produceActionTable;
	
	private final CyServiceRegistrar registrar;
	
	public CytoscapeMenuBar(CyServiceRegistrar reg) {
		registrar = reg;
		actionMenuItemMap = new HashMap<>();
		actionOverrideMap = new HashMap<>(); // map the name to the whole line
		menuTracker = new JMenuTracker(this);

		// Load the first menu, just to please the layouter. Also make sure the
		// menu bar doesn't get too small.
		// "File" is always first
		setMinimumSize(getMenu("File").getPreferredSize());
		readStopList();
		readMenuCustomizer();
	}
	
	/**
	 * If the given Action has a present and false inMenuBar property, return;
	 * otherwise create the menu item.
	 */
	public boolean addAction(CyAction action) {
		if (null == action.getName())
			return false;
		
		if (produceActionTable)	
			dumpActionToTable(action);
		
		var configgedAction = configgedAction(action);
		
		if (configgedAction == null || !configgedAction.isInMenuBar())
			return false;

		boolean insertSepBefore = configgedAction.insertSeparatorBefore();;
		boolean insertSepAfter = configgedAction.insertSeparatorAfter();

		// We allow an Action to be in this menu bar only once.
		if (actionMenuItemMap.containsKey(configgedAction))
			return false;

		// Actions with no preferredMenu don't show up in any menu.
		var menuName = configgedAction.getPreferredMenu();
		
		if (menuName == null || menuName.isEmpty())
			return false;
		
		var gravityTracker = menuTracker.getGravityTracker(menuName);
		var mi = createMenuItem(configgedAction);
		var item = configgedAction.getName();
		
		if (stopList.contains(item))
			mi.setVisible(false);			

		// Add an Accelerator Key, if wanted
		var accelerator = configgedAction.getAcceleratorKeyStroke();
		
		if (accelerator != null)
			mi.setAccelerator(accelerator);

		((JMenu) gravityTracker.getMenu()).addMenuListener(configgedAction);
		
		if (insertSepBefore)
			gravityTracker.addMenuSeparator(configgedAction.getMenuGravity() - .0001);
		
		gravityTracker.addMenuItem(mi, configgedAction.getMenuGravity());
		
		if (insertSepAfter)
			gravityTracker.addMenuSeparator(configgedAction.getMenuGravity() + .0001);

		// set this true to include the gravity into the menu names (so you can see how to set your own)
		boolean showGravity = false;			
		
		if (showGravity) {
			var debugName = configgedAction.getName();
			debugName = "[" + configgedAction.getMenuGravity() + "] " + debugName;
			configgedAction.setName(debugName);
		}

		logger.debug("Inserted action for menu: " + menuName + " with gravity: " + configgedAction.getMenuGravity());
		actionMenuItemMap.put(configgedAction, mi);

		revalidate();
		repaint();
		
		return true;
	}

	// this will produce the table that you can put into excel to edit menu / toolbar appearance
	private void dumpActionToTable(CyAction action) {
		var clasname = "" + action.getClass();
		clasname = clasname.substring(1 + clasname.lastIndexOf('.'));
		
		System.out.println(
				action.getName() + "\t"
				+ ((action.getAcceleratorKeyStroke() == null) ? "" : action.getAcceleratorKeyStroke()) + "\t" 
				+ (action.isInMenuBar() ? "T" : "F") + "\t"
				+ ((action.getPreferredMenu()  == null) ? "" : action.getPreferredMenu()) + "\t" 
				+ action.getMenuGravity() + "\t" 
				+ (action.isInToolBar() ? "T" : "F") + "\t" 
				+ action.getToolbarGravity() + "\t" 
				+ (action.isEnabled() ? "T" : "F") + "\t" 
				+ clasname + "\t"
		);
	}

	private AbstractCyAction configgedAction(CyAction action) {
		var mappedAction = actionOverrideMap.get(action.getName());
		
		if (mappedAction != null) {
			var tokens = mappedAction.split(TABSTR);
//			assert tokens.length == 9;
//			var name = tokens[0];
			var accelerator = tokens[1];
			
			//-------------------- !!!!!!!!!!!!!!  ----------- this will break, as setters aren't in the api yet
			if (tokens.length == 9) { // its the full table (otherwise its just accelerator definition)
//				boolean inMenuBar = "T".equals(tokens[2]);
//				String prefMenu = tokens[3];
//				String gravity = tokens[4];
//				boolean inToolBar = "T".equals(tokens[5]);
//				String toolgravity = tokens[6];
				boolean enabled = "T".equals(tokens[7]);   // last two not used
//				String classname = tokens[8];
				
				action.setEnabled(enabled);
//				action.setIsInMenuBar(inMenuBar);
//				action.setPreferredMenu(prefMenu);
				
//				float f = 0, g = 0;
//				try {
//					f = Float.parseFloat(gravity);
//				} catch (Exception e) {
//				}
//				action.setMenuGravity(g);
//				try {
//					g = Float.parseFloat(toolgravity);
//				} catch (Exception e) {
//				}
//				action.setToolbarGravity(g);
//				action.setIsInToolBar(inToolBar);
			}
			
//			if (verbose && !produceActionTable) {
//				 var key = KeyStroke.getKeyStroke(accelerator);
//				 if (key != action.getAcceleratorKeyStroke())
//					System.out.println("Overriding: " + name + " with " + accelerator);  // + (inMenuBar ? " menu" : "") + (inToolBar ? " tool" : "")
//			}
			
			try {
				var cast = (AbstractCyAction) action;
				cast.setAcceleratorKeyStroke(KeyStroke.getKeyStroke(accelerator));
			} catch (ClassCastException e) {
				System.err.println("Action isn't an AbstractCyAction");
			}
		}
		
		return (AbstractCyAction) action;
	}

	public void showAll() {
		for (var item : actionMenuItemMap.values())
			item.setVisible(true);
	}

	public void addSeparator(String menuName, double gravity) {
		if (menuName == null || menuName.isEmpty())
			menuName = DEFAULT_MENU_SPECIFIER;

		var gravityTracker = menuTracker.getGravityTracker(menuName);
		gravityTracker.addMenuSeparator(gravity);
	}

	/**
	 * If the given Action has a present and false inMenuBar property, return;
	 * otherwise, if there's a menu item for the action, remove it.
	 * Its menu is determined by its preferredMenu property if it is present; otherwise by DEFAULT_MENU_SPECIFIER.
	 */
	public boolean removeAction(CyAction action) {
		var menuItem = actionMenuItemMap.remove(action);
		
		if (menuItem == null)
			return false;

		String menuName = null;

		if (action.isInMenuBar())
			menuName = action.getPreferredMenu();
		else
			return false;

		if (menuName == null)
			menuName = DEFAULT_MENU_SPECIFIER;

		var gravityTracker = menuTracker.getGravityTracker(menuName);
		gravityTracker.removeComponent(menuItem);
		((JMenu) gravityTracker.getMenu()).removeMenuListener(action);

		return true;
	}

	public JMenu addMenu(String menuName, final double gravity) {
		menuName += "[" + gravity + "]";
		var gravityTracker = menuTracker.getGravityTracker(menuName);
		
		return (JMenu) gravityTracker.getMenu();
	}

	/**
	 * @return the menu named in the given String. The String may contain multiple menu names,
	 *         separated by dots ('.'). If any contained menu name does not correspond to an existing menu,
	 *         then that menu will be created as a child of the menu preceeding the most recent dot or,
	 *         if there is none, then as a child of this MenuBar.
	 */
	public JMenu getMenu(String menuName) {
		if (menuName == null)
			menuName = DEFAULT_MENU_SPECIFIER;

		var gravityTracker = menuTracker.getGravityTracker(menuName);
		revalidate();
		repaint();

		return (JMenu) gravityTracker.getMenu();
	}

	public JMenuTracker getMenuTracker() {
		return menuTracker;
	}

	private JMenuItem createMenuItem(CyAction action) {
		final JMenuItem ret;
		
		if (action.useCheckBoxMenuItem())
			ret = new JCheckBoxMenuItem(action);
		else
			ret = new JMenuItem(action);

		return ret;
	}

	public JMenuBar getJMenuBar() {
		return this;
	}

	private void readStopList() {
		stopList.clear();
		
		final List<String> lines;
		
		try {
			var applicationConfig = registrar.getService(CyApplicationConfiguration.class);
			
			if (applicationConfig == null)
				return;

			File configDirectory = applicationConfig.getConfigurationDirectoryLocation();
			File configFile = null;
			
			if (configDirectory.exists())
				configFile = new File(configDirectory.toPath() + "/menubar.stoplist");
			
			if (configFile == null)
				return;
			
			lines = Files.readAllLines(configFile.toPath(), Charset.defaultCharset());
		} catch (IOException e) {
			// file not found: there's no customization, just return
			return;
		}
				
		for (var line : lines)
			stopList.add(line.trim());
	}
	
	private void readMenuCustomizer() {
		actionOverrideMap.clear();
		
		final List<String> lines;

		try {
			var applicationConfig = registrar.getService(CyApplicationConfiguration.class);

			if (applicationConfig == null)
				return;

			File configDirectory = applicationConfig.getConfigurationDirectoryLocation();
			File configFile = null;

			if (configDirectory.exists())
				configFile = new File(configDirectory.toPath() + "/menubar.custom.txt");

			if (configFile == null)
				return;

			lines = Files.readAllLines(configFile.toPath(), Charset.defaultCharset());
		} catch (IOException e) {
			// file not found: there's no customization, just return
			return;
		}

		for (var line : lines)
			addCustomizerRow(line.trim());
	}
	

	private void addCustomizerRow(String trimmed) {
		var tokens = trimmed.split(TABSTR);
		assert tokens.length == 8;
		
		String name = tokens[0];
//		String accelerator = tokens[1];
//		String prefMenu = tokens[2];
//		String gravity = tokens[3];
//		String inMenuBar = tokens[4];
//		String inToolBar = tokens[5];
//		String enabled = tokens[6];
//		String classname = tokens[7];
		
		actionOverrideMap.put(name, trimmed);
	}
}
