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
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadCache {
	
	/* Map of new to old element IDs */
	private Map<Long, Object> oldIdMap;
	/* Map of old ID to node/edge indexes */
	private Map<Object, Integer> indexMap;
	
	private Map<CyNode, Object/*network's id*/> networkPointerMap;
	
	/* Maps of XML ID's to elements (the keys should be a Long if reading a Cy3 session file) */
	private Map<Object, CyNetwork> networkById;
	private Map<Object, CyNode> nodeById;
	private Map<Object, CyEdge> edgeById;
	
	/* Maps of node labels to nodes (necessary because of 2.x sessions, which uses the node label as its session ID) */
	private Map<String, CyNode> nodeByName;
	
	private Map<CyNetwork, Set<Long>> nodeLinkMap;
	private Map<CyNetwork, Set<Long>> edgeLinkMap;
	
	private static final Logger logger = LoggerFactory.getLogger(ReadCache.class);
	
	public void init() {
		oldIdMap = new HashMap<Long, Object>();
		indexMap = new HashMap<Object, Integer>();
		
		nodeById = new HashMap<Object, CyNode>();
		edgeById = new HashMap<Object, CyEdge>();
		networkById = new HashMap<Object, CyNetwork>();
		
		nodeByName = new HashMap<String, CyNode>();
		
		nodeLinkMap = new HashMap<CyNetwork, Set<Long>>();
		edgeLinkMap = new HashMap<CyNetwork, Set<Long>>();
		networkPointerMap = new HashMap<CyNode, Object>();
	}
	
	public void dispose() {
		nodeById = null;
		edgeById = null;
		networkById = null;
		
		nodeByName = null;
		
		nodeLinkMap = null;
		edgeLinkMap = null;
		networkPointerMap = null;
	}
	
	/**
	 * Cache the element for future reference.
	 * @param xgmmlId The XGMML id of the element.
	 * @param element A CyNetwork, CyNetworkView, CyNode or CyEdge.
	 */
	public void cache(Object xgmmlId, CyTableEntry element) {
    	int index = -1;
    	
    	if (element instanceof CyNode) {
    		if (xgmmlId != null)
    			nodeById.put(xgmmlId, (CyNode) element);
    		
    		index = ((CyNode) element).getIndex();
    	} else if (element instanceof CyEdge) {
    		if (xgmmlId != null)
    			edgeById.put(xgmmlId, (CyEdge) element);
    		
    		index = ((CyEdge) element).getIndex();
    	} else if (element instanceof CyNetwork) {
    		if (xgmmlId != null)
    			networkById.put(xgmmlId, (CyNetwork) element);
	    }
	    
    	if (xgmmlId != null) {
	    	oldIdMap.put(element.getSUID(), xgmmlId);
			indexMap.put(xgmmlId, index);
    	}
    }
	
	/**
	 * Probably only necessary when parsing 2.x session files.
	 * @param name
	 * @param node
	 */
	public void cacheNodeByName(String name, CyNode node) {
		if (name != null && !name.isEmpty() && node != null)
			nodeByName.put(name,  node);
	}
	
	public void addNetworkPointer(CyNode node, Object networkId) {
		if (node == null)
			throw new NullPointerException("Cannot parse network pointer: node is null.");
		if (networkId == null)
			throw new NullPointerException("Cannot parse network pointer: network id is null.");
		
		networkPointerMap.put(node, networkId);
	}
	
	public Object getOldId(Long suid) {
		return oldIdMap.get(suid);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CyTableEntry> T getObjectById(Object oldId, Class<T> type) {
		if (type == CyNetwork.class)
			return (T) networkById.get(oldId);
		if (type == CyNode.class)
			return (T) nodeById.get(oldId);
		if (type == CyEdge.class)
			return (T) edgeById.get(oldId);
		
		return null;
	}
	
	public Integer getIndex(Object oldId) {
		return indexMap.get(oldId);
	}
	
	public CyNetwork getNetwork(Object oldId) {
		return networkById.get(oldId);
	}
	
	public CyNode getNode(Object oldId) {
		return nodeById.get(oldId);
	}
	
	public CyEdge getEdge(Object oldId) {
		return edgeById.get(oldId);
	}
	
	public CyNode getNodeByName(String nodeName) {
		return nodeByName.get(nodeName);
	}
	
	public Map<CyNetwork, Set<Long>> getNodeLinks() {
		return nodeLinkMap;
	}

	public Map<CyNetwork, Set<Long>> getEdgeLinks() {
		return edgeLinkMap;
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
