package org.cytoscape.task.internal.export.web;

import java.util.Map;

import org.cytoscape.io.write.CySessionWriterFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskIterator;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

public class ExportAsWebArchiveTaskFactory extends AbstractTaskFactory {

	private static final Integer TH = 3000;
	
	private CySessionWriterFactory fullWriterFactory;
	private CySessionWriterFactory simpleWriterFactory;
	private CySessionWriterFactory zippedWriterFactory;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public ExportAsWebArchiveTaskFactory(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	/**
	 * Find correct writer for the web archive.
	 * @param writerFactory
	 * @param props
	 */
	public void registerFactory(CySessionWriterFactory writerFactory, Map<?, ?> props) {
		Object id = props.get(ServiceProperties.ID);

		if (id == null)
			return;

		if (id.equals("fullWebSessionWriterFactory"))
			this.fullWriterFactory = writerFactory;

		if (id.equals("simpleWebSessionWriterFactory"))
			this.simpleWriterFactory = writerFactory;

		if (id.equals("zippedJsonWriterFactory"))
			this.zippedWriterFactory = writerFactory;
	}

	public void unregisterFactory(CySessionWriterFactory writerFactory, Map<?, ?> props) {
	}

	@Override
	public TaskIterator createTaskIterator() {
		var networks = serviceRegistrar.getService(CyNetworkManager.class).getNetworkSet();
		var showWarning = false;
		
		for (var net : networks) {
			final int nodeCount = net.getNodeCount();
			final int edgeCount = net.getEdgeCount();

			if (nodeCount > TH || edgeCount > TH) {
				showWarning = true;
				break;
			}
		}

		var exportTask = new ExportAsWebArchiveTask(fullWriterFactory, simpleWriterFactory, zippedWriterFactory,
				serviceRegistrar);
		
		return showWarning ? new TaskIterator(new ShowWarningTask(exportTask)) : new TaskIterator(exportTask);
	}
	
	@Override
	public boolean isReady() {
		return serviceRegistrar.getService(CyNetworkManager.class).getNetworkSet().size() > 0;
	}
}
