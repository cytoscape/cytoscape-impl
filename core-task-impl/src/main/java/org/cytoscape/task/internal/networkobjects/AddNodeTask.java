package org.cytoscape.task.internal.networkobjects;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class AddNodeTask extends AbstractTask implements ObservableTask {
	CyNode newNode;

	@Tunable(description="Network to add a node to", context="nogui")
	public CyNetwork network = null;

	@Tunable(description="Name of the node to add", context="nogui")
	public String name = null;

	public AddNodeTask() {
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		if (network == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Network must be specified for add command");
			return;
		}			

		newNode = network.addNode();
		if (name != null) {
			network.getRow(newNode).set(CyNetwork.NAME, name);
		}
		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Added node "+newNode.toString()+" to network");

	}

	public Object getResults(Class type) {
		if (type.equals(CyNode.class)) {
			return newNode;
		} else if (type.equals(String.class)){
			if (newNode == null)
				return "<none>";
			return newNode.toString();
		}
		return newNode;
	}
}
