package org.cytoscape.task.internal.select;

import java.util.Collection;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

public abstract class AbstractSelectTask extends AbstractTask {
	
	protected CyNetwork network;
	
	protected final SelectUtils selectUtils;
	protected final CyServiceRegistrar serviceRegistrar;

	public AbstractSelectTask(CyNetwork net, CyServiceRegistrar serviceRegistrar) {
		this.network = net;
		this.selectUtils = new SelectUtils(serviceRegistrar);
		this.serviceRegistrar = serviceRegistrar;
	}

	protected CyNetworkView getNetworkView(CyNetwork network) {
		CyNetworkView view = null;
		CyNetworkView currentView = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView();		
		
		if (currentView != null && currentView.getModel().equals(network)) {
			view = currentView;
		} else {
			Collection<CyNetworkView> views = serviceRegistrar.getService(CyNetworkViewManager.class)
					.getNetworkViews(network);
			
			if (!views.isEmpty())
				view = views.iterator().next();
		}
		
		return view;
	}
	
	protected final void updateView() {
		// This is necessary, otherwise this does not update presentation!
		serviceRegistrar.getService(CyEventHelper.class).flushPayloadEvents();
		
		/*
		final Collection<CyNetworkView> views = viewManager.getNetworkViews(network);
		CyNetworkView view = null;
		if(views.size() != 0)
			view = views.iterator().next();
		
		if (view != null)
			view.updateView();
		*/
	}
}
