package org.cytoscape.task.internal.loadnetwork;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

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

public class LoadMultipleNetworksTask extends AbstractLoadNetworkTask {
	
	private ListSingleSelection<String> targetColumnList;
	private ListSingleSelection<NetworkViewRenderer> rendererList;
	
	private final CyRootNetwork rootNetwork;
	private final Map<String, CyNetworkReader> readers;

	public LoadMultipleNetworksTask(Map<String/*network-name*/, CyNetworkReader> readers, CyRootNetwork rootNetwork,
			CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		this.rootNetwork = rootNetwork;
		this.readers = readers;
		
		init();
	}
	
	@ProvidesTitle
	@Override
	public String getTitle() {
		return "Load Networks from Files";
	}

	@Tunable(description = "Node Identifier Mapping Column:", groups = "_Network", gravity = 1.0)
	public ListSingleSelection<String> getTargetColumnList() {
		return targetColumnList;
	}

	public void setTargetColumnList(ListSingleSelection<String> colList) {
		if (colList != targetColumnList) {
			targetColumnList = colList;
			
			if (targetColumnList.getPossibleValues().contains(CyRootNetwork.SHARED_NAME))
				targetColumnList.setSelectedValue(CyRootNetwork.SHARED_NAME);
		}
	}
	
	@Tunable(description = "Network View Renderer:", groups = "_Network", gravity = 2.0)
	public ListSingleSelection<NetworkViewRenderer> getNetworkViewRendererList() {
		return rendererList;
	}
	
	public void setNetworkViewRendererList(final ListSingleSelection<NetworkViewRenderer> rendererList) {
		this.rendererList = rendererList;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;

		if (readers == null && readers.isEmpty())
			throw new NullPointerException("No network reader specified.");

		final String rootNetName = rootNetwork != null ?
				rootNetwork.getRow(rootNetwork).get(CyRootNetwork.NAME, String.class) : null;
		final String targetColumn = targetColumnList != null ? targetColumnList.getSelectedValue() : null;
		final NetworkViewRenderer renderer = rendererList != null ? rendererList.getSelectedValue() : null;
		
		readers.values().forEach(r -> {
			if (r instanceof AbstractCyNetworkReader) {
				AbstractCyNetworkReader ar = (AbstractCyNetworkReader) r;
				
				if (rootNetName != null) {
					ListSingleSelection<String> ls = new ListSingleSelection<>(Collections.singletonList(rootNetName));
					ls.setSelectedValue(rootNetName);
					ar.setRootNetworkList(ls);
				} else {
					// Force "create new collection"
					ar.setRootNetworkList(new ListSingleSelection<>(Collections.emptyList()));
					ar.setTargetColumnList(new ListSingleSelection<>(Collections.emptyList()));
				}
				
				if (targetColumn != null) {
					ListSingleSelection<String> ls = new ListSingleSelection<>(Collections.singletonList(targetColumn));
					ls.setSelectedValue(targetColumn);
					ar.setTargetColumnList(ls);
				}
				
				if (renderer != null) {
					ListSingleSelection<NetworkViewRenderer> ls =
							new ListSingleSelection<>(Collections.singletonList(renderer));
					ls.setSelectedValue(renderer);
					ar.setNetworkViewRendererList(ls);
				}
			}
		});
		
		for (Entry<String, CyNetworkReader> entry : readers.entrySet()) {
			CyNetworkReader r = entry.getValue();
			name = entry.getKey();
			loadNetwork(r);
		}
	}
	
	private void init() {
		// Only initialize these tunable lists (so they can be displayed) if there is more than one file to load,
		// otherwise let the reader's tunables handle it.
		if (readers != null && readers.size() > 1) {
			// Initialize column list
			if (rootNetwork != null)
				setTargetColumnList(getTargetColumns(rootNetwork));
			else
				setTargetColumnList(new ListSingleSelection<>());
			
			// Initialize renderer list
			final List<NetworkViewRenderer> renderers = new ArrayList<>();
			NetworkViewRenderer defViewRenderer = null;
			
			final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
			final Set<NetworkViewRenderer> rendererSet = applicationManager.getNetworkViewRendererSet();
			
			// If there is only one registered renderer, we don't want to add it to the List Selection,
			// so the combo-box does not appear to the user, since there is nothing to select anyway.
			if (rendererSet.size() > 1) {
				renderers.addAll(rendererSet);
				Collections.sort(renderers, new Comparator<NetworkViewRenderer>() {
					@Override
					public int compare(NetworkViewRenderer r1, NetworkViewRenderer r2) {
						return r1.toString().compareToIgnoreCase(r2.toString());
					}
				});
			}
			defViewRenderer = applicationManager.getDefaultNetworkViewRenderer();
			
			rendererList = new ListSingleSelection<>(renderers);
			
			if (defViewRenderer != null && renderers.contains(defViewRenderer))
				rendererList.setSelectedValue(defViewRenderer);
		}
	}
	
	private final ListSingleSelection<String> getTargetColumns(final CyNetwork network) {
		final CyTable selectedTable = network.getTable(CyNode.class, CyRootNetwork.SHARED_ATTRS);
		final List<String> colNames = new ArrayList<>();
		
		for (CyColumn col : selectedTable.getColumns()) {
			// Exclude SUID from the mapping key list
			if (!col.getName().equalsIgnoreCase(CyIdentifiable.SUID) && !col.getName().endsWith(".SUID") &&
					(col.getType() == String.class || col.getType() == Integer.class || col.getType() == Long.class))
				colNames.add(col.getName());
		}
		
		if (colNames.isEmpty() || (colNames.size() == 1 && colNames.contains(CyRootNetwork.SHARED_NAME)))
			return new ListSingleSelection<>();
		
		sort(colNames);
		
		return new ListSingleSelection<>(colNames);
	}
	
	private void sort(final List<String> names) {
		if (!names.isEmpty()) {
			final Collator collator = Collator.getInstance(Locale.getDefault());
			
			Collections.sort(names, new Comparator<String>() {
				@Override
				public int compare(String s1, String s2) {
					if (s1 == null && s2 == null) return 0;
					if (s1 == null) return -1;
					if (s2 == null) return 1;
					return collator.compare(s1, s2);
				}
			});
		}
	}
}
