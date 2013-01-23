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

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient;
import org.cytoscape.webservice.psicquic.RegistryManager;
import org.cytoscape.webservice.psicquic.mapper.MergedNetworkBuilder;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class ProcessSearchResultTask extends AbstractTask {
	private final PSICQUICRestClient client;

	private final String query;

	private final SearchRecoredsTask searchTask;
	private final CyLayoutAlgorithmManager layouts;

	private final CyNetworkView netView;
	private final View<CyNode> nodeView;

	private final CyEventHelper eh;
	private final VisualMappingManager vmm;

	private volatile boolean canceled = false;

	private final RegistryManager registryManager;
	private final MergedNetworkBuilder builder;

	public ProcessSearchResultTask(final String query, final PSICQUICRestClient client, final SearchRecoredsTask searchTask,
			final CyNetworkView parentNetworkView, final View<CyNode> nodeView, final CyEventHelper eh,
			final VisualMappingManager vmm, final CyLayoutAlgorithmManager layouts, final RegistryManager registryManager, final MergedNetworkBuilder builder) {
		this.client = client;
		this.query = query;
		this.netView = parentNetworkView;
		this.nodeView = nodeView;
		this.eh = eh;
		this.vmm = vmm;
		this.searchTask = searchTask;
		this.layouts = layouts;
		this.registryManager = registryManager;
		this.builder = builder;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Expanding by PSICQUIC Services");
		taskMonitor.setStatusMessage("Loading interaction from remote service...");
		taskMonitor.setProgress(0.01d);
		
		if(canceled)
			return;
		
		Map<String, String> result = processSearchResult();
		insertTasksAfterCurrentTask(new ExpandFromSelectedSourcesTask(query, client, result, netView, nodeView, eh, vmm, layouts, builder));
		taskMonitor.setProgress(1.0d);
	}
	
	@Override
	public void cancel() {
		this.canceled = true;
		client.cancel();
	}

	private final Map<String, String> processSearchResult() {
		final Map<String, String> sourceMap = new HashMap<String, String>();
		final Map<String, Long> rs = searchTask.getResult();
		
		for (final String sourceURL : rs.keySet()) {
			final Long interactionCount = rs.get(sourceURL);
			if (interactionCount <= 0)
				continue;

			sourceMap.put(registryManager.getSource2NameMap().get(sourceURL) + ": " + interactionCount, sourceURL);
		}		
		return sourceMap;
	}
}
