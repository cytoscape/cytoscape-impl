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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentRenderingEngineEvent;
import org.cytoscape.application.events.SetSelectedNetworkViewsEvent;
import org.cytoscape.application.events.SetSelectedNetworksEvent;
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
	private final ReadWriteLock lock;

	// Trackers for current network object
	private CyNetwork currentNetwork;
	private CyNetworkView currentNetworkView;
	private RenderingEngine<CyNetwork> currentRenderingEngine;
	private CyTable currentTable;

	private NetworkViewRenderer currentRenderer;
	private Map<String, NetworkViewRenderer> renderers;

	private NetworkViewRenderer defaultRenderer;

	public CyApplicationManagerImpl(final CyEventHelper cyEventHelper,
	                                final CyNetworkManager networkManager,
	                                final CyNetworkViewManager networkViewManager) {
		this.cyEventHelper = cyEventHelper;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		lock = new ReentrantReadWriteLock(true);
		
		selectedNetworkViews = new LinkedList<CyNetworkView>();
		renderers = new LinkedHashMap<String, NetworkViewRenderer>() ;
	}

	@Override
	public void handleEvent(final NetworkViewAddedEvent event) {
		if (!event.getNetworkView().equals(currentNetworkView))
			setCurrentNetworkView(event.getNetworkView());
	}

	@Override
	public void handleEvent(final NetworkAddedEvent event) {
		if (!event.getNetwork().equals(currentNetwork))
			setCurrentNetwork(event.getNetwork());
	}

	@Override
	public void handleEvent(final NetworkAboutToBeDestroyedEvent event) {
		final CyNetwork toBeDestroyed = event.getNetwork();

		lock.writeLock().lock();
		try {
			logger.debug("NetworkAboutToBeDestroyedEvent: " + toBeDestroyed + ". Current: " + currentNetwork);
			
			if (toBeDestroyed.equals(currentNetwork)) {
				setCurrentNetwork(null);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void handleEvent(final NetworkViewAboutToBeDestroyedEvent event) {
		final CyNetworkView toBeDestroyed = event.getNetworkView();

		lock.writeLock().lock();
		try {
			logger.debug("NetworkViewAboutToBeDestroyedEvent: " + toBeDestroyed + ". Current: " + currentNetworkView);
			
			if (toBeDestroyed.equals(currentNetworkView)) {
				setCurrentNetworkView(null);
			}
			
			// TODO: Do we need to fire an event?  Most listeners that care
			// would just listen for NetworkViewAboutToBeDestroyedEvent.
			selectedNetworkViews.removeAll(Collections.singletonList(toBeDestroyed));
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void handleEvent(RenderingEngineAboutToBeRemovedEvent event) {
		RenderingEngine<?> renderingEngine = event.getRenderingEngine();
		
		lock.writeLock().lock();
		try {
			if (renderingEngine == currentRenderingEngine) {
				setCurrentRenderingEngine(null);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	@Override
	public CyNetwork getCurrentNetwork() {
		lock.readLock().lock();
		try {
			return currentNetwork;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void setCurrentNetwork(final CyNetwork network) {
		boolean changed = false;
		
		lock.writeLock().lock();
		try {
			if (network != null && !networkManager.networkExists(network.getSUID()))
				throw new IllegalArgumentException("Network is not registered in this ApplicationManager: " + network);

			logger.info("Set current network called: " + network);
			changed = (network == null && currentNetwork != null) || (network != null && !network.equals(currentNetwork));
			
			if (changed) {
				currentNetwork = network;
				
				if (network != null) {
					// If the new current network is not selected, reset the selection and select the current one only
					if (!getSelectedNetworks().contains(network))
						setSelectedNetworks(Collections.singletonList(network));
					
					// Set new current network view, unless the current view's model is already the new current network
					final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(network);
					
					if (!views.contains(currentNetworkView))
						setCurrentNetworkView(views.isEmpty() ? null : views.iterator().next());
				} else {
					if (currentNetworkView != null)
						setCurrentNetworkView(null);
				}
			}
		} finally {
			lock.writeLock().unlock();
		}

		if (changed) {
			logger.debug("Current network is set. Firing SetCurrentNetworkEvent: " + network);
			cyEventHelper.fireEvent(new SetCurrentNetworkEvent(this, currentNetwork));
		}
	}

	@Override
	public CyNetworkView getCurrentNetworkView() {
		lock.readLock().lock();
		try {
			return currentNetworkView;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void setCurrentNetworkView(final CyNetworkView view) {
		boolean changed = false;
		
		lock.writeLock().lock();
		try {
			if (view != null && !networkManager.networkExists(view.getModel().getSUID()))
				throw new IllegalArgumentException("network is not recognized by this ApplicationManager");

			logger.debug("Set current network view called: " + view);

			changed = (view == null && currentNetworkView != null) || (view != null && !view.equals(currentNetworkView));

			if (changed) {
				currentNetworkView = view;
				
				if (view != null) {
					// If the new current view is not selected, reset selected views and select the current one only
					if (!selectedNetworkViews.contains(view))
						setSelectedNetworkViews(Collections.singletonList(view));
					
					if (!view.getModel().equals(currentNetwork))
						setCurrentNetwork(view.getModel());
				}
			}
		} finally {
			lock.writeLock().unlock();
		}

		if (changed) {
			logger.debug("Current network view is set. Firing SetCurrentNetworkViewEvent: " + view);
			cyEventHelper.fireEvent(new SetCurrentNetworkViewEvent(this, currentNetworkView));
		}
	}

	@Override
	public List<CyNetworkView> getSelectedNetworkViews() {
		lock.readLock().lock();
		try {
			return new ArrayList<CyNetworkView>(selectedNetworkViews);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void setSelectedNetworkViews(final List<CyNetworkView> networkViews) {
		lock.writeLock().lock();
		try {
			selectedNetworkViews.clear();
			
			if (networkViews != null)
				selectedNetworkViews.addAll(networkViews);

			if (currentNetworkView != null && !selectedNetworkViews.contains(currentNetworkView))
				selectedNetworkViews.add(currentNetworkView);
		} finally {
			lock.writeLock().unlock();
		}

		cyEventHelper.fireEvent(new SetSelectedNetworkViewsEvent(this,
		                                                         new ArrayList<CyNetworkView>(selectedNetworkViews)));
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
		Set<CyNetwork> selectedNetworks = networks != null ? new LinkedHashSet<CyNetwork>(networks)
				: new LinkedHashSet<CyNetwork>();
		
		lock.writeLock().lock();
		try {
			if (currentNetwork != null)
				selectedNetworks.add(currentNetwork);
			
			selectedNetworks = selectNetworks(selectedNetworks);
		} finally {
			lock.writeLock().unlock();
		}

		cyEventHelper.fireEvent(new SetSelectedNetworksEvent(this, new ArrayList<CyNetwork>(selectedNetworks)));
	}

	@Override
	public RenderingEngine<CyNetwork> getCurrentRenderingEngine() {
		return currentRenderingEngine;
	}

	@Override
	public void setCurrentRenderingEngine(RenderingEngine<CyNetwork> engine) {
		boolean changed = (engine == null && currentRenderingEngine != null)
				|| (engine != null && !engine.equals(currentRenderingEngine));
		
		this.currentRenderingEngine = engine;
		
		if (engine != null) {
			currentRenderer = getRenderer(engine.getRendererId());
		} else {
			currentRenderer = null;
		}
		
		if (changed)
			cyEventHelper.fireEvent(new SetCurrentRenderingEngineEvent(this, this.currentRenderingEngine));
	}

	private NetworkViewRenderer getRenderer(String rendererId) {
		return renderers.get(rendererId);
	}

	@Override
	public CyTable getCurrentTable() {
		return currentTable;
	}

	@Override
	public void setCurrentTable(CyTable table) {
		currentTable = table;
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
		lock.writeLock().lock();
		try {
			setCurrentNetwork(null);
			setCurrentNetworkView(null);
			setSelectedNetworkViews(null);
			setCurrentRenderingEngine(null);
			setCurrentTable(null);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	@Override
	public NetworkViewRenderer getCurrentNetworkViewRenderer() {
		if (currentRenderer != null) {
			return currentRenderer;
		}
		return getDefaultRenderer();
	}

	private NetworkViewRenderer getDefaultRenderer() {
		if (defaultRenderer != null) {
			return defaultRenderer;
		}
		
		if (renderers.isEmpty()) {
			return null;
		}
		
		// Since renderers is a LinkedHashSet, the iterator gives back entries
		// in insertion order.
		defaultRenderer = renderers.entrySet().iterator().next().getValue();
		return defaultRenderer;
	}
	
	public void addNetworkViewRenderer(NetworkViewRenderer renderer, Map<?, ?> properties) {
		renderers.put(renderer.getId(), renderer);
	}

	public void removeNetworkViewRenderer(NetworkViewRenderer renderer, Map<?, ?> properties) {
		renderers.remove(renderer.getId());
		if (defaultRenderer == renderer) {
			defaultRenderer = null;
		}
	}
	
	@Override
	public NetworkViewRenderer getDefaultNetworkViewRenderer() {
		return getDefaultRenderer();
	}
	
	@Override
	public void setDefaultNetworkViewRenderer(NetworkViewRenderer renderer) {
		defaultRenderer = renderer;
	}
}
