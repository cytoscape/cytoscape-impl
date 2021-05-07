package org.cytoscape.task.internal.networkobjects;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

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

public class SetPropertiesTaskFactory extends AbstractTaskFactory {
	
	//Set statements don't have to return anything, much like a RESTful PUT.
	public static final String COMMAND_EXAMPLE_JSON = "{}";
	
	private final Class <? extends CyIdentifiable> type;
	private final CyServiceRegistrar serviceRegistrar;

	public SetPropertiesTaskFactory(Class<? extends CyIdentifiable> type, CyServiceRegistrar serviceRegistrar) {
		this.type = type;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator() {
		if (type.equals(CyNetwork.class))
			return new TaskIterator(new SetNetworkPropertiesTask(serviceRegistrar));
		else if (type.equals(CyNode.class))
			return new TaskIterator(new SetNodePropertiesTask(serviceRegistrar));
		else if (type.equals(CyEdge.class))
			return new TaskIterator(new SetEdgePropertiesTask(serviceRegistrar));
		
		return null;
	}
}
