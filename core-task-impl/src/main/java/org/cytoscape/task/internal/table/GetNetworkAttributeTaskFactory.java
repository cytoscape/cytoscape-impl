package org.cytoscape.task.internal.table;

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
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

public class GetNetworkAttributeTaskFactory extends AbstractTaskFactory {
	
	public static final String COMMAND_EXAMPLE_JSON = "["
			+ "  { \"name\": \"Object 1\", \"SUID\": 101 }, "
			+ "  { \"name\": \"Object 2\", \"SUID\": 102 }"
			+ "]";
	
	private final Class<?> type;
	private final CyServiceRegistrar serviceRegistrar;
	
	public GetNetworkAttributeTaskFactory(Class <? extends CyIdentifiable> type, CyServiceRegistrar serviceRegistrar) {
		this.type = type;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		if (type.equals(CyNetwork.class))
			return new TaskIterator(new GetNetworkAttributeTask(serviceRegistrar));
		else if (type.equals(CyEdge.class))
			return new TaskIterator(new GetEdgeAttributeTask(serviceRegistrar));
		else if (type.equals(CyNode.class))
			return new TaskIterator(new GetNodeAttributeTask(serviceRegistrar));
		return null;
	}
}
