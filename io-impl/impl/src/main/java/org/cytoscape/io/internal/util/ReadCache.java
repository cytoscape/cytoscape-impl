package org.cytoscape.io.internal.util;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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
import java.util.WeakHashMap;

import org.cytoscape.io.internal.read.xgmml.handler.XGMMLParseUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
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
	private Map<CySubNetwork, Set<CyNode>> unresolvedNodeMap;
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
		unresolvedNodeMap = new WeakHashMap<CySubNetwork, Set<CyNode>>();
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
		unresolvedNodeMap = null;
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
	
	public void addUnresolvedNode(final CyNode node, final CySubNetwork net) {
		Set<CyNode> nodes = unresolvedNodeMap.get(net);
		
		if (nodes == null) {
			nodes = new HashSet<CyNode>();
			unresolvedNodeMap.put(net, nodes);
		}
		
		nodes.add(node);
	}
	
	public boolean removeUnresolvedNode(final CyNode node, final CySubNetwork net) {
		Set<CyNode> nodes = unresolvedNodeMap.get(net);
		return nodes != null ? nodes.remove(node) : false;
	}
	
	public void deleteUnresolvedNodes() {
		// Delete unresolved nodes from
		for (Map.Entry<CySubNetwork, Set<CyNode>> entry : unresolvedNodeMap.entrySet()) {
			final CySubNetwork net = entry.getKey();
			final Set<CyNode> nodes = entry.getValue();
			
			if (net != null && nodes != null && !nodes.isEmpty()) {
				logger.error("The following nodes can't be resolved and will be deleted from network \"" + net + "\": " 
						+ nodes);
				net.removeNodes(nodes);
			}
		}
	}

	public void addElementLink(final String href, final Class<? extends CyIdentifiable> clazz, final CyNetwork net) {
		Map<CyNetwork, Set<Long>> map = null;
		final Long id = XGMMLParseUtil.getIdFromXLink(href);
		
		if (clazz == CyNode.class)
			map = getNodeLinks();
		else if (clazz == CyEdge.class)
			map = getEdgeLinks();
		
		if (map != null && net != null) {
			Set<Long> idSet = map.get(net);
			
			if (idSet == null) {
				idSet = new HashSet<Long>();
				map.put(net, idSet);
			}
			
			idSet.add(id);
		}
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
	
	/**
	 * @return All network tables, except DEFAULT_ATTRS and SHARED_DEFAULT_ATTRS ones.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Set<CyTable> getNetworkTables() {
		final Set<CyTable> tables = new HashSet<CyTable>();
		final Set<CyNetwork> networks = new HashSet<CyNetwork>();
		final Class<?>[] types = new Class[] { CyNetwork.class, CyNode.class, CyEdge.class };
		
		if (networkByIdMap.values() != null)
			networks.addAll(networkByIdMap.values());
		
		for (final CyNetwork n : networks) {
			for (final Class t : types) {
				Map<String, CyTable> tblMap = new HashMap<String, CyTable>(netTblMgr.getTables(n, t));
				tblMap.remove(CyNetwork.DEFAULT_ATTRS);
				
				if (tblMap != null)
					tables.addAll(tblMap.values());
				
				if (n instanceof CySubNetwork) {
					// Don't forget the root-network tables.
					tblMap = new HashMap<String, CyTable>(netTblMgr.getTables(((CySubNetwork) n).getRootNetwork(), t));
					tblMap.remove(CyRootNetwork.DEFAULT_ATTRS);
					tblMap.remove(CyRootNetwork.SHARED_DEFAULT_ATTRS);
					
					if (tblMap != null)
						tables.addAll(tblMap.values());
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
