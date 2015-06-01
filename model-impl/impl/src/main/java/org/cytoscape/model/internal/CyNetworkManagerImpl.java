package org.cytoscape.model.internal;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkDestroyedEvent;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of CyNetworkManager.
 */
public class CyNetworkManagerImpl implements CyNetworkManager {

	private static final Logger logger = LoggerFactory.getLogger(CyNetworkManagerImpl.class);

	private final Map<Long, CyNetwork> networkMap;
	private final CyServiceRegistrar serviceRegistrar;
	
	private final Object lock = new Object();

    /**
     * 
     * @param cyEventHelper
     */
	public CyNetworkManagerImpl(final CyServiceRegistrar serviceRegistrar) {
		this.networkMap = new HashMap<Long, CyNetwork>();
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public Set<CyNetwork> getNetworkSet() {
		synchronized (lock) {
			return new HashSet<CyNetwork>(networkMap.values());
		}
	}

	@Override
	public CyNetwork getNetwork(long id) {
		synchronized (lock) {
			return networkMap.get(id);
		}
	}

	@Override
	public boolean networkExists(long network_id) {
		synchronized (lock) {
			return networkMap.containsKey(network_id);
		}
	}

	@Override
	public void destroyNetwork(final CyNetwork network) {
		if (network == null)
			throw new NullPointerException("Network is null");

		final Long networkId = network.getSUID();

		synchronized (lock) {
			if (!networkMap.containsKey(networkId))
				throw new IllegalArgumentException("network is not recognized by this NetworkManager");
		}
		
		final CyEventHelper cyEventHelper = serviceRegistrar.getService(CyEventHelper.class);
		
		// let everyone know!
		cyEventHelper.fireEvent(new NetworkAboutToBeDestroyedEvent(CyNetworkManagerImpl.this, network));

		synchronized (lock) {
			// check again within the lock in case something has changed
			if (!networkMap.containsKey(networkId))
				throw new IllegalArgumentException("network is not recognized by this NetworkManager");

			for (CyNode n : network.getNodeList())
				network.getRow(n).set(CyNetwork.SELECTED, false);
			for (CyEdge e : network.getEdgeList())
				network.getRow(e).set(CyNetwork.SELECTED, false);

			networkMap.remove(networkId);
		}

		if (network instanceof CySubNetwork) {
			final CySubNetwork subNetwork = (CySubNetwork) network;
			final CyRootNetwork rootNetwork = subNetwork.getRootNetwork();
			final CySubNetwork baseNetwork = rootNetwork.getBaseNetwork();

			if (!subNetwork.equals(baseNetwork) || rootNetwork.getSubNetworkList().size() > 1) {
				rootNetwork.removeSubNetwork(subNetwork);
				network.dispose();
			}

			if (!hasRegisteredNetworks(rootNetwork))
				rootNetwork.dispose();
		} else {
			network.dispose();
		}

		// let everyone know that some network is gone
		cyEventHelper.fireEvent(new NetworkDestroyedEvent(CyNetworkManagerImpl.this));
	}

	private boolean hasRegisteredNetworks(final CyRootNetwork rootNetwork) {
		synchronized (lock) {
			for (CySubNetwork network : rootNetwork.getSubNetworkList()) {
				if (networkMap.containsKey(network.getSUID())) {
					return true;
				}
			}
			
			return false;
		}
	}

	@Override
	public void addNetwork(final CyNetwork network) {
		if (network == null)
			throw new NullPointerException("Network is null");

		synchronized (lock) {
			logger.debug("Adding new Network Model: Model ID = " + network.getSUID());
			
			// Make sure the network has a name
			final CyRow row = network.getRow(network);
			final String name = row.get(CyNetwork.NAME, String.class);
			final String sharedName = row.get(CyRootNetwork.SHARED_NAME, String.class);
			
			if (name != null && !name.trim().isEmpty() && (sharedName == null || sharedName.trim().isEmpty())) {
				row.set(CyRootNetwork.SHARED_NAME, name);
			} else if (sharedName != null && !sharedName.trim().isEmpty() && (name == null || name.trim().isEmpty())) {
				row.set(CyNetwork.NAME, sharedName);
			} else if ((sharedName == null || sharedName.trim().isEmpty()) && (name == null || name.trim().isEmpty())) {
				final CyNetworkNaming namingUtil = serviceRegistrar.getService(CyNetworkNaming.class);
				final String newName = namingUtil.getSuggestedNetworkTitle("Network");
				row.set(CyNetwork.NAME, newName);
				row.set(CyRootNetwork.SHARED_NAME, newName);
			}
			
			// Add the new network to the internal map
			networkMap.put(network.getSUID(), network);
		}

		final CyEventHelper cyEventHelper = serviceRegistrar.getService(CyEventHelper.class);
		cyEventHelper.fireEvent(new NetworkAddedEvent(CyNetworkManagerImpl.this, network));
	}

	@Override
	public void reset() {
		synchronized (lock) {
			networkMap.clear();
		}
	}
}
