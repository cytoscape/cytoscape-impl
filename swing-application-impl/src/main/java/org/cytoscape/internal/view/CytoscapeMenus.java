/*
 File: CytoscapeMenus.java

 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.internal.view;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

import org.cytoscape.application.swing.CyAction;

public class CytoscapeMenus {

	final private CytoscapeMenuBar menuBar;
	final private CytoscapeToolBar toolBar;

	public CytoscapeMenus(CytoscapeMenuBar menuBar, CytoscapeToolBar toolBar) {
		this.menuBar = menuBar;
		this.toolBar = toolBar;

		menuBar.addMenu("File", 0.0);
		menuBar.addMenu("File.Recent Session", 0.0);
		menuBar.addMenu("File.New", 0.5);
		menuBar.addMenu("File.New.Network", 0.0);
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
		if (!isMac())
			menuBar.addSeparator("File", 8.0);

		menuBar.addSeparator("Edit", 2.0);
		menuBar.addSeparator("Edit", 4.0);
		menuBar.addSeparator("Edit", 6.0);

		menuBar.addMenu("Edit.Preferences", 10.0);

		menuBar.addSeparator("View", 2.0);
		menuBar.addSeparator("View", 6.0);

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
		toolBar.addSeparator(4.0f);
		toolBar.addSeparator(6.0f);
		toolBar.addSeparator(8.0f);
		toolBar.addSeparator(10.0f);
	}

	private boolean isMac() {
		return System.getProperty("os.name").startsWith("Mac OS X");
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
}
