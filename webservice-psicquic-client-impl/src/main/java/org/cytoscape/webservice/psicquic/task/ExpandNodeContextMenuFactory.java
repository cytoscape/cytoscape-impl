package org.cytoscape.webservice.psicquic.task;

/*
 * #%L
 * Cytoscape PSIQUIC Web Service Impl (webservice-psicquic-client-impl)
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
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient;
import org.cytoscape.webservice.psicquic.RegistryManager;
import org.cytoscape.webservice.psicquic.mapper.CyNetworkBuilder;
import org.cytoscape.work.TaskIterator;

public class ExpandNodeContextMenuFactory extends AbstractNodeViewTaskFactory {

	private final CyEventHelper eh;
	private final VisualMappingManager vmm;
	private final RegistryManager manager;
	private final PSICQUICRestClient client;

	private final CyLayoutAlgorithmManager layouts;
	private final CyNetworkBuilder builder;

	public ExpandNodeContextMenuFactory(CyEventHelper eh, VisualMappingManager vmm, final PSICQUICRestClient client,
			final RegistryManager manager, final CyLayoutAlgorithmManager layouts, final CyNetworkBuilder builder) {
		this.eh = eh;
		this.vmm = vmm;
		this.client = client;
		this.manager = manager;
		this.layouts = layouts;
		this.builder = builder;
	}

	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView netView) {
		if (manager == null)
			throw new NullPointerException("RegistryManager is null");

		// Create query
		String query = netView.getModel().getDefaultNodeTable().getRow(nodeView.getModel().getSUID())
				.get(CyNetwork.NAME, String.class);
		if (query == null)
			throw new NullPointerException("Query object is null.");
		else {
			return new TaskIterator(new BuildQueryTask(netView, nodeView, eh, vmm, client, manager, layouts, builder));
		}
	}
}
