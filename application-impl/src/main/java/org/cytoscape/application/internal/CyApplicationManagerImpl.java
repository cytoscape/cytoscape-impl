package org.cytoscape.application.internal;

/*
 * #%L
 * Cytoscape Application Impl (application-impl)
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentRenderingEngineEvent;
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
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.events.RenderingEngineAboutToBeRemovedEvent;
import org.cytoscape.view.presentation.events.RenderingEngineAboutToBeRemovedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of CyApplicationManager.
 */
public class CyApplicationManagerImpl implements CyApplicationManager,
                                                 NetworkAboutToBeDestroyedListener,
                                                 NetworkViewAboutToBeDestroyedListener,
												 NetworkAddedListener,
												 NetworkViewAddedListener,
												 RenderingEngineAboutToBeRemovedListener {
	
	private static final Logger logger = LoggerFactory.getLogger(CyApplicationManagerImpl.class);
	
	private final CyEventHelper cyEventHelper;
	private final CyNetworkManager networkManager;
	private final CyNetworkViewManager networkViewManager;
	private final List<CyNetworkView> selectedNetworkViews;
	private final Object lock = new Object();

	// Trackers for current network object
	private CyNetwork currentNetwork;
	private CyNetworkView currentNetworkView;
	private RenderingEngine<CyNetwork> currentRenderingEngine;
	private CyTable currentTable;

	private Map<String, NetworkViewRenderer> renderers;

	private NetworkViewRenderer defaultRenderer;

	public CyApplicationManagerImpl(final CyEventHelper cyEventHelper,
	                                final CyNetworkManager networkManager,
	                                final CyNetworkViewManager networkViewManager) {
		this.cyEventHelper = cyEventHelper;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		
		selectedNetworkViews = new LinkedList<CyNetworkView>();
		renderers = new LinkedHashMap<String, NetworkViewRenderer>() ;
	}

	@Override
	public void handleEvent(final NetworkViewAddedEvent event) {
		List<CyEvent<?>> eventsToFire = new ArrayList<CyEvent<?>>();
		synchronized (lock) {
			if (!event.getNetworkView().equals(currentNetworkView))
				internalSetCurrentNetworkView(event.getNetworkView(), eventsToFire);
		}
		
		for (CyEvent<?> event2 : eventsToFire) {
			cyEventHelper.fireEvent(event2);
		}
	}

	@Override
	public void handleEvent(final NetworkAddedEvent event) {
		List<CyEvent<?>> eventsToFire = new ArrayList<CyEvent<?>>();
		synchronized (lock) {
			if (!event.getNetwork().equals(currentNetwork))
				internalSetCurrentNetwork(event.getNetwork(), eventsToFire);
		}
		
		for (CyEvent<?> event2 : eventsToFire) {
			cyEventHelper.fireEvent(event2);
		}
	}

	@Override
	public void handleEvent(final NetworkAboutToBeDestroyedEvent event) {
		final CyNetwork toBeDestroyed = event.getNetwork();

		List<CyEvent<?>> eventsToFire = new ArrayList<CyEvent<?>>();
		synchronized (lock) {
			logger.debug("NetworkAboutToBeDestroyedEvent: " + toBeDestroyed + ". Current: " + currentNetwork);
			
			if (toBeDestroyed.equals(currentNetwork)) {
				internalSetCurrentNetwork(null, eventsToFire);
			}
		}

		for (CyEvent<?> event2 : eventsToFire) {
			cyEventHelper.fireEvent(event2);
		}
}

	@Override
	public void handleEvent(final NetworkViewAboutToBeDestroyedEvent event) {
		final CyNetworkView toBeDestroyed = event.getNetworkView();

		List<CyEvent<?>> eventsToFire = new ArrayList<CyEvent<?>>();
		synchronized (lock) {
			logger.debug("NetworkViewAboutToBeDestroyedEvent: " + toBeDestroyed + ". Current: " + currentNetworkView);
			
			if (toBeDestroyed.equals(currentNetworkView)) {
				internalSetCurrentNetworkView(null, eventsToFire);
			}
			
			// TODO: Do we need to fire an event?  Most listeners that care
			// would just listen for NetworkViewAboutToBeDestroyedEvent.
			selectedNetworkViews.removeAll(Collections.singletonList(toBeDestroyed));
		}

		for (CyEvent<?> event2 : eventsToFire) {
			cyEventHelper.fireEvent(event2);
		}
	}

	@Override
	public void handleEvent(RenderingEngineAboutToBeRemovedEvent event) {
		RenderingEngine<?> renderingEngine = event.getRenderingEngine();
		
		synchronized (lock) {
			if (renderingEngine == currentRenderingEngine) {
				setCurrentRenderingEngine(null);
			}
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
		List<CyEvent<?>> eventsToFire = new ArrayList<CyEvent<?>>();
		
		synchronized (lock) {
			internalSetCurrentNetwork(network, eventsToFire);
		}

		for (CyEvent<?> event : eventsToFire) {
			cyEventHelper.fireEvent(event);
		}
	}

	private void internalSetCurrentNetwork(CyNetwork network, List<CyEvent<?>> eventsToFire) {
		if (network != null && !networkManager.networkExists(network.getSUID()))
			throw new IllegalArgumentException("Network is not registered in this ApplicationManager: " + network);

		logger.info("Set current network called: " + network);
		boolean changed = (network == null && currentNetwork != null) || (network != null && !network.equals(currentNetwork));
		
		if (changed) {
			currentNetwork = network;
			
			if (network != null) {
				// If the new current network is not selected, reset the selection and select the current one only
				if (!getSelectedNetworks().contains(network))
					internalSetSelectedNetworks(Collections.singletonList(network), eventsToFire);
				
				// Set new current network view, unless the current view's model is already the new current network
				final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(network);
				
				if (!views.contains(currentNetworkView))
					internalSetCurrentNetworkView(views.isEmpty() ? null : views.iterator().next(), eventsToFire);
			} else {
				if (currentNetworkView != null)
					internalSetCurrentNetworkView(null, eventsToFire);
			}

			logger.debug("Current network is set. Firing SetCurrentNetworkEvent: " + network);
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
	public void setCurrentNetworkView(final CyNetworkView view) {System.out.println(view);
		List<CyEvent<?>> eventsToFire = new ArrayList<CyEvent<?>>();

		synchronized (lock) {
			internalSetCurrentNetworkView(view, eventsToFire);
		}

		for (CyEvent<?> event : eventsToFire) {
			cyEventHelper.fireEvent(event);
		}
	}

	private void internalSetCurrentNetworkView(final CyNetworkView view, List<CyEvent<?>> eventsToFire) {
		if (view != null && !networkManager.networkExists(view.getModel().getSUID()))
			throw new IllegalArgumentException("network is not recognized by this ApplicationManager");

		logger.debug("Set current network view called: " + view);

		boolean changed = (view == null && currentNetworkView != null) || (view != null && !view.equals(currentNetworkView));

		if (changed) {
			currentNetworkView = view;
			
			if (view != null) {
				// If the new current view is not selected, reset selected views and select the current one only
				if (!selectedNetworkViews.contains(view))
					internalSetSelectedNetworkViews(Collections.singletonList(view), eventsToFire);
				
				if (!view.getModel().equals(currentNetwork))
					internalSetCurrentNetwork(view.getModel(), eventsToFire);
			}

			logger.debug("Current network view is set. Firing SetCurrentNetworkViewEvent: " + view);
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
		List<CyEvent<?>> eventsToFire = new ArrayList<CyEvent<?>>();

		synchronized (lock) {
			internalSetSelectedNetworkViews(networkViews, eventsToFire);
		}

		for (CyEvent<?> event : eventsToFire) {
			cyEventHelper.fireEvent(event);
		}
	}

	private void internalSetSelectedNetworkViews(List<CyNetworkView> networkViews, List<CyEvent<?>> eventsToFire) {
		selectedNetworkViews.clear();
		
		if (networkViews != null)
			selectedNetworkViews.addAll(networkViews);

		if (currentNetworkView != null && !selectedNetworkViews.contains(currentNetworkView))
			selectedNetworkViews.add(currentNetworkView);
		
		eventsToFire.add(new SetSelectedNetworkViewsEvent(this, new ArrayList<CyNetworkView>(selectedNetworkViews)));
	}
	
	@Override
	public List<CyNetwork> getSelectedNetworks() {
		final Set<CyNetwork> allNetworks = networkManager.getNetworkSet();
		final List<CyNetwork> selectedNetworks = new ArrayList<CyNetwork>();
		
		for (final CyNetwork n : allNetworks) {
			final CyRow row = n.getRow(n);
			
			if (row.get(CyNetwork.SELECTED, Boolean.class, false))
				selectedNetworks.add(n);
		}
		
		return selectedNetworks;
	}

	@Override
	public void setSelectedNetworks(final List<CyNetwork> networks) {
		List<CyEvent<?>> eventsToFire = new ArrayList<CyEvent<?>>();
		
		synchronized (lock) {
			internalSetSelectedNetworks(networks, eventsToFire);
		}

		for (CyEvent<?> event : eventsToFire) {
			cyEventHelper.fireEvent(event);
		}
	}

	private void internalSetSelectedNetworks(List<CyNetwork> networks, List<CyEvent<?>> eventsToFire) {
		Set<CyNetwork> selectedNetworks = networks != null ? new LinkedHashSet<CyNetwork>(networks)
				: new LinkedHashSet<CyNetwork>();
		if (currentNetwork != null)
			selectedNetworks.add(currentNetwork);
		
		selectedNetworks = selectNetworks(selectedNetworks);
		
		eventsToFire.add(new SetSelectedNetworksEvent(this, new ArrayList<CyNetwork>(selectedNetworks)));
	}
	
	@Override
	public RenderingEngine<CyNetwork> getCurrentRenderingEngine() {
		synchronized (lock) {
			return currentRenderingEngine;
		}
	}

	@Override
	public void setCurrentRenderingEngine(RenderingEngine<CyNetwork> engine) {
		boolean changed;
		synchronized (lock) {
			changed = (engine == null && currentRenderingEngine != null)
					  || (engine != null && !engine.equals(currentRenderingEngine));
			
			this.currentRenderingEngine = engine;
		}
		
		if (changed)
			cyEventHelper.fireEvent(new SetCurrentRenderingEngineEvent(this, this.currentRenderingEngine));
	}

	@Override
	public NetworkViewRenderer getNetworkViewRenderer(final String rendererId) {
		synchronized (lock) {
			return renderers.get(rendererId);
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
		synchronized (lock) {
			currentTable = table;
		}
	}
	
	private Set<CyNetwork> selectNetworks(final Collection<CyNetwork> networks) {
		final Set<CyNetwork> selectedNetworks = new HashSet<CyNetwork>();
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
			renderers.put(renderer.getId(), renderer);
		}
	}

	public void removeNetworkViewRenderer(NetworkViewRenderer renderer, Map<?, ?> properties) {
		synchronized (lock) {
			renderers.remove(renderer.getId());
			if (defaultRenderer == renderer) {
				defaultRenderer = null;
			}
		}
	}
	
	@Override
	public NetworkViewRenderer getDefaultNetworkViewRenderer() {
		synchronized (lock) {
			if (defaultRenderer != null)
				return defaultRenderer;
			
			if (renderers.isEmpty())
				return null;
			
			// Since renderers is a LinkedHashSet, the iterator gives back entries in insertion order.
			defaultRenderer = renderers.entrySet().iterator().next().getValue();
			
			return defaultRenderer;
		}
	}
	
	@Override
	public void setDefaultNetworkViewRenderer(NetworkViewRenderer renderer) {
		synchronized (lock) {
			defaultRenderer = renderer;
		}
	}
	
	@Override
	public Set<NetworkViewRenderer> getNetworkViewRendererSet() {
		synchronized (lock) {
			return new LinkedHashSet<>(renderers.values());
		}
	}
}
