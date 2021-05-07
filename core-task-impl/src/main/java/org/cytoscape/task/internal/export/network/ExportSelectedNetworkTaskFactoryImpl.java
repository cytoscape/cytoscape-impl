package org.cytoscape.task.internal.export.network;

import java.io.File;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.write.ExportNetworkTaskFactory;
import org.cytoscape.task.write.ExportNetworkViewTaskFactory;
import org.cytoscape.task.write.ExportSelectedNetworkTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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

public class ExportSelectedNetworkTaskFactoryImpl implements ExportSelectedNetworkTaskFactory  {
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public ExportSelectedNetworkTaskFactoryImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator() {
		final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		CyNetworkView view = applicationManager.getCurrentNetworkView();

		if (view != null)
			return getExportNetworkViewTaskFactory().createTaskIterator(view);

		CyNetwork network = applicationManager.getCurrentNetwork();

		if (network != null)
			return getExportNetworkTaskFactory().createTaskIterator(network);

		return null;
	}

	@Override
	public TaskIterator createTaskIterator(File file) {
		final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);

		CyNetworkView view = applicationManager.getCurrentNetworkView();

		if (view != null)
			return getExportNetworkViewTaskFactory().createTaskIterator(view, file);

		CyNetwork network = applicationManager.getCurrentNetwork();

		if (network != null)
			return getExportNetworkTaskFactory().createTaskIterator(network, file);

		return null;
	}

	@Override
	public boolean isReady() {
		final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		
		return ((applicationManager.getCurrentNetworkView() != null)
				|| (applicationManager.getCurrentNetwork() != null));
	}
	
	private ExportNetworkTaskFactory getExportNetworkTaskFactory() {
		return serviceRegistrar.getService(ExportNetworkTaskFactory.class, "(id=exportNetworkTaskFactory)");
	}
	
	private ExportNetworkViewTaskFactory getExportNetworkViewTaskFactory() {
		return serviceRegistrar.getService(ExportNetworkViewTaskFactory.class, "(id=exportNetworkViewTaskFactory)");
	}
}
