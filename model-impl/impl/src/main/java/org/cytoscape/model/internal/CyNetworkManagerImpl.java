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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkDestroyedEvent;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of CyNetworkManager.
 */
public class CyNetworkManagerImpl implements CyNetworkManager {

	private static final Logger logger = LoggerFactory .getLogger(CyNetworkManagerImpl.class);

	private final Map<Long, CyNetwork> networkMap;
	private final CyEventHelper cyEventHelper;
	
	private final ReadWriteLock lock;

    /**
     * 
     * @param cyEventHelper
     */
	public CyNetworkManagerImpl(final CyEventHelper cyEventHelper) {
		this.networkMap = new HashMap<Long, CyNetwork>();
		this.cyEventHelper = cyEventHelper;
		lock = new ReentrantReadWriteLock(true);
	}

	@Override
	public Set<CyNetwork> getNetworkSet() {
		lock.readLock().lock();
		try {
			return new HashSet<CyNetwork>(networkMap.values());
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public CyNetwork getNetwork(long id) {
		lock.readLock().lock();
		try {
			return networkMap.get(id);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean networkExists(long network_id) {
		lock.readLock().lock();
		try {
			return networkMap.containsKey(network_id);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void destroyNetwork(final CyNetwork network) {
		if (network == null)
			throw new NullPointerException("Network is null");

		final Long networkId = network.getSUID();

		// check outside the write lock so that we fail early
		lock.readLock().lock();
		try {
			if (!networkMap.containsKey(networkId))
				throw new IllegalArgumentException("network is not recognized by this NetworkManager");
		} finally {
			lock.readLock().unlock();
		}
		
		// let everyone know!
		cyEventHelper.fireEvent(new NetworkAboutToBeDestroyedEvent(CyNetworkManagerImpl.this, network));

		lock.writeLock().lock();
		try {
			// check again within the lock in case something has changed
			if (!networkMap.containsKey(networkId))
				throw new IllegalArgumentException("network is not recognized by this NetworkManager");

			for (CyNode n : network.getNodeList())
				network.getRow(n).set(CyNetwork.SELECTED, false);
			for (CyEdge e : network.getEdgeList())
				network.getRow(e).set(CyNetwork.SELECTED, false);

			networkMap.remove(networkId);
		} finally {
			lock.writeLock().unlock();
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
		lock.readLock().lock();
		try {
			for (CySubNetwork network : rootNetwork.getSubNetworkList()) {
				if (networkMap.containsKey(network.getSUID())) {
					return true;
				}
			}
			
			return false;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void addNetwork(final CyNetwork network) {
		if (network == null)
			throw new NullPointerException("Network is null");

		lock.writeLock().lock();
		try {
			logger.debug("Adding new Network Model: Model ID = " + network.getSUID());
			networkMap.put(network.getSUID(), network);
		} finally {
			lock.writeLock().unlock();
		}

		cyEventHelper.fireEvent(new NetworkAddedEvent(CyNetworkManagerImpl.this, network));
	}

	@Override
	public void reset() {
		lock.writeLock().lock();
		try {
			networkMap.clear();
		} finally {
			lock.writeLock().unlock();
		}
	}
}
