/*
 Copyright (c) 2006, 2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.io.internal.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadCache {
	
	/* Map of new to old element IDs */
	private Map<Long, Object> oldIdMap;
	
	/* Maps of XML ID's to elements (the keys should be a Long if reading a Cy3 session file) */
	private Map<Object, CyNetwork> networkByIdMap;
	private Map<Object, CyNetworkView> networkViewByIdMap;
	private Map<Object, CyNode> nodeByIdMap;
	private Map<Object, CyEdge> edgeByIdMap;
	
	/* Maps of node labels to nodes (necessary because of 2.x sessions, which uses the node label as its session ID) */
	private Map<Object, CyNode> nodeByNameMap;
	
	private Map<CyNetwork, Set<Long>> nodeLinkMap;
	private Map<CyNetwork, Set<Long>> edgeLinkMap;
	private Map<CyNode, Object/*network's id*/> networkPointerMap;
	
	private final CyNetworkTableManager netTblMgr;
	
	private static final Logger logger = LoggerFactory.getLogger(ReadCache.class);
	
	
	public ReadCache(final CyNetworkTableManager netTblMgr) {
		this.netTblMgr = netTblMgr;
	}

	public void init() {
		oldIdMap = new HashMap<Long, Object>();
		nodeByIdMap = new HashMap<Object, CyNode>();
		edgeByIdMap = new HashMap<Object, CyEdge>();
		networkByIdMap = new HashMap<Object, CyNetwork>();
		networkViewByIdMap = new HashMap<Object, CyNetworkView>();
		nodeByNameMap = new HashMap<Object, CyNode>();
		nodeLinkMap = new WeakHashMap<CyNetwork, Set<Long>>();
		edgeLinkMap = new WeakHashMap<CyNetwork, Set<Long>>();
		networkPointerMap = new WeakHashMap<CyNode, Object>();
	}
	
	public void dispose() {
		nodeByIdMap = null;
		edgeByIdMap = null;
		networkByIdMap = null;
		networkViewByIdMap = null;
		nodeByNameMap = null;
		nodeLinkMap = null;
		edgeLinkMap = null;
		networkPointerMap = null;
	}
	
	/**
	 * Cache the element for future reference.
	 * @param xgmmlId The XGMML id of the element.
	 * @param element A CyNetwork, CyNetworkView, CyNode or CyEdge.
	 */
	public void cache(Object xgmmlId, CyIdentifiable element) {
		if (xgmmlId != null) {
			if (element instanceof CyNode) {
				nodeByIdMap.put(xgmmlId, (CyNode) element);
			} else if (element instanceof CyEdge) {
				edgeByIdMap.put(xgmmlId, (CyEdge) element);
			} else if (element instanceof CyNetwork) {
				networkByIdMap.put(xgmmlId, (CyNetwork) element);
			} else if (element instanceof CyNetworkView) {
				networkViewByIdMap.put(xgmmlId, (CyNetworkView) element);
			}

			oldIdMap.put(element.getSUID(), xgmmlId);
		}
    }
	
	/**
	 * Probably only necessary when parsing 2.x session files.
	 * @param name
	 * @param node
	 */
	public void cacheNodeByName(String name, CyNode node) {
		if (name != null && !name.isEmpty() && node != null)
			nodeByNameMap.put(name,  node);
	}
	
	public void addNetworkPointer(CyNode node, Object networkId) {
		if (node == null)
			throw new NullPointerException("Cannot parse network pointer: node is null.");
		if (networkId == null)
			throw new NullPointerException("Cannot parse network pointer: network id is null.");
		
		networkPointerMap.put(node, networkId);
	}
	
	public Object getNetworkPointerId(CyNode node) {
		return networkPointerMap.get(node);
	}
	
	public boolean hasNetworkPointers() {
		return !networkPointerMap.isEmpty();
	}
	
	public Object getOldId(Long suid) {
		return oldIdMap.get(suid);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CyIdentifiable> T getObjectById(Object oldId, Class<T> type) {
		if (type == CyNetwork.class)
			return (T) networkByIdMap.get(oldId);
		if (type == CyNetworkView.class)
			return (T) networkViewByIdMap.get(oldId);
		if (type == CyNode.class)
			return (T) nodeByIdMap.get(oldId);
		if (type == CyEdge.class)
			return (T) edgeByIdMap.get(oldId);
		
		return null;
	}
	
	public CyNetwork getNetwork(Object oldId) {
		return networkByIdMap.get(oldId);
	}
	
	public CyNetworkView getNetworkView(Object oldId) {
		return networkViewByIdMap.get(oldId);
	}
	
	public CyNode getNode(Object oldId) {
		return nodeByIdMap.get(oldId);
	}
	
	public CyEdge getEdge(Object oldId) {
		return edgeByIdMap.get(oldId);
	}
	
	public CyNode getNodeByName(String nodeName) {
		return nodeByNameMap.get(nodeName);
	}
	
	public Map<CyNetwork, Set<Long>> getNodeLinks() {
		return nodeLinkMap;
	}

	public Map<CyNetwork, Set<Long>> getEdgeLinks() {
		return edgeLinkMap;
	}
	
	public Map<Object, CyNetwork> getNetworkByIdMap() {
		return networkByIdMap;
	}
	
	public Map<Object, CyNetworkView> getNetworkViewByIdMap() {
		return networkViewByIdMap;
	}

	public Map<Object, CyNode> getNodeByIdMap() {
		return nodeByIdMap;
	}

	public Map<Object, CyEdge> getEdgeByIdMap() {
		return edgeByIdMap;
	}

	public Map<Object, CyNode> getNodeByNameMap() {
		return nodeByNameMap;
	}
	
	public Set<CyTable> getNetworkTables() {
		final Set<CyTable> tables = new HashSet<CyTable>();
		final Set<CyNetwork> networks = new HashSet<CyNetwork>();
		final Class<?>[] types = new Class[] { CyNetwork.class, CyNode.class, CyEdge.class };
		
		if (networkByIdMap.values() != null)
			networks.addAll(networkByIdMap.values());
		
		for (final CyNetwork n : networks) {
			for (final Class t : types) {
				Map<String, CyTable> tabMap = netTblMgr.getTables(n, t);
				
				if (tabMap != null)
					tables.addAll(tabMap.values());
				
				if (n instanceof CySubNetwork) {
					// Don't forget the root-network tables.
					tabMap = netTblMgr.getTables(((CySubNetwork) n).getRootNetwork(), t);
					
					if (tabMap != null)
						tables.addAll(tabMap.values());
				}
			}
		}
			
		return tables;
	}
	
	public void createNetworkPointers() {
		if (networkPointerMap != null) {
			// Iterate the rows and recreate the network pointers
			for (Map.Entry<CyNode, Object> entry : networkPointerMap.entrySet()) {
				final CyNode node = entry.getKey();
				final Object oldNetId = entry.getValue();
				CyNetwork network = getNetwork(oldNetId);
				
				if (network != null) {
					node.setNetworkPointer(network);
				} else {
					logger.error("Cannot recreate network pointer: Cannot find network " + oldNetId);
				}
			}
		}
	}
}
