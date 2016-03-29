package org.cytoscape.internal.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.PopupMenuEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
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
 * This action simply delegates to {@link CreateNetworkViewTaskFactory}, but it is necessary
 * because we want to change the action name dynamically.
 */
@SuppressWarnings("serial")
public class CreateNetworkViewsAction extends AbstractCyAction {

	private final CyServiceRegistrar serviceRegistrar;

	public CreateNetworkViewsAction(final float menuGravity, final CyServiceRegistrar serviceRegistrar) {
		super("Create Views");
		this.serviceRegistrar = serviceRegistrar;
		
		setMenuGravity(menuGravity);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final List<CyNetwork> networks = serviceRegistrar.getService(CyApplicationManager.class).getSelectedNetworks();
		
		if (!networks.isEmpty()) {
			final CreateNetworkViewTaskFactory factory =
					serviceRegistrar.getService(CreateNetworkViewTaskFactory.class);
			final DialogTaskManager taskManager = serviceRegistrar.getService(DialogTaskManager.class);
			
			taskManager.execute(factory.createTaskIterator(networks));
		}
	}
	
	@Override
	public void updateEnableState() {
		final List<CyNetwork> networks = serviceRegistrar.getService(CyApplicationManager.class).getSelectedNetworks();
		final List<CyNetwork> networksWithoutViews = new ArrayList<>();
		final CyNetworkViewManager netViewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
		
		// TODO Remove when multiple views per network are fully supported
		for (CyNetwork n : networks) {
			if (!netViewManager.viewExists(n))
				networksWithoutViews.add(n);
		}
		
		int count = networksWithoutViews.size();
		
		if (count > 0)
			setName("Create " + count + " View" + (count == 1 ? "" : "s"));
		else
			setName("Create Views");
		
		setEnabled(count > 0);
	}
	
	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		updateEnableState();
	}
}
