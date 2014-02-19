package org.cytoscape.task.internal.select;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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
 


import java.util.Collection;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTask;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;


public abstract class AbstractSelectTask extends AbstractTask {
	protected final CyNetworkViewManager networkViewManager;
	protected final SelectUtils selectUtils;
	protected final CyEventHelper eventHelper;
	protected CyNetwork network;

	public AbstractSelectTask(final CyNetwork net, final CyNetworkViewManager networkViewManager, final CyEventHelper eventHelper) {
		// super(net);
		this.network = net;
		this.networkViewManager = networkViewManager;
		this.selectUtils = new SelectUtils();
		this.eventHelper = eventHelper;
	}

	protected final void updateView() {
		// This is necessary, otherwise, this does not update presentation!
		eventHelper.flushPayloadEvents();
		
		final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(network);
		CyNetworkView view = null;
		if(views.size() != 0)
			view = views.iterator().next();
		
		if (view != null)
			view.updateView();
	}
}
