package org.cytoscape.view.vizmap.gui.internal.action;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

import java.util.Map;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.PopupMenuEvent;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.DefaultViewEditor;
import org.cytoscape.view.vizmap.gui.VizMapGUI;
import org.cytoscape.view.vizmap.gui.internal.theme.ThemeManager;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMainPanel;

/**
 * Action class to process commands.
 */
public abstract class AbstractVizMapperAction extends AbstractAction implements CyAction {

	private static final long serialVersionUID = 2123044076909272338L;
	
	protected DefaultViewEditor defViewEditor;
	protected VisualMappingManager vmm;
	protected ThemeManager themeManager;

	protected Properties vizmapUIResource;

	protected String menuLabel;
	protected String iconId;
	protected JMenuItem menuItem;

	private final String name;
	protected final ServicesUtil servicesUtil;
	
	public AbstractVizMapperAction(final String name, final ServicesUtil servicesUtil) {
		super(name);
		this.name = name;
		this.servicesUtil = servicesUtil;
	}

	public void setDefaultAppearenceBuilder(final DefaultViewEditor defViewEditor) {
		this.defViewEditor = defViewEditor;
	}

	public void setMenuLabel(final String menuLabel) {
		this.menuLabel = menuLabel;
	}
	
	public void setIconId(final String iconId) {
		this.iconId = iconId;
	}
	
	public void setThemeManager(final ThemeManager themeManager) {
		this.themeManager = themeManager;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isInMenuBar() {
		return false;
	}

	@Override
	public boolean isInToolBar() {
		return false;
	}

	@Override
	public boolean insertSeparatorBefore() {
		return false;
	}

	@Override
	public boolean insertSeparatorAfter() {
		return false;
	}

	@Override
	public float getMenuGravity() {
		return 0;
	}

	@Override
	public float getToolbarGravity() {
		return 0;
	}

	@Override
	public KeyStroke getAcceleratorKeyStroke() {
		return null;
	}

	@Override
	public String getPreferredMenu() {
		return null;
	}

	@Override
	public boolean useCheckBoxMenuItem() {
		return false;
	}

	@Override
	public Map<String, String> getProperties() {
		return null;
	}
	
	@Override
	public void menuCanceled(MenuEvent e) {
		updateEnableState();
	}

	@Override
	public void menuDeselected(MenuEvent e) {
		updateEnableState();
	}

	@Override
	public void menuSelected(MenuEvent e) {
		updateEnableState();
	}

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		updateEnableState();
	}
	
	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
		// Nothing to do here
	}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		// Nothing to do here
	}
	
	protected VizMapperMainPanel getVizMapperMainPanel() {
		final VizMapGUI gui = servicesUtil.get(VizMapGUI.class);
		
		if (gui instanceof VizMapperMainPanel)
			return (VizMapperMainPanel) gui;
			
		return null;
	}
}
