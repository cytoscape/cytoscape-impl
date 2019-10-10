package org.cytoscape.search.internal;

import java.util.Collection;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;

/*
 * #%L
 * Cytoscape Search Impl (search-impl)
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

public class ViewUpdatorImpl implements ViewUpdator {

	private final CyServiceRegistrar serviceRegistrar;

	/**
	 * Constructor
	 * @param serviceRegistrar used to get views in {@link #updateView()}
	 */
	public ViewUpdatorImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	/**
	 * If view(s) exists for the current network, update them.
	 * 
	 * @param network used to find any views displaying this network
	 */
	@Override
	public void updateView(CyNetwork network) {
		final CyNetworkViewManager viewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
		final CyApplicationManager appManager = serviceRegistrar.getService(CyApplicationManager.class);
		
		final Collection<CyNetworkView> views = viewManager.getNetworkViews(network);
		
		if (views != null && views.size() != 0) {
			CyNetworkView targetView = views.iterator().next();
			if (targetView != null)
				targetView.updateView();
		}

		final CyNetworkView view = appManager.getCurrentNetworkView();
		
		if (view != null)
			view.updateView();
	}

}
