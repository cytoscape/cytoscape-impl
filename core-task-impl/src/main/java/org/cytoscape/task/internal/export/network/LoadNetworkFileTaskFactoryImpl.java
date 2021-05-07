package org.cytoscape.task.internal.export.network;

import java.io.File;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;

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

/**
 * Task to load a new network.
 */
public class LoadNetworkFileTaskFactoryImpl extends AbstractTaskFactory implements LoadNetworkFileTaskFactory {

	private final CyServiceRegistrar serviceRegistrar;

	public LoadNetworkFileTaskFactoryImpl(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		// Load, visualize, and layout.
		return new TaskIterator(3, new LoadNetworkFileTask(serviceRegistrar));
	}

	@Override
	public TaskIterator createTaskIterator(File file) {
		return createTaskIterator(file, null);
	}

	@Override
	public TaskIterator createTaskIterator(File file, TaskObserver observer) {
		CyNetworkReader reader = serviceRegistrar.getService(CyNetworkReaderManager.class)
				.getReader(file.toURI(), file.toURI().toString());

		return new TaskIterator(3, new LoadNetworkTask(reader, file.getName(), serviceRegistrar));
	}
}
