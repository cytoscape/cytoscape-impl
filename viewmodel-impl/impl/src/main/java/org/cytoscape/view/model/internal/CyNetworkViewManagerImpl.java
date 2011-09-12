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


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.di.util.DIUtil;
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

	private final Map<Long, CyNetworkView> networkViewMap;
	private final CyEventHelper cyEventHelper;

	/**
	 * 
	 * @param cyEventHelper
	 */
	public CyNetworkViewManagerImpl(final CyEventHelper cyEventHelper) {
		networkViewMap = new HashMap<Long, CyNetworkView>();
		this.cyEventHelper = DIUtil.stripProxy(cyEventHelper);
	}

	@Override
	public synchronized void reset() {
		networkViewMap.clear();
	}

	@Override
	public synchronized void handleEvent(final NetworkAboutToBeDestroyedEvent event) {
		final long networkId = event.getNetwork().getSUID();
		if (viewExists(networkId))
			destroyNetworkView(networkViewMap.get(networkId));
	}

	@Override
	public synchronized Set<CyNetworkView> getNetworkViewSet() {
		return new HashSet<CyNetworkView>(networkViewMap.values());
	}

	@Override
	public synchronized CyNetworkView getNetworkView(long networkId) {
		return networkViewMap.get(networkId);
	}

	@Override
	public synchronized boolean viewExists(long networkId) {
		return networkViewMap.containsKey(networkId);
	}

	@Override
	public void destroyNetworkView(CyNetworkView view) {
		if (view == null)
			throw new NullPointerException("view is null");

		final Long viewID = view.getModel().getSUID();

		// do this outside of the lock to fail early
		if (!networkViewMap.containsKey(viewID))
			throw new IllegalArgumentException("network view is not recognized by this NetworkManager");

		// let everyone know!
		cyEventHelper.fireEvent(new NetworkViewAboutToBeDestroyedEvent(this, view));

		synchronized (this) {
			// do this again within the lock to be safe
			if (!networkViewMap.containsKey(viewID))
				throw new IllegalArgumentException("network view is not recognized by this NetworkManager");

			networkViewMap.remove(viewID);
		}

		cyEventHelper.fireEvent(new NetworkViewDestroyedEvent(this));
		view = null;
		logger.debug("######### Network View deleted: " + viewID);
	}

	@Override
	public void addNetworkView(final CyNetworkView view) {
		if (view == null)
			throw new NullPointerException("CyNetworkView is null");

		final CyNetwork network = view.getModel();
		long networkId = network.getSUID();

		synchronized (this) {
			logger.debug("Adding new Network View Model: Model ID = " + networkId);
			networkViewMap.put(networkId, view);
		}

		logger.debug("Firing event: NetworkViewAddedEvent");
		cyEventHelper.fireEvent(new NetworkViewAddedEvent(this, view));
		logger.debug("Done event: NetworkViewAddedEvent");
	}
}
