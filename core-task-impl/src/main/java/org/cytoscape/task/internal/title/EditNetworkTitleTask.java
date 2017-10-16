package org.cytoscape.task.internal.title;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.AbstractNetworkTask;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.undo.UndoSupport;
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

public class EditNetworkTitleTask extends AbstractNetworkTask implements TunableValidator, ObservableTask {
	
	@ProvidesTitle
	public String getTitle() {
		return "Rename Network";
	}

	@Tunable(description = "New title for network")
	public String name;

	@Tunable(description = "Network to rename", context = "nogui")
	public CyNetwork sourceNetwork;

	private final CyServiceRegistrar serviceRegistrar;

	public EditNetworkTitleTask(CyNetwork net, CyServiceRegistrar serviceRegistrar) {
		super(net);
		this.serviceRegistrar = serviceRegistrar;
		
		name = network.getRow(network).get(CyNetwork.NAME, String.class);
	}

	@Override
	public ValidationState getValidationState(final Appendable errMsg) {
		name = name.trim();
		
		// Check if the network tile already existed
		boolean titleAlreayExisted = false;
		String newTitle = serviceRegistrar.getService(CyNetworkNaming.class).getSuggestedNetworkTitle(name);
		
		if (!newTitle.equalsIgnoreCase(name))
			titleAlreayExisted = true;

		if (titleAlreayExisted) {
			// Inform user duplicated network title!
			try {
				errMsg.append("Duplicated network name.");
			} catch (Exception e) {
				System.out.println("Warning: Duplicated network name.");
			}
			
			return ValidationState.INVALID;
		}

		return ValidationState.OK;
	}
	
	@Override
	public void run(TaskMonitor e) {
		e.setProgress(0.0);
		
		if (sourceNetwork == null)
			sourceNetwork = network;
		
		final String oldTitle = network.getRow(sourceNetwork).get(CyNetwork.NAME, String.class);
		e.setProgress(0.3);
		
		network.getRow(sourceNetwork).set(CyNetwork.NAME, name);
		e.setProgress(0.6);
		
		serviceRegistrar.getService(UndoSupport.class).postEdit(new NetworkTitleEdit(sourceNetwork, oldTitle));
		e.setProgress(1.0);
	}

	public Object getResults(Class type) {
		if (type.equals(CyNetwork.class)) {
			return sourceNetwork;
		} else if (type.equals(String.class)){
			if (sourceNetwork == null)
				return "<none>";
			return "Network "+sourceNetwork.getSUID()+" renamed to "+name;
		}  else if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {if (sourceNetwork == null) 
				return "{}";
			else {
				CyJSONUtil cyJSONUtil = serviceRegistrar.getService(CyJSONUtil.class);
				return cyJSONUtil.toJson(sourceNetwork);
			}};
			return res;
		}
		return sourceNetwork;
	}
	
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(CyNetwork.class, String.class, JSONResult.class);
	}
}
