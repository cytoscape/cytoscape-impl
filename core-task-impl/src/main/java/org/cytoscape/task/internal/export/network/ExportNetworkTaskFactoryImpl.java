package org.cytoscape.task.internal.export.network;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.task.write.ExportNetworkTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;

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

public class ExportNetworkTaskFactoryImpl extends AbstractNetworkTaskFactory 
                                          implements ExportNetworkTaskFactory, TaskFactory {

	private final CyServiceRegistrar serviceRegistrar;
	
	public ExportNetworkTaskFactoryImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(2, new CyNetworkWriter(null, true, serviceRegistrar));
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(2, new CyNetworkWriter(network, false, serviceRegistrar));
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork network, File file) {
		final Map<String, Object> m = new HashMap<>();
		m.put("OutputFile", file);

		CyNetworkWriter writer = new CyNetworkWriter(network, false, serviceRegistrar);
		writer.setDefaultFileFormatUsingFileExt(file);
		
		return serviceRegistrar.getService(TunableSetter.class).createTaskIterator(new TaskIterator(2, writer), m);
	}

	@Override
	public boolean isReady() {
		return true;
	}
}
