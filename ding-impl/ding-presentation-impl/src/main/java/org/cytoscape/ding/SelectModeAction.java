package org.cytoscape.ding;

import static org.cytoscape.ding.DVisualLexicon.NETWORK_EDGE_SELECTION;
import static org.cytoscape.ding.DVisualLexicon.NETWORK_NODE_SELECTION;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

@SuppressWarnings("serial")
public class SelectModeAction extends AbstractCyAction {

	public static final String NODES = "Nodes Only";
	public static final String EDGES = "Edges Only";
	public static final String ALL = "Nodes and Edges";
	
	private final CyServiceRegistrar serviceRegistrar;

	public SelectModeAction(final String name, float gravity, final CyServiceRegistrar serviceRegistrar) {
		super(name);
		this.serviceRegistrar = serviceRegistrar;
		
		useCheckBoxMenuItem = true;
		setPreferredMenu("Select.Mouse Drag Selects");
		setMenuGravity(gravity);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		final CyNetworkView view = applicationManager.getCurrentNetworkView();

		if (view != null) {
			if (name.equalsIgnoreCase(NODES)) {
				view.setLockedValue(NETWORK_NODE_SELECTION, Boolean.TRUE);
				view.setLockedValue(NETWORK_EDGE_SELECTION, Boolean.FALSE);
			} else if (name.equalsIgnoreCase(EDGES)) {
				view.setLockedValue(NETWORK_NODE_SELECTION, Boolean.FALSE);
				view.setLockedValue(NETWORK_EDGE_SELECTION, Boolean.TRUE);
			} else if (name.equalsIgnoreCase(ALL)) {
				view.setLockedValue(NETWORK_NODE_SELECTION, Boolean.TRUE);
				view.setLockedValue(NETWORK_EDGE_SELECTION, Boolean.TRUE);
			}
		}
	}
	
	@Override
	public boolean isEnabled() {
		return serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView() != null;
	}
	
	@Override
	public void menuSelected(MenuEvent e) {
		final JCheckBoxMenuItem item = getThisItem(); 

		if (item != null) {
			final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
			final CyNetworkView view = applicationManager.getCurrentNetworkView();
			
			if (view == null)
				item.setSelected(false);
			else if (view.getVisualProperty(NETWORK_NODE_SELECTION) && view.getVisualProperty(NETWORK_EDGE_SELECTION))
				item.setSelected(name.equalsIgnoreCase(ALL));
			else if (view.getVisualProperty(NETWORK_NODE_SELECTION))
				item.setSelected(name.equalsIgnoreCase(NODES));
			else if (view.getVisualProperty(NETWORK_EDGE_SELECTION))
				item.setSelected(name.equalsIgnoreCase(EDGES));
			else
				item.setSelected(false);
		}
		
		updateEnableState();
	}
	
	private JCheckBoxMenuItem getThisItem() {
		final CySwingApplication swingApplication = serviceRegistrar.getService(CySwingApplication.class);
		final JMenu menu = swingApplication.getJMenu(preferredMenu);
		
		for (int i = 0; i < menu.getItemCount(); i++) {
			final JMenuItem item = menu.getItem(i);
			
			if (item instanceof JCheckBoxMenuItem && item.getText().equals(getName()))
				return (JCheckBoxMenuItem) item;
		}
		
		return null;
	}
}
