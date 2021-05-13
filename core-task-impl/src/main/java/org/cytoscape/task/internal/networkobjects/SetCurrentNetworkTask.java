package org.cytoscape.task.internal.networkobjects;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

public class SetCurrentNetworkTask extends AbstractTask implements ObservableTask {
	CyApplicationManager appManager;

	@Tunable(description="Network to make current", 
	         longDescription=StringToModel.CY_NETWORK_LONG_DESCRIPTION,
					 exampleStringValue=StringToModel.CY_NETWORK_EXAMPLE_STRING,
	         context="nogui")
	public CyNetwork network;

	public SetCurrentNetworkTask(CyApplicationManager appManager) {
		this.appManager = appManager;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		appManager.setCurrentNetwork(network);
		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Set current network to "+network.toString());
	}

	@Override
	@SuppressWarnings({"rawtypes","unchecked"})
	public Object getResults(Class type) {
		if (type.equals(String.class)){
			return "Set current network to "+network.toString();
		} else if (type.equals(JSONResult.class)) {
			JSONResult res = () -> { return "{}"; };
			return res;
		}
		return null;
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, JSONResult.class);
	}

}
