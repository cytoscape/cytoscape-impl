package org.cytoscape.task.internal.view;

import java.util.Collection;
import java.util.Collections;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

public class SetCurrentNetworkViewTask extends AbstractTask {
	
	@Tunable(
			description = "Network",
			longDescription = StringToModel.CY_NETWORK_LONG_DESCRIPTION,
			exampleStringValue = StringToModel.CY_NETWORK_EXAMPLE_STRING,
			context = "nogui"
	)
	public CyNetwork network = null;

	@Tunable(
			description = "View to set as the current view",
			longDescription = StringToModel.CY_NETWORK_VIEW_LONG_DESCRIPTION,
			exampleStringValue = StringToModel.CY_NETWORK_VIEW_EXAMPLE_STRING,
			context = "nogui"
	)
	public CyNetworkView view = null;
	
	private final CyServiceRegistrar serviceRegistrar;

	public SetCurrentNetworkViewTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Set Current Network View");
		
		if (view == null) {
			Collection<CyNetworkView> viewList = network != null
					? serviceRegistrar.getService(CyNetworkViewManager.class).getNetworkViews(network)
					: Collections.emptyList();

			if (!viewList.isEmpty())
				view = viewList.iterator().next();
		}

		tm.showMessage(TaskMonitor.Level.INFO, "Setting current network view to " + view);
		serviceRegistrar.getService(CyApplicationManager.class).setCurrentNetworkView(view);
	}
}
