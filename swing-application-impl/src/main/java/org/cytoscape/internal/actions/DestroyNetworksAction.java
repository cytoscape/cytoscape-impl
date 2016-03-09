package org.cytoscape.internal.actions;

import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.event.PopupMenuEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.internal.view.NetworkMainPanel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.destroy.DestroyNetworkTaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;

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

@SuppressWarnings("serial")
public class DestroyNetworksAction extends AbstractCyAction {
	
	private final NetworkMainPanel netPanel;
	private final CyServiceRegistrar serviceRegistrar;

	public DestroyNetworksAction(final float menuGravity, final NetworkMainPanel netPanel,
			final CyServiceRegistrar serviceRegistrar) {
		super("Destroy Networks");
		this.netPanel = netPanel;
		this.serviceRegistrar = serviceRegistrar;
		
		setMenuGravity(menuGravity);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final Set<CyNetwork> subNetworks = getSelectedSubNetworks();
		
		if (!subNetworks.isEmpty()) {
			final DestroyNetworkTaskFactory factory = serviceRegistrar.getService(DestroyNetworkTaskFactory.class);
			final DialogTaskManager taskManager = serviceRegistrar.getService(DialogTaskManager.class);
			
			taskManager.execute(factory.createTaskIterator(subNetworks));
		}
	}

	@Override
	public void updateEnableState() {
		final boolean collectionsSelected = netPanel.countSelectedRootNetworks() > 0;
		final boolean networksSelected = netPanel.countSelectedSubNetworks() > 0;
		
		setEnabled(collectionsSelected || networksSelected);
		
		setName(
				"Destroy Selected " +
				(networksSelected ? "Networks" : "") +
				(collectionsSelected && networksSelected ? " and " : "") +
				(collectionsSelected ? "Collections" : "")
		);
	}
	
	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		updateEnableState();
	}
	
	private Set<CyNetwork> getSelectedSubNetworks() {
		// Includes subnetworks from selected collections as well
		return netPanel.getSelectedNetworks(true);
	}
}
