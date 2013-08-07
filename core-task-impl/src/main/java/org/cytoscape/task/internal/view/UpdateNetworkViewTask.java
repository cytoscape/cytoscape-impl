package org.cytoscape.task.internal.view;

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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class UpdateNetworkViewTask extends AbstractTask {
	final CyApplicationManager appMgr;
	final CyNetworkViewManager viewMgr;

	@Tunable(description="Network view to update", context="nogui")
	public CyNetwork network = null;

	public UpdateNetworkViewTask(final CyApplicationManager appMgr,
	                             final CyNetworkViewManager viewMgr) {
		this.appMgr = appMgr;
		this.viewMgr = viewMgr;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (network == null) network = appMgr.getCurrentNetwork();
		Collection<CyNetworkView> views = viewMgr.getNetworkViews(network);
		for (CyNetworkView view: views) {
			view.updateView();
			taskMonitor.showMessage(TaskMonitor.Level.INFO, "Updated view: "+view);
		}
	}
}
