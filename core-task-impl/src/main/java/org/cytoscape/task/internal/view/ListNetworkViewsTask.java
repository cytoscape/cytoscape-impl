package org.cytoscape.task.internal.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

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

public class ListNetworkViewsTask extends AbstractTask implements ObservableTask {
	
	@Tunable(
			description = "Network",
			longDescription = StringToModel.CY_NETWORK_LONG_DESCRIPTION,
			exampleStringValue = StringToModel.CY_NETWORK_EXAMPLE_STRING,
			context = "nogui"
	)
	public CyNetwork network;

	private List<CyNetworkView> views;
	private final CyServiceRegistrar serviceRegistrar;

	public ListNetworkViewsTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (network == null)
			network = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();
		
		views = new ArrayList<>(serviceRegistrar.getService(CyNetworkViewManager.class).getNetworkViews(network));
		taskMonitor.showMessage(TaskMonitor.Level.INFO,
				"Views for network " + (network != null ? DataUtils.getNetworkName(network) : null));
		
		for (CyNetworkView view : views) {
			taskMonitor.showMessage(TaskMonitor.Level.INFO, "    " + view);
			return;
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getResults(Class type) {
		if (type == String.class)
			return DataUtils.convertData(views);
		
		if (type == JSONResult.class) {
			String json = serviceRegistrar.getService(CyJSONUtil.class).cyIdentifiablesToJson(views);
			JSONResult res = () -> { return json; };
			
			return res;
		}
		
		 return views;
	}
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(List.class, String.class, JSONResult.class);
	}
}
