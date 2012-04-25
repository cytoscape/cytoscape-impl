/*
 File: NetworkManager.java

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
package org.cytoscape.model.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkDestroyedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of CyNetworkManager.
 */
public class CyNetworkManagerImpl implements CyNetworkManager {

    private static final Logger logger = LoggerFactory.getLogger(CyNetworkManagerImpl.class);

    private final Map<Long, CyNetwork> networkMap;
    private final CyEventHelper cyEventHelper;

    /**
     * 
     * @param cyEventHelper
     */
    public CyNetworkManagerImpl(final CyEventHelper cyEventHelper) {
	this.networkMap = new HashMap<Long, CyNetwork>();
	this.cyEventHelper = cyEventHelper;
    }

    @Override
    public synchronized Set<CyNetwork> getNetworkSet() {
	return new HashSet<CyNetwork>(networkMap.values());
    }

    @Override
    public synchronized CyNetwork getNetwork(long id) {
	return networkMap.get(id);
    }

    @Override
    public synchronized boolean networkExists(long network_id) {
	return networkMap.containsKey(network_id);
    }

    // TODO
    // Does this need to distinguish between root networks and subnetworks?
    @Override
    public void destroyNetwork(CyNetwork network) {
	if (network == null)
	    throw new NullPointerException("Network is null");

	final Long networkId = network.getSUID();

	// check outside the lock so that we fail early
	if (!networkMap.containsKey(networkId))
	    throw new IllegalArgumentException("network is not recognized by this NetworkManager");

	// let everyone know!
	cyEventHelper.fireEvent(new NetworkAboutToBeDestroyedEvent(CyNetworkManagerImpl.this, network));

	synchronized (this) {
	    // check again within the lock in case something has changed
	    if (!networkMap.containsKey(networkId))
		throw new IllegalArgumentException("network is not recognized by this NetworkManager");

		for (CyNode n : network.getNodeList())
		    network.getRow(n).set(CyNetwork.SELECTED, false);
		for (CyEdge e : network.getEdgeList())
		    network.getRow(e).set(CyNetwork.SELECTED, false);

	    networkMap.remove(networkId);
	    
	    // TODO: remove tables!!

	    network = null;
	}

	// let everyone know that some network is gone
	cyEventHelper.fireEvent(new NetworkDestroyedEvent(CyNetworkManagerImpl.this));
    }

    @Override
    public void addNetwork(final CyNetwork network) {
	if (network == null)
	    throw new NullPointerException("Network is null");

	synchronized (this) {
	    logger.debug("Adding new Network Model: Model ID = " + network.getSUID());
	    networkMap.put(network.getSUID(), network);
	}

	cyEventHelper.fireEvent(new NetworkAddedEvent(CyNetworkManagerImpl.this, network));
    }

	@Override
	public synchronized void reset() {
		networkMap.clear();
	}
}
