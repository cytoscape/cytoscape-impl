package org.cytoscape.application.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.application.TableViewRenderer;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentRenderingEngineEvent;
import org.cytoscape.application.events.SetCurrentTableEvent;
import org.cytoscape.application.events.SetSelectedNetworkViewsEvent;
import org.cytoscape.application.events.SetSelectedNetworksEvent;
import org.cytoscape.event.CyEvent;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.events.RenderingEngineAboutToBeRemovedEvent;
import org.cytoscape.view.presentation.events.RenderingEngineAboutToBeRemovedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Application Impl (application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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
 * An implementation of CyApplicationManager.
 */
public class CyApplicationManagerImpl implements CyApplicationManager,
                                                 NetworkAboutToBeDestroyedListener,
                                                 NetworkViewAboutToBeDestroyedListener,
												 RenderingEngineAboutToBeRemovedListener {
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	private static final String LAST_DIRECTORY = "directory.last";
	
	private final CyServiceRegistrar serviceRegistrar;
	private final Object lock = new Object();
	
	
	private CyNetwork currentNetwork;
	private CyNetworkView currentNetworkView;
	private CyTable currentTable;
	
	private RenderingEngine<CyNetwork> currentNetworkRenderingEngine;
	private final List<CyNetworkView> selectedNetworkViews = new LinkedList<>();
	private Map<String,NetworkViewRenderer> networkRenderers = new LinkedHashMap<>();
	private NetworkViewRenderer defaultNetworkRenderer;
	
	private Map<String,TableViewRenderer> tableRenderers = new LinkedHashMap<>();
	private TableViewRenderer defaultTableRenderer;
	

	public CyApplicationManagerImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void handleEvent(final NetworkAboutToBeDestroyedEvent event) {
		final CyNetwork toBeDestroyed = event.getNetwork();
		final List<CyEvent<?>> eventsToFire = new ArrayList<>();
		
		synchronized (lock) {
			logger.debug("NetworkAboutToBeDestroyedEvent: " + toBeDestroyed + ". Current: " + currentNetwork);
			
			if (toBeDestroyed.equals(currentNetwork)) {
				internalSetCurrentNetwork(null, eventsToFire);
			}
		}

		fireEvents(eventsToFire);
	}

	@Override
	public void handleEvent(final NetworkViewAboutToBeDestroyedEvent event) {
		final CyNetworkView toBeDestroyed = event.getNetworkView();
		final List<CyEvent<?>> eventsToFire = new ArrayList<>();
		
		synchronized (lock) {
			logger.debug("NetworkViewAboutToBeDestroyedEvent: " + toBeDestroyed + ". Current: " + currentNetworkView);
			
			if (toBeDestroyed.equals(currentNetworkView)) {
				internalSetCurrentNetworkView(null, eventsToFire);
			}
			
			// TODO: Do we need to fire an event?  Most listeners that care
			// would just listen for NetworkViewAboutToBeDestroyedEvent.
			selectedNetworkViews.removeAll(Collections.singletonList(toBeDestroyed));
		}

		fireEvents(eventsToFire);
	}

	@Override
	public void handleEvent(RenderingEngineAboutToBeRemovedEvent event) {
		final RenderingEngine<?> renderingEngine = event.getRenderingEngine();
		
		synchronized (lock) {
			if (renderingEngine == currentNetworkRenderingEngine)
				setCurrentRenderingEngine(null);
		}
	}
	
	@Override
	public CyNetwork getCurrentNetwork() {
		synchronized (lock) {
			return currentNetwork;
		}
	}

	@Override
	public void setCurrentNetwork(final CyNetwork network) {
		final List<CyEvent<?>> eventsToFire = new ArrayList<>();
		
		synchronized (lock) {
			internalSetCurrentNetwork(network, eventsToFire);
		}

		fireEvents(eventsToFire);
	}

	private void internalSetCurrentNetwork(CyNetwork network, List<CyEvent<?>> eventsToFire) {
		final CyNetworkManager networkManager = serviceRegistrar.getService(CyNetworkManager.class);
		
		if (network != null && !networkManager.networkExists(network.getSUID()))
			throw new IllegalArgumentException("Network is not registered in this ApplicationManager: " + network);

		boolean changed = (network == null && currentNetwork != null) || (network != null && !network.equals(currentNetwork));
		
		if (changed) {
			currentNetwork = network;
			
			if (network != null) {
				// If the new current network is not selected, reset the selection and select the current one only
				if (!getSelectedNetworks().contains(network))
					internalSetSelectedNetworks(Collections.singletonList(network), eventsToFire);
			}
			
			eventsToFire.add(new SetCurrentNetworkEvent(this, currentNetwork));
		}
	}
	
	@Override
	public CyNetworkView getCurrentNetworkView() {
		synchronized (lock) {
			return currentNetworkView;
		}
	}

	@Override
	public void setCurrentNetworkView(final CyNetworkView view) {
		final List<CyEvent<?>> eventsToFire = new ArrayList<>();

		synchronized (lock) {
			internalSetCurrentNetworkView(view, eventsToFire);
		}

		fireEvents(eventsToFire);
	}

	private void internalSetCurrentNetworkView(final CyNetworkView view, List<CyEvent<?>> eventsToFire) {
		final CyNetworkManager networkManager = serviceRegistrar.getService(CyNetworkManager.class);
		
		if (view != null && !networkManager.networkExists(view.getModel().getSUID()))
			throw new IllegalArgumentException("network is not recognized by this ApplicationManager");

		boolean changed = (view == null && currentNetworkView != null) || 
				          (view != null && !view.equals(currentNetworkView));

		if (changed) {
			currentNetworkView = view;
			eventsToFire.add(new SetCurrentNetworkViewEvent(this, currentNetworkView));
		}
	}
	
	@Override
	public List<CyNetworkView> getSelectedNetworkViews() {
		synchronized (lock) {
			return new ArrayList<CyNetworkView>(selectedNetworkViews);
		}
	}

	@Override
	public void setSelectedNetworkViews(final List<CyNetworkView> networkViews) {
		final List<CyEvent<?>> eventsToFire = new ArrayList<>();

		synchronized (lock) {
			internalSetSelectedNetworkViews(networkViews, eventsToFire);
		}

		fireEvents(eventsToFire);
	}

	private void internalSetSelectedNetworkViews(List<CyNetworkView> networkViews, List<CyEvent<?>> eventsToFire) {
		selectedNetworkViews.clear();
		
		if (networkViews != null)
			selectedNetworkViews.addAll(networkViews);

		eventsToFire.add(new SetSelectedNetworkViewsEvent(this, new ArrayList<CyNetworkView>(selectedNetworkViews)));
	}
	
	@Override
	public List<CyNetwork> getSelectedNetworks() {
		final CyNetworkManager networkManager = serviceRegistrar.getService(CyNetworkManager.class);
		final Set<CyNetwork> allNetworks = networkManager.getNetworkSet();
		final List<CyNetwork> selectedNetworks = new ArrayList<>();
		
		for (final CyNetwork n : allNetworks) {
			final CyRow row = n.getRow(n);
			
			if (row.get(CyNetwork.SELECTED, Boolean.class, false))
				selectedNetworks.add(n);
		}
		
		return selectedNetworks;
	}

	@Override
	public void setSelectedNetworks(final List<CyNetwork> networks) {
		final List<CyEvent<?>> eventsToFire = new ArrayList<>();
		
		synchronized (lock) {
			internalSetSelectedNetworks(networks, eventsToFire);
		}

		fireEvents(eventsToFire);
	}

	private void internalSetSelectedNetworks(List<CyNetwork> networks, List<CyEvent<?>> eventsToFire) {
		Set<CyNetwork> selectedNetworks = networks != null ? new LinkedHashSet<>(networks) : new LinkedHashSet<>();
		
		if (!selectedNetworks.equals(new LinkedHashSet<>(getSelectedNetworks()))) {
			selectedNetworks = selectNetworks(selectedNetworks);
			eventsToFire.add(new SetSelectedNetworksEvent(this, new ArrayList<>(selectedNetworks)));
		}
	}
	
	@Override
	public RenderingEngine<CyNetwork> getCurrentRenderingEngine() {
		synchronized (lock) {
			return currentNetworkRenderingEngine;
		}
	}

	@Override
	public void setCurrentRenderingEngine(RenderingEngine<CyNetwork> engine) {
		boolean changed;
		synchronized (lock) {
			changed = (engine == null && currentNetworkRenderingEngine != null)
					  || (engine != null && !engine.equals(currentNetworkRenderingEngine));
			
			this.currentNetworkRenderingEngine = engine;
		}
		
		if (changed)
			fireEvents(Collections.singletonList(new SetCurrentRenderingEngineEvent(this, currentNetworkRenderingEngine)));
	}
	
	@Override
	public TableViewRenderer getTableViewRenderer(final String rendererId) {
		synchronized (lock) {
			return tableRenderers.get(rendererId);
		}
	}

	@Override
	public NetworkViewRenderer getNetworkViewRenderer(final String rendererId) {
		synchronized (lock) {
			return networkRenderers.get(rendererId);
		}
	}

	@Override
	public CyTable getCurrentTable() {
		synchronized (lock) {
			return currentTable;
		}
	}

	@Override
	public void setCurrentTable(CyTable table) {
		var eventsToFire = new ArrayList<CyEvent<?>>(1);
		
		synchronized (lock) {
			if (!Objects.equals(currentTable, table)) {
				currentTable = table;
				eventsToFire.add(new SetCurrentTableEvent(this, currentTable));
			}
		}
		
		fireEvents(eventsToFire);
	}
	
	private Set<CyNetwork> selectNetworks(final Collection<CyNetwork> networks) {
		final CyNetworkManager networkManager = serviceRegistrar.getService(CyNetworkManager.class);
		
		final Set<CyNetwork> selectedNetworks = new HashSet<>();
		final Set<CyNetwork> allNetworks = networkManager.getNetworkSet();
		
		for (final CyNetwork n : allNetworks) {
			final boolean selected = networks != null && networks.contains(n);
			final CyRow row = n.getRow(n);
			row.set(CyNetwork.SELECTED, selected);
			
			if (selected)
				selectedNetworks.add(n);
		}

		return selectedNetworks;
	}

	@Override
	public void reset() {
		synchronized (lock) {
			setCurrentNetwork(null);
			setCurrentNetworkView(null);
			setSelectedNetworkViews(null);
			setCurrentRenderingEngine(null);
			setCurrentTable(null);
		}
	}
	
	@Override
	public NetworkViewRenderer getCurrentNetworkViewRenderer() {
		synchronized (lock) {
			NetworkViewRenderer netViewRenderer = null;
			
			if (currentNetworkView != null)
				netViewRenderer = getNetworkViewRenderer(currentNetworkView.getRendererId());
			
			if (netViewRenderer == null)
				netViewRenderer = getDefaultNetworkViewRenderer();
			
			return netViewRenderer;
		}
	}

	public void addNetworkViewRenderer(NetworkViewRenderer renderer, Map<?, ?> properties) {
		synchronized (lock) {
			networkRenderers.put(renderer.getId(), renderer);
		}
	}

	public void removeNetworkViewRenderer(NetworkViewRenderer renderer, Map<?, ?> properties) {
		synchronized (lock) {
			networkRenderers.remove(renderer.getId());
			
			if (defaultNetworkRenderer == renderer) {
				defaultNetworkRenderer = null;
			}
		}
	}
	
	public void addTableViewRenderer(TableViewRenderer renderer, Map<?, ?> properties) {
		synchronized (lock) {
			tableRenderers.put(renderer.getId(), renderer);
		}
	}

	public void removeTableViewRenderer(TableViewRenderer renderer, Map<?, ?> properties) {
		synchronized (lock) {
			tableRenderers.remove(renderer.getId());
			
			if (defaultTableRenderer == renderer) {
				defaultTableRenderer = null;
			}
		}
	}
	
	@Override
	public NetworkViewRenderer getDefaultNetworkViewRenderer() {
		synchronized (lock) {
			if (defaultNetworkRenderer == null) {
				if (networkRenderers.isEmpty()) {
					return null;
				}
				// Since renderers is a LinkedHashSet, the iterator gives back entries in insertion order.
				defaultNetworkRenderer = networkRenderers.entrySet().iterator().next().getValue();
			}
			return defaultNetworkRenderer;
		}
	}
	
	@Override
	public void setDefaultNetworkViewRenderer(NetworkViewRenderer renderer) {
		synchronized (lock) {
			defaultNetworkRenderer = renderer;
		}
	}
	
	@Override
	public void setDefaultTableViewRenderer(TableViewRenderer renderer) {
		synchronized (lock) {
			defaultTableRenderer = renderer;
		}
	}
	
	@Override
	public TableViewRenderer getDefaultTableViewRenderer() {
		synchronized (lock) {
			if (defaultTableRenderer == null) {
				if (tableRenderers.isEmpty()) {
					return null;
				}
				// Since renderers is a LinkedHashSet, the iterator gives back entries in insertion order.
				defaultTableRenderer = tableRenderers.entrySet().iterator().next().getValue();
			}
			return defaultTableRenderer;
		}
	}
	
	@Override
	public Set<TableViewRenderer> getTableViewRendererSet() {
		synchronized (lock) {
			return new LinkedHashSet<>(tableRenderers.values());
		}
	}
	
	@Override
	public Set<NetworkViewRenderer> getNetworkViewRendererSet() {
		synchronized (lock) {
			return new LinkedHashSet<>(networkRenderers.values());
		}
	}
	
	@Override
	public File getCurrentDirectory() {
		final Properties props = (Properties) serviceRegistrar
				.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)").getProperties();
		String lastDir = props.getProperty(LAST_DIRECTORY);
		File dir = (lastDir != null) ? new File(lastDir) : null;
		
		if (dir == null || !dir.exists() || !dir.isDirectory()) {
			dir = new File(System.getProperty("user.dir"));
			
			if (dir != null) // if path exists but is not valid, remove the property
				props.remove(LAST_DIRECTORY);
		}
		
		return dir;
	}
	
	@Override
	public boolean setCurrentDirectory(File dir) {
		if (dir == null || !dir.exists() || !dir.isDirectory())
			return false;
		
		final Properties props = (Properties) serviceRegistrar
				.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)").getProperties();
		props.setProperty(LAST_DIRECTORY, dir.getAbsolutePath());
		
		return true;
	}
	
	private void fireEvents(final List<CyEvent<?>> eventsToFire) {
		final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		
		for (CyEvent<?> event : eventsToFire)
			eventHelper.fireEvent(event);
	}
}
