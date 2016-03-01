package org.cytoscape.internal.actions;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.PopupMenuEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.destroy.DestroyNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
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

/**
 * This action simply delegates to {@link DestroyNetworkViewTaskFactory}, but it is necessary
 * because we want all views from selected networks to be deleted when the user right-clicks the Network list.
 * If the task factory is used directly only the selected views are deleted,
 * which is not what we want if a network has multiples views.
 */
@SuppressWarnings("serial")
public class DestroyNetworkViewsAction extends AbstractCyAction {

	private final CyServiceRegistrar serviceRegistrar;

	public DestroyNetworkViewsAction(final float menuGravity, final CyServiceRegistrar serviceRegistrar) {
		super("Destroy Views");
		this.serviceRegistrar = serviceRegistrar;
		
		setMenuGravity(menuGravity);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final List<CyNetwork> networks = serviceRegistrar.getService(CyApplicationManager.class).getSelectedNetworks();
		final CyNetworkViewManager netViewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
		final Set<CyNetworkView> views = new HashSet<>();
		
		for (CyNetwork n : networks)
			views.addAll(netViewManager.getNetworkViews(n));
		
		if (!views.isEmpty()) {
			final DestroyNetworkViewTaskFactory factory =
					serviceRegistrar.getService(DestroyNetworkViewTaskFactory.class);
			final DialogTaskManager taskManager = serviceRegistrar.getService(DialogTaskManager.class);
			
			taskManager.execute(factory.createTaskIterator(views));
		}
	}
	
	@Override
	public void updateEnableState() {
		final List<CyNetwork> networks = serviceRegistrar.getService(CyApplicationManager.class).getSelectedNetworks();
		final CyNetworkViewManager netViewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
		
		for (CyNetwork n : networks) {
			if (netViewManager.viewExists(n)) {
				setEnabled(true);
				return;
			}
		}
		
		setEnabled(false);
	}
	
	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		updateEnableState();
	}
}
