package org.cytoscape.internal.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.event.PopupMenuEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.write.ExportNetworkImageTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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
 * Executes a {@link ExportNetworkImageTaskFactory} for each {@link CyNetworkView} of the selected {@link CyNetwork}.
 */
@SuppressWarnings("serial")
public class ExportImageAction extends AbstractCyAction {

	private final CyServiceRegistrar serviceRegistrar;
	
	public ExportImageAction(final float menuGravity, final CyServiceRegistrar serviceRegistrar) {
		super("Export as Image...");
		this.serviceRegistrar = serviceRegistrar;
		
		setMenuGravity(menuGravity);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		List<CyNetwork> networks = serviceRegistrar.getService(CyApplicationManager.class).getSelectedNetworks();
		
		if (networks.size() == 1) {
			ExportNetworkImageTaskFactory factory = serviceRegistrar.getService(ExportNetworkImageTaskFactory.class);
			TaskIterator taskIterator = new TaskIterator();
			Collection<CyNetworkView> views = serviceRegistrar.getService(CyNetworkViewManager.class)
					.getNetworkViews(networks.get(0));
			
			if (!views.isEmpty()) {
				for (CyNetworkView nv : views)
					taskIterator.append(factory.createTaskIterator(nv));
				
				serviceRegistrar.getService(DialogTaskManager.class).execute(taskIterator);
			}
		}
	}
	
	@Override
	public void updateEnableState() {
		List<CyNetwork> networks = serviceRegistrar.getService(CyApplicationManager.class).getSelectedNetworks();
		
		setEnabled(networks.size() == 1
				&& serviceRegistrar.getService(CyNetworkViewManager.class).viewExists(networks.get(0)));
	}
	
	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		updateEnableState();
	}
}
