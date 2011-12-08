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
	
	/* Map of old to new element IDs */
	private Map<String, Long> idMap;
	/* Map of new to old element IDs */
	private Map<Long, String> oldIdMap;
	/* Map of old ID to node/edge indexes */
	private Map<String, Integer> indexMap;
	
	private Map<Long/*node id*/, String/*old network id*/> networkPointerMap;
	
	/* Map of XML ID's to nodes */
	private Map<String, CyNode> nodeIdMap;
	/* Map of XML ID's to edges */
	private Map<String, CyEdge> edgeIdMap;
	/* Map of XML ID's to networks */
	private Map<String, CyNetwork> networkIdMap;
	
	private Map<CyNetwork, Set<String>> nodeLinkMap;
	private Map<CyNetwork, Set<String>> edgeLinkMap;
	
	private static final Logger logger = LoggerFactory.getLogger(ReadCache.class);
	
	public void init() {
		idMap = new HashMap<String, Long>();
		oldIdMap = new HashMap<Long, String>();
		indexMap = new HashMap<String, Integer>();
		networkPointerMap = new HashMap<Long, String>();
		
		nodeIdMap = new HashMap<String, CyNode>();
		edgeIdMap = new HashMap<String, CyEdge>();
		networkIdMap = new HashMap<String, CyNetwork>();
		
		nodeLinkMap = new HashMap<CyNetwork, Set<String>>();
		edgeLinkMap = new HashMap<CyNetwork, Set<String>>();
	}
	
	public void dispose() {
		nodeIdMap = null;
		edgeIdMap = null;
		networkIdMap = null;
		
		nodeLinkMap = null;
		edgeLinkMap = null;
	}
	
	public <T extends CyTableEntry> void cache(T element, String strId) {
    	int index = -1;
    	
    	if (element instanceof CyNode) {
    		nodeIdMap.put(strId, (CyNode) element);
    		index = ((CyNode) element).getIndex();
    	} else if (element instanceof CyEdge) {
    		edgeIdMap.put(strId, (CyEdge) element);
    		index = ((CyEdge) element).getIndex();
    	} else if (element instanceof CyNetwork) {
	    	networkIdMap.put(strId, (CyNetwork) element);
	    }
    	
        this.cache(strId, element.getSUID(), index);
    }
    
	public void cache(String oldId, long newId, int index) {
		if (oldId != null && !oldId.isEmpty()) {
			idMap.put(oldId, newId);
			oldIdMap.put(newId, oldId);
			indexMap.put(oldId, index);
		}
	}
	
	public void cache(String oldId, long newId) {
		cache(oldId, newId, 0);
	}
	
	public void addNetworkPointer(Long nodeId, String oldNetworkId) {
		networkPointerMap.put(nodeId, oldNetworkId);
	}
	
	public String getOldId(Long suid) {
		return oldIdMap.get(suid);
	}

	public Long getNewId(String oldId) {
		return idMap.get(oldId);
	}
	
	public Integer getIndex(String oldId) {
		return indexMap.get(oldId);
	}
	
	public CyNetwork getNetwork(String oldId) {
		return networkIdMap.get(oldId);
	}
	
	public CyNode getNode(String oldId) {
		return nodeIdMap.get(oldId);
	}
	
	public CyEdge getEdge(String oldId) {
		return edgeIdMap.get(oldId);
	}
	
	public Map<CyNetwork, Set<String>> getNodeLinks() {
		return nodeLinkMap;
	}

	public Map<CyNetwork, Set<String>> getEdgeLinks() {
		return edgeLinkMap;
	}
	
	public Map<String, Long> getIdMap() {
		return idMap;
	}
	
	public void createNetworkPointers() {
		if (networkPointerMap != null) {
			// Iterate the rows and recreate the network pointers
			for (Map.Entry<Long, String> entry : networkPointerMap.entrySet()) {
				final Long nodeId = entry.getKey();
				final String oldNetId = entry.getValue();
				CyNetwork network = getNetwork(oldNetId);
				
				if (network != null) {
					String oldNodeId = getOldId(nodeId);
					CyNode node = getNode(oldNodeId);
					
					if (node != null)
						node.setNetworkPointer(network);
					else
						logger.error("Cannot recreate network pointer for network " + oldNetId + ": Cannot find node "
								+ oldNodeId);
				} else {
					logger.error("Cannot recreate network pointer: Cannot find network " + oldNetId);
				}
			}
		}
	}
}
