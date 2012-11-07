/*
 File: CyApplicationManagerImpl.java

 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.application.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
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

	// Trackers for current network object
	private CyNetwork currentNetwork;
	private CyNetworkView currentNetworkView;
	private RenderingEngine<CyNetwork> currentRenderer;
	private CyTable currentTable;

	public CyApplicationManagerImpl(final CyEventHelper cyEventHelper,
	                                final CyNetworkManager networkManager,
	                                final CyNetworkViewManager networkViewManager) {
		this.cyEventHelper = cyEventHelper;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;

		selectedNetworkViews = new LinkedList<CyNetworkView>();
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

		synchronized (this) {
			logger.debug("NetworkAboutToBeDestroyedEvent: " + toBeDestroyed + ". Current: " + currentNetwork);
			
			if (toBeDestroyed.equals(currentNetwork)) {
				setCurrentNetwork(null);
			}
		}
	}

	@Override
	public void handleEvent(final NetworkViewAboutToBeDestroyedEvent event) {
		final CyNetworkView toBeDestroyed = event.getNetworkView();

		synchronized (this) {
			logger.debug("NetworkViewAboutToBeDestroyedEvent: " + toBeDestroyed + ". Current: " + currentNetworkView);
			
			if (toBeDestroyed.equals(currentNetworkView)) {
				setCurrentNetworkView(null);
			}
			
			// TODO: Do we need to fire an event?  Most listeners that care
			// would just listen for NetworkViewAboutToBeDestroyedEvent.
			selectedNetworkViews.removeAll(Collections.singletonList(toBeDestroyed));
		}
	}

	@Override
	public void handleEvent(RenderingEngineAboutToBeRemovedEvent event) {
		RenderingEngine<?> renderingEngine = event.getRenderingEngine();
		
		synchronized (this) {
			if (renderingEngine == currentRenderer) {
				setCurrentRenderingEngine(null);
			}
		}
	}
	
	@Override
	public synchronized CyNetwork getCurrentNetwork() {
		return currentNetwork;
	}

	@Override
	public void setCurrentNetwork(final CyNetwork network) {
		boolean changed = false;
		
		synchronized (this) {
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
		}

		if (changed) {
			logger.debug("Current network is set. Firing SetCurrentNetworkEvent: " + network);
			cyEventHelper.fireEvent(new SetCurrentNetworkEvent(this, currentNetwork));
		}
	}

	@Override
	public synchronized CyNetworkView getCurrentNetworkView() {
		return currentNetworkView;
	}

	@Override
	public void setCurrentNetworkView(final CyNetworkView view) {
		boolean changed = false;
		
		synchronized (this) {
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
		}

		if (changed) {
			logger.debug("Current network view is set. Firing SetCurrentNetworkViewEvent: " + view);
			cyEventHelper.fireEvent(new SetCurrentNetworkViewEvent(this, currentNetworkView));
		}
	}

	@Override
	public synchronized List<CyNetworkView> getSelectedNetworkViews() {
		return new ArrayList<CyNetworkView>(selectedNetworkViews);
	}

	@Override
	public void setSelectedNetworkViews(final List<CyNetworkView> networkViews) {
		synchronized (this) {
			selectedNetworkViews.clear();
			
			if (networkViews != null)
				selectedNetworkViews.addAll(networkViews);

			if (currentNetworkView != null && !selectedNetworkViews.contains(currentNetworkView))
				selectedNetworkViews.add(currentNetworkView);
		}

		cyEventHelper.fireEvent(new SetSelectedNetworkViewsEvent(this,
		                                                         new ArrayList<CyNetworkView>(selectedNetworkViews)));
	}

	@Override
	public synchronized List<CyNetwork> getSelectedNetworks() {
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
		
		synchronized (this) {
			if (currentNetwork != null)
				selectedNetworks.add(currentNetwork);
			
			selectedNetworks = selectNetworks(selectedNetworks);
		}

		cyEventHelper.fireEvent(new SetSelectedNetworksEvent(this, new ArrayList<CyNetwork>(selectedNetworks)));
	}

	@Override
	public RenderingEngine<CyNetwork> getCurrentRenderingEngine() {
		return currentRenderer;
	}

	@Override
	public void setCurrentRenderingEngine(RenderingEngine<CyNetwork> engine) {
		boolean changed = (engine == null && currentRenderer != null)
				|| (engine != null && !engine.equals(currentRenderer));
		
		this.currentRenderer = engine;
		
		if (changed)
			cyEventHelper.fireEvent(new SetCurrentRenderingEngineEvent(this, this.currentRenderer));
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
		setCurrentNetwork(null);
		setCurrentNetworkView(null);
		setSelectedNetworkViews(null);
		setCurrentRenderingEngine(null);
		setCurrentTable(null);
	}
}
