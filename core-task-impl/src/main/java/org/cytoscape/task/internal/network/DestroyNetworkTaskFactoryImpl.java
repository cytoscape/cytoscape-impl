package org.cytoscape.task.internal.network;

import java.util.ArrayList;
import java.util.Collection;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkCollectionTaskFactory;
import org.cytoscape.task.destroy.DestroyNetworkTaskFactory;
import org.cytoscape.work.TaskFactory;
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

public class DestroyNetworkTaskFactoryImpl extends AbstractNetworkCollectionTaskFactory
		implements DestroyNetworkTaskFactory, TaskFactory {

	private final CyServiceRegistrar serviceRegistrar;

	public DestroyNetworkTaskFactoryImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator(Collection<CyNetwork> networks) {
		return new TaskIterator(new DestroyNetworkTask(networks, serviceRegistrar));
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new DestroyNetworkTask(new ArrayList<>(), serviceRegistrar));
	}

	@Override
	public boolean isReady() {
		return true;
	}
}
