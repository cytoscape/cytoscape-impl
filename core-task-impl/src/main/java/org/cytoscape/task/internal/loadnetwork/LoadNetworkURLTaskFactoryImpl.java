package org.cytoscape.task.internal.loadnetwork;

import java.net.URISyntaxException;
import java.net.URL;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;

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

/**
 * Task to load a new network.
 */
public class LoadNetworkURLTaskFactoryImpl extends AbstractTaskFactory implements LoadNetworkURLTaskFactory {

	private final CyServiceRegistrar serviceRegistrar;

	public LoadNetworkURLTaskFactoryImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator() {
		// Usually we need to create view, so expected number is 2.
		return new TaskIterator(2, new LoadNetworkURLTask(serviceRegistrar));
	}

	public TaskIterator createTaskIterator(final URL url) {
		return loadCyNetworks(url);
	}

	@Override
	public TaskIterator createTaskIterator(final URL url, TaskObserver observer) {
		return loadCyNetworks(url);
	}
	
	@Override
	public TaskIterator loadCyNetworks(final URL url) {
		// Code adapted from LoadNetworkURLTask
		// TODO: Refactor to avoid duplication of code
		final String urlString = url.getFile();
		final String[] parts = urlString.split("/");
		final String name = parts[parts.length-1];
		CyNetworkReader reader = null;
		
		try {
			reader = serviceRegistrar.getService(CyNetworkReaderManager.class)
					.getReader(url.toURI(), url.toURI().toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return new TaskIterator(2, new LoadNetworkTask(reader, name, serviceRegistrar));
	}
}
