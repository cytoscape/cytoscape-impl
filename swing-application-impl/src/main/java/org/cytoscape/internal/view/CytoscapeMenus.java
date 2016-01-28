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

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.util.swing.LookAndFeelUtil;

public class CytoscapeMenus {

	final private CytoscapeMenuBar menuBar;
	final private CytoscapeToolBar toolBar;

	public CytoscapeMenus(CytoscapeMenuBar menuBar, CytoscapeToolBar toolBar) {
		this.menuBar = menuBar;
		this.toolBar = toolBar;

		menuBar.addMenu("File", 0.0);
		menuBar.addMenu("File.New", 0.0);
		menuBar.addMenu("File.New.Network", 0.0);
		menuBar.addMenu("File.Open Recent", 1.9);
		menuBar.addMenu("File.Import", 5.0);
		menuBar.addMenu("File.Export", 5.1);
		menuBar.addMenu("Edit", 0.0);
		menuBar.addMenu("View", 0.0);
		menuBar.addMenu("Select", 0.0);
		menuBar.addMenu("Select.Nodes", 1.0);
		menuBar.addMenu("Select.Edges", 1.1);
		menuBar.addMenu("Layout", 0.0);
		menuBar.addMenu("Apps", 0.0);
		menuBar.addMenu("Tools", 0.0);
		menuBar.addMenu("Help", 0.0);

		menuBar.addSeparator("File", 2.0);
		menuBar.addSeparator("File", 4.0);
		menuBar.addSeparator("File", 6.0);
		menuBar.addSeparator("File", 7.0);
		
		if (!LookAndFeelUtil.isMac())
			menuBar.addSeparator("File", 8.0);

		menuBar.addSeparator("Edit", 2.0);
		menuBar.addSeparator("Edit", 4.0);
		menuBar.addSeparator("Edit", 6.0);

		menuBar.addMenu("Edit.Preferences", 10.0);

		menuBar.addSeparator("View", 2.0);
		menuBar.addSeparator("View", 6.0);
		menuBar.addSeparator("View", 9.0);

		menuBar.addSeparator("Select", 2.0);
		menuBar.addSeparator("Select", 4.0);
		menuBar.addSeparator("Select", 6.0);

		menuBar.addSeparator("Layout", 2.0);
		menuBar.addSeparator("Layout", 4.0);
		menuBar.addSeparator("Layout", 6.0);

		menuBar.addSeparator("Apps", 2.0);

		menuBar.addSeparator("Help", 2.0);
		menuBar.addSeparator("Help", 6.0);
		menuBar.addSeparator("Help", 9.0);

		toolBar.addSeparator(2.0f);
		toolBar.addSeparator(3.0f);
		toolBar.addSeparator(4.0f);
		toolBar.addSeparator(6.0f);
		toolBar.addSeparator(8.0f);
//		toolBar.addSeparator(10.0f);
	}

	public JMenu getJMenu(String s) {
		return menuBar.getMenu(s);
	}

	public JMenuBar getJMenuBar() {
		return menuBar;
	}

	public JToolBar getJToolBar() {
		return toolBar;
	}

	public void removeAction(CyAction action) {
		if (action.isInMenuBar())
			menuBar.removeAction(action);

		if (action.isInToolBar())
			toolBar.removeAction(action);
	}

	public void addAction(CyAction action) {
		if (action.isInMenuBar())
			menuBar.addAction(action);

		if (action.isInToolBar())
			toolBar.addAction(action);
	}

	public JMenuBar createDummyMenuBar() {
		final JMenuBar dummy = new JMenuBar();
		
		if (menuBar != null) {
			final int menuCount = menuBar.getMenuCount();
			
			for (int i = 0; i < menuCount; i++) {
				final JMenu menu = menuBar.getMenu(i);
				
				if (menu != null)
					dummy.add(new JMenu(menu.getText()));
			}
		}
		
		return dummy;
	}
}
