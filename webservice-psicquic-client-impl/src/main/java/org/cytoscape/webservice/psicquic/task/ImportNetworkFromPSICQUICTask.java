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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient.SearchMode;
import org.cytoscape.webservice.psicquic.PSIMI25VisualStyleBuilder;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportNetworkFromPSICQUICTask extends AbstractTask implements ObservableTask {

	private static final Logger logger = LoggerFactory.getLogger(ImportNetworkFromPSICQUICTask.class);

	private static final String VIEW_THRESHOLD = "viewThreshold";
	private static final int DEF_VIEW_THRESHOLD = 3000;
	
	private final PSICQUICRestClient client;
	private final CyNetworkManager manager;

	// TaskFactory for creating view
	private final CreateNetworkViewTaskFactory createViewTaskFactory;

	private String query;
	private Collection<String> targetServices;

	private final String defaultSearchResultURL = "http://www.ebi.ac.uk/Tools/webservices/psicquic/intact/webservices/current/search/";

	private Set<String> searchResult;
	private Map<String, CyNetwork> result;

	private SearchRecoredsTask searchTask;

	private final SearchMode mode;
	private final boolean mergeNetworks;

	private final PSIMI25VisualStyleBuilder vsBuilder;
	private final VisualMappingManager vmm;
	private CyServiceRegistrar serviceRegistrar;

	private volatile boolean canceled;

	public ImportNetworkFromPSICQUICTask(
			final String query,
			final PSICQUICRestClient client,
			final CyNetworkManager manager,
			final Set<String> searchResult,
			final SearchMode mode,
			final CreateNetworkViewTaskFactory createViewTaskFactory,
			final PSIMI25VisualStyleBuilder vsBuilder,
			final VisualMappingManager vmm,
			final boolean toCluster,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.client = client;
		this.manager = manager;
		this.query = query;
		this.mergeNetworks = toCluster;
		this.searchResult = searchResult;
		this.mode = mode;
		this.createViewTaskFactory = createViewTaskFactory;
		this.vmm = vmm;
		this.vsBuilder = vsBuilder;
		this.serviceRegistrar = serviceRegistrar;
	}

	public ImportNetworkFromPSICQUICTask(
			final String query,
			final PSICQUICRestClient client,
			final CyNetworkManager manager,
			final SearchRecoredsTask searchTask,
			final SearchMode mode,
			final CreateNetworkViewTaskFactory createViewTaskFactory,
			final PSIMI25VisualStyleBuilder vsBuilder,
			final VisualMappingManager vmm,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.client = client;
		this.manager = manager;
		this.query = query;
		this.mergeNetworks = false;
		this.searchTask = searchTask;
		this.mode = mode;
		this.createViewTaskFactory = createViewTaskFactory;
		this.vmm = vmm;
		this.vsBuilder = vsBuilder;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Importing Interactions from PSICQUIC Services");
		taskMonitor.setStatusMessage("Loading interactions from remote service...");
		taskMonitor.setProgress(0.01d);

		if (searchResult == null)
			processSearchResult();
		else
			targetServices = searchResult;

		if (searchResult == null) {
			searchResult = new HashSet<String>();
			searchResult.add(defaultSearchResultURL);
			targetServices = searchResult;
		}

		if (query == null)
			throw new NullPointerException("Query is null");
		if (targetServices == null)
			throw new NullPointerException("Target service set is null");

		// Switch task type based on the user option.

		final Date date = new Date();
		final SimpleDateFormat timestamp = new SimpleDateFormat("yyyy/MM/dd K:mm:ss a, z");
		final String suffix = "(" + timestamp.format(date) + ")";
		result = new HashMap<String, CyNetwork>();
		
		if (mergeNetworks) {
			final CyNetwork network = client.importMergedNetwork(query, targetServices, mode, taskMonitor);
			network.getRow(network).set(CyNetwork.NAME, "Merged Network " + suffix);
			addNetworkData(network);
			manager.addNetwork(network);
			result.put("clustered", network);
		} else {
			final Collection<CyNetwork> networks = client.importNetworks(query, targetServices, mode, taskMonitor);
			
			for (CyNetwork network : networks) {
				final String networkName = network.getRow(network).get(CyNetwork.NAME, String.class) + " " + suffix;
				network.getRow(network).set(CyNetwork.NAME, networkName);
				addNetworkData(network);
				manager.addNetwork(network);
				result.put(networkName, network);
			}
		}

		if (canceled)
			return;
		
		// Check Visual Style exists or not
		VisualStyle psiStyle = null;
		
		for (VisualStyle style : vmm.getAllVisualStyles()) {
			if (style.getTitle().equals(PSIMI25VisualStyleBuilder.DEF_VS_NAME)) {
				psiStyle = style;
				break;
			}
		}
		
		if (psiStyle == null) {
			psiStyle = vsBuilder.getVisualStyle();
			vmm.addVisualStyle(psiStyle);
		}
		
		vmm.setCurrentVisualStyle(psiStyle);

		if (canceled)
			return;
		
		final List<CyNetwork> smallNetworks = new ArrayList<>();
		final int viewThreshold = getViewThreshold();
		
		for (CyNetwork net : result.values()) {
			final int numGraphObjects = net.getNodeCount() + net.getEdgeCount();
			
			if (numGraphObjects < viewThreshold)
				smallNetworks.add(net);
		}
		
		if (canceled)
			return;
		
		if (!smallNetworks.isEmpty())
			insertTasksAfterCurrentTask(createViewTaskFactory.createTaskIterator(smallNetworks));
	}

	@Override
	public void cancel() {
		this.canceled = true;
		client.cancel();
	}

	private void addNetworkData(final CyNetwork network) {
		network.getRow(network).getTable().createColumn("created by", String.class, true);
		network.getRow(network).set("created by", "PSICQUIC Web Service");
	}

	private void processSearchResult() {
		Map<String, Long> rs = searchTask.getResult();
		targetServices = new HashSet<String>();

		for (final String sourceURL : rs.keySet()) {
			final Long interactionCount = rs.get(sourceURL);
			if (interactionCount == 0)
				continue;

			targetServices.add(sourceURL);
		}
	}

	public Set<CyNetwork> getNetworks() {
		return new HashSet<CyNetwork>(result.values());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getResults(Class<? extends T> type) {
		return (T) getNetworks();
	}
	
	private int getViewThreshold() {
		final Properties props = (Properties) 
				serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)").getProperties();
		final String vts = props.getProperty(VIEW_THRESHOLD);
		int threshold;
		
		try {
			threshold = Integer.parseInt(vts);
		} catch (Exception e) {
			threshold = DEF_VIEW_THRESHOLD;
		}

		return threshold;
	}
}
