package org.cytoscape.internal.view;

import static org.cytoscape.application.swing.ActionEnableSupport.ENABLE_FOR_NETWORK_AND_VIEW;
import static org.cytoscape.application.swing.ActionEnableSupport.ENABLE_FOR_SELECTED_NODES_OR_EDGES;
import static org.cytoscape.internal.util.ViewUtil.invokeOnEDT;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.IN_TOOL_BAR;

import java.awt.Component;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JSeparator;
import javax.swing.JToolBar;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.util.swing.LookAndFeelUtil;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class CytoscapeMenus {

	final private CytoscapeMenuBar menuBar;
	final private CytoscapeToolBar toolBar;
	final private Set<CyAction> viewFrameActions;
	
	public CytoscapeMenus(CytoscapeMenuBar menuBar, CytoscapeToolBar toolBar) {
		this.menuBar = menuBar;
		this.toolBar = toolBar;
		
		viewFrameActions = new LinkedHashSet<>();

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
	
	public JToolBar createViewFrameToolBar() {
		JToolBar viewToolBar = null;
		final int total = toolBar.getComponentCount();
		boolean addSeparator = false;
		int buttonsAfterSeparator = 0;
		
		for (int i = 0; i < total; i++) {
			final Component c = toolBar.getComponent(i);
			
			if (c instanceof JButton && ((JButton)c).getAction() instanceof CyAction) {
				final JButton btn = ((JButton)c);
				final CyAction action = (CyAction) btn.getAction();
				
				if (viewFrameActions.contains(action)) {
					if (viewToolBar == null) {
						viewToolBar = new JToolBar();
						viewToolBar.setBorder(toolBar.getBorder());
					}
					
					if (addSeparator) {
						viewToolBar.addSeparator();
						addSeparator = false;
						buttonsAfterSeparator = 0;
					}
					
					final JButton newBtn = CytoscapeToolBar.createToolBarButton(action);
					viewToolBar.add(newBtn);
					buttonsAfterSeparator++;
				}
			} else if (c instanceof JSeparator && buttonsAfterSeparator > 0) {
				addSeparator = true;
			}
		}
		
		if (viewToolBar != null && viewToolBar.getComponentCount() > 0 &&
				viewToolBar.getComponentAtIndex(viewToolBar.getComponentCount() - 1) instanceof JSeparator)
			viewToolBar.remove(viewToolBar.getComponentCount() - 1);
		
		return viewToolBar;
	}

	public void removeAction(CyAction action) {
		invokeOnEDT(() -> {
			if (action.isInMenuBar())
				menuBar.removeAction(action);
	
			if (action.isInToolBar())
				toolBar.removeAction(action);
		});
	}

	public void addAction(CyAction action, Map<?, ?> props) {
		invokeOnEDT(() -> {
			if (action.isInMenuBar())
				menuBar.addAction(action);
	
			if (action.isInToolBar())
				toolBar.addAction(action);
			
			if ("true".equals(props.get(IN_TOOL_BAR)) && 
					(ENABLE_FOR_NETWORK_AND_VIEW.equals(props.get(ENABLE_FOR)) ||
							ENABLE_FOR_SELECTED_NODES_OR_EDGES.equals(props.get(ENABLE_FOR))))
				viewFrameActions.add(action);
		});
	}

	public void setMenuBarVisible(final boolean b) {
		if (menuBar != null) {
			final int menuCount = menuBar.getMenuCount();

			for (int i = 0; i < menuCount - 1; i++) {
				final JMenu menu = menuBar.getMenu(i);

				if (menu != null && menu.isVisible() != b)
					menu.setVisible(b);
			}
		}
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
