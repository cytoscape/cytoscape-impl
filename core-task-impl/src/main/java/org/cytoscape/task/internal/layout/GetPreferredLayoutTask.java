package org.cytoscape.task.internal.layout;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
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

public class GetPreferredLayoutTask extends AbstractTask implements ObservableTask {

	private CyLayoutAlgorithm preferredLayout;
	private final CyServiceRegistrar serviceRegistrar;
	
	@Tunable(description="Gets the name of the current preferred layout", context="nogui", longDescription="Gets the name of the current preferred layout")
	public CyNetwork network = null;


	public GetPreferredLayoutTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) {
		preferredLayout = serviceRegistrar.getService(CyLayoutAlgorithmManager.class).getDefaultLayout();

		if (preferredLayout != null)
			tm.showMessage(TaskMonitor.Level.INFO, "Preferred layout is " + preferredLayout.getName());
		else
			tm.showMessage(TaskMonitor.Level.WARN, "...but it's not available!");
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getResults(Class type) {
		if (type.equals(String.class))
			return preferredLayout.getName();
		else if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {
				if (preferredLayout == null) { 
					return "{ }";
				} else {
					return "\"" + preferredLayout.getName() + "\"";	
			}};
			return res;
		}else if (preferredLayout == null) {
			return null;
		}
		return preferredLayout;
	}
	
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, JSONResult.class);
	}
}
