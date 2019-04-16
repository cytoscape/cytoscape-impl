package org.cytoscape.internal.actions;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.internal.view.CytoPanelImpl;
import org.cytoscape.internal.view.CytoPanelNameInternal;
import org.cytoscape.internal.view.CytoscapeDesktop;
import org.cytoscape.service.util.CyServiceRegistrar;

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
public class CytoPanelAction extends AbstractCyAction {
	
	private final CytoPanelNameInternal position;
	private final CytoscapeDesktop desktop;
	private final CyServiceRegistrar serviceRegistrar;

	public CytoPanelAction(
			CytoPanelNameInternal position,
			CytoscapeDesktop desktop,
			float menuGravity,
			CyServiceRegistrar serviceRegistrar
	) {
		super("Show " + position.getTitle());

		this.position = position;
		this.desktop = desktop;
		this.serviceRegistrar = serviceRegistrar;

		setPreferredMenu("View");
		setMenuGravity(menuGravity);
		useCheckBoxMenuItem = true;
	}

	/**
	 * Toggles the cytopanel state.  
	 */
	@Override
	public void actionPerformed(ActionEvent ev) {
		CytoPanelImpl cytoPanel = (CytoPanelImpl) desktop.getCytoPanel(position);

		if (cytoPanel.isRemoved())
			desktop.showCytoPanel(cytoPanel);
		else
			desktop.removeCytoPanel(cytoPanel);
	} 

	@Override
	public void menuSelected(MenuEvent me) {
		updateEnableState();
		JCheckBoxMenuItem item = getThisItem();
		CytoPanel cytoPanel = desktop.getCytoPanel(position);

		if (item != null && cytoPanel instanceof CytoPanelImpl)
			item.setSelected(cytoPanel.getCytoPanelComponentCount() > 0 && !((CytoPanelImpl) cytoPanel).isRemoved());
	}
	
	@Override
	public void updateEnableState() {
		CytoPanel cytoPanel = desktop.getCytoPanel(position);
		setEnabled(cytoPanel instanceof CytoPanelImpl && cytoPanel.getThisComponent() != null
				&& cytoPanel.getCytoPanelComponentCount() > 0);
	}

	private JCheckBoxMenuItem getThisItem() {
		JMenu menu = serviceRegistrar.getService(CySwingApplication.class).getJMenu(preferredMenu);
		
		for (int i = 0; i < menu.getItemCount(); i++) {
			JMenuItem item = menu.getItem(i);
			
			if (item instanceof JCheckBoxMenuItem && item.getText().equals(getName()))
				return (JCheckBoxMenuItem) item;
		}
		
		return null;
	}
}
