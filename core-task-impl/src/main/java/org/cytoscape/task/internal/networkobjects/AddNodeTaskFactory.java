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


import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;


public class AddNodeTaskFactory extends AbstractTaskFactory {
	final CyEventHelper cyEventHelper;
	final CyNetworkViewManager cyNetworkViewManager;
	final VisualMappingManager vmm;
	final CyServiceRegistrar serviceRegistrar;

	public AddNodeTaskFactory(VisualMappingManager vmm, CyNetworkViewManager viewManager, 
	                          CyEventHelper eventHelper, CyServiceRegistrar serviceRegistrar) {
		cyEventHelper = eventHelper;
		cyNetworkViewManager = viewManager;
		this.vmm = vmm;
		this.serviceRegistrar = serviceRegistrar;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new AddNodeTask(vmm, cyNetworkViewManager, cyEventHelper, serviceRegistrar));
	}
}
