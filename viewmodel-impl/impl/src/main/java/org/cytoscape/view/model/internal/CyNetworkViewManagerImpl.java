/*
 File: CyNetworkViewManagerImpl.java

 Copyright (c) 2006, 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.view.model.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
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

	/**
	 * 
	 * @param cyEventHelper
	 */
	public CyNetworkViewManagerImpl(final CyEventHelper cyEventHelper) {
		networkViewMap = new WeakHashMap<CyNetwork, Collection<CyNetworkView>>();
		this.cyEventHelper = cyEventHelper;
	}

	@Override
	public synchronized void reset() {
		networkViewMap.clear();
	}

	@Override
	public synchronized void handleEvent(final NetworkAboutToBeDestroyedEvent event) {
		final CyNetwork network = event.getNetwork();
		if (viewExists(network)) {
			// Remove ALL views associated with this network model
			for (final CyNetworkView view : networkViewMap.get(network))
				destroyNetworkView(view);
		}
	}

	@Override
	public synchronized Set<CyNetworkView> getNetworkViewSet() {
		final Set<CyNetworkView> views = new HashSet<CyNetworkView>();
		
		final Collection<Collection<CyNetworkView>> vals = networkViewMap.values();
		for (Collection<CyNetworkView> setFoViews : vals)
			views.addAll(setFoViews);

		return views;
	}

	@Override
	public synchronized Collection<CyNetworkView> getNetworkViews(final CyNetwork network) {
		final Collection<CyNetworkView> views = networkViewMap.get(network); 
		
		if(views != null)
			return views;
		else
			return new HashSet<CyNetworkView>();
	}

	@Override
	public synchronized boolean viewExists(final CyNetwork network) {
		if(networkViewMap.containsKey(network) == false)
			return false;
		
		final Collection<CyNetworkView> views = networkViewMap.get(network);
		if(views.size() == 0)
			return false;
		else
			return true;
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
		if (!networkViewMap.containsKey(network))
			throw new IllegalArgumentException("network view is not recognized by this NetworkManager");

		// let everyone know!
		cyEventHelper.fireEvent(new NetworkViewAboutToBeDestroyedEvent(this, view));

		synchronized (this) {
			// do this again within the lock to be safe
			if (!networkViewMap.containsKey(network))
				throw new IllegalArgumentException("network view is not recognized by this NetworkManager");

			final Collection<CyNetworkView> views = networkViewMap.get(network);
			views.remove(view);
			networkViewMap.put(network, views);
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
		synchronized (this) {
			Collection<CyNetworkView> existingSet = networkViewMap.get(network);

			if (existingSet == null)
				existingSet = new HashSet<CyNetworkView>();
			existingSet.add(view);
			networkViewMap.put(network, existingSet);
		}
		cyEventHelper.fireEvent(new NetworkViewAddedEvent(this, view));
	}
}
