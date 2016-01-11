package org.cytoscape.view.model.internal;

/*
 * #%L
 * Cytoscape View Model Impl (viewmodel-impl)
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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewDestroyedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of CyNetworkViewManager.
 */
public class CyNetworkViewManagerImpl implements CyNetworkViewManager, NetworkAboutToBeDestroyedListener {
	
	private static final Logger logger = LoggerFactory.getLogger(CyNetworkViewManagerImpl.class);

	private final Map<CyNetwork, Collection<CyNetworkView>> networkViewMap;
	private final CyEventHelper cyEventHelper;
	private final CyNetworkManager netMgr;
	private final Set<CyNetworkView> viewsAboutToBeDestroyed;

	private final Object lock = new Object();

	public CyNetworkViewManagerImpl(final CyEventHelper cyEventHelper, final CyNetworkManager netMgr) {
		networkViewMap = new WeakHashMap<>();
		viewsAboutToBeDestroyed = new HashSet<>();
		this.cyEventHelper = cyEventHelper;
		this.netMgr = netMgr;
	}

	@Override
	public void reset() {
		synchronized (lock) {
			networkViewMap.clear();
		}
	}

	@Override
	public void handleEvent(final NetworkAboutToBeDestroyedEvent event) {
		final CyNetwork network = event.getNetwork();
		
		synchronized (lock) {
			// Remove ALL views associated with this network model
			final Collection<CyNetworkView> viewList = getNetworkViews(network);
			
			for (final CyNetworkView view : viewList)
				destroyNetworkView(view);
		}
	}

	@Override
	public Set<CyNetworkView> getNetworkViewSet() {
		final Set<CyNetworkView> views = new LinkedHashSet<>();
		
		synchronized (lock) {
			final Collection<Collection<CyNetworkView>> vals = networkViewMap.values();
			
			for (Collection<CyNetworkView> setFoViews : vals)
				views.addAll(setFoViews);
	
			views.removeAll(viewsAboutToBeDestroyed);
		}
		
		return views;
	}

	@Override
	public Collection<CyNetworkView> getNetworkViews(final CyNetwork network) {
		synchronized (lock) {
			final Collection<CyNetworkView> views = networkViewMap.get(network); 
			
			return views != null ? new LinkedHashSet<>(views) : new LinkedHashSet<>();
		}
	}

	@Override
	public boolean viewExists(final CyNetwork network) {
		synchronized (lock) {
			final Collection<CyNetworkView> views = networkViewMap.get(network);
			
			return views != null && !views.isEmpty();
		}
	}

	@Override
	public void destroyNetworkView(CyNetworkView view) {
		if (view == null) {
			// Do nothing if view is null.
			logger.warn("Network view is null.");
			return;
		}

		final CyNetwork network = view.getModel();

		// do this outside of the lock to fail early
		synchronized (lock) {
			if (!networkViewMap.containsKey(network))
				throw new IllegalArgumentException("network view is not recognized by this NetworkManager");
		
			viewsAboutToBeDestroyed.add(view);
		}
		
		// let everyone know!
		cyEventHelper.fireEvent(new NetworkViewAboutToBeDestroyedEvent(this, view));

		synchronized (lock) {
			// do this again within the lock to be safe
			if (!networkViewMap.containsKey(network))
				throw new IllegalArgumentException("network view is not recognized by this NetworkManager");

			final Collection<CyNetworkView> views = networkViewMap.get(network);
			views.remove(view);
			networkViewMap.put(network, views);
		
			viewsAboutToBeDestroyed.remove(view);
			view.dispose();
		}
		
		cyEventHelper.fireEvent(new NetworkViewDestroyedEvent(this));
		view = null;
	}

	@Override
	public void addNetworkView(final CyNetworkView view) {
		if (view == null) {
			// Do nothing if view is null.
			logger.warn("Network view is null.");
			return;
		}
		
		final CyNetwork network = view.getModel();
		
		synchronized (lock) {
			if (!netMgr.networkExists(network.getSUID()))
				throw new IllegalArgumentException(
						"Network view cannot be added, because its network ("
								+ network + ") is not registered");
			
			Collection<CyNetworkView> existingSet = networkViewMap.get(network);

			if (existingSet == null)
				existingSet = new LinkedHashSet<>();
			
			existingSet.add(view);
			networkViewMap.put(network, existingSet);
		}
		cyEventHelper.fireEvent(new NetworkViewAddedEvent(this, view));
	}
}
