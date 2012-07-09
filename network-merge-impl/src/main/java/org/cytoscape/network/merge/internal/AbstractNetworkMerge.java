/* File: AbstractNetworkMerge.java

 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

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

package org.cytoscape.network.merge.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;

import org.cytoscape.work.TaskMonitor;

/**
 * NetworkMerge implement
 */
public abstract class AbstractNetworkMerge implements NetworkMerge {
	
	protected boolean withinNetworkMerge = false;
	protected final TaskMonitor taskMonitor;
	
	// For canceling task
	private boolean interrupted;

	public AbstractNetworkMerge(final TaskMonitor taskMonitor) {
		this.taskMonitor = taskMonitor;
		interrupted = false;
	}

	public void setWithinNetworkMerge(boolean withinNetworkMerge) {
		this.withinNetworkMerge = withinNetworkMerge;
	}

	public void interrupt() {
		interrupted = true;
	}

	/**
	 * Check whether two nodes match
	 * 
	 * @param n1
	 *            ,n2 two nodes belongs to net1 and net2 respectively
	 * 
	 * @return true if n1 and n2 matches
	 */
	protected abstract boolean matchNode(CyNetwork net1, CyNode n1, CyNetwork net2, CyNode n2);

	/**
	 * Merge (matched) nodes into one
	 * 
	 * @param mapNetNode
	 *            map of network to node, node in the network to be merged
	 * @param newNode
	 *            merge data to this new node
	 */
	protected abstract void mergeNode(Map<CyNetwork, Set<CyNode>> mapNetNode, CyNode newNode, CyNetwork newNetwork);

	/**
	 * Merge (matched) nodes into one. This method will be refactored in
	 * Cytoscape3
	 * 
	 * @param mapNetEdge
	 *            map from network to Edge, Edge in the network to be merged
	 * @param newEdge
	 *            merge data to this edge
	 * 
	 * @return merged Edge
	 */
	protected abstract void mergeEdge(Map<CyNetwork, Set<CyEdge>> mapNetEdge, CyEdge newEdge, CyNetwork newNetwork);

	/**
	 * Check whether two edges match
	 * 
	 * @param e1
	 *            ,e2 two edges belongs to net1 and net2 respectively
	 * 
	 * @return true if n1 and n2 matches
	 */
	protected boolean matchEdge(CyNetwork network1, CyNetwork network2, CyEdge e1, CyEdge e2,
			Set<Set<CyNode>> matchedNodes) {
		if (e1 == null || e2 == null) {
			throw new NullPointerException();
		}

		// TODO should interaction be considered or not?
		String i1 = network1.getRow(e1).get("interaction", String.class);
		String i2 = network2.getRow(e2).get("interaction", String.class);

		if ((i1 == null && i2 != null) || (i1 != null && i2 == null)) {
			return false;
		}

		if (i1 != null && !i1.equals(i2))
			return false;

		if (e1.isDirected()) { // directed
			if (!e2.isDirected())
				return false;
			return matchedNodes.contains(getNodePair(e1.getSource(), e2.getSource()))
					&& matchedNodes.contains(getNodePair(e1.getTarget(), e2.getTarget()));
		} else { // non directed
			if (e2.isDirected())
				return false;
			if (matchedNodes.contains(getNodePair(e1.getSource(), e2.getSource()))
					&& matchedNodes.contains(getNodePair(e1.getTarget(), e2.getTarget())))
				return true;
			if (matchedNodes.contains(getNodePair(e1.getSource(), e2.getTarget()))
					&& matchedNodes.contains(getNodePair(e1.getTarget(), e2.getSource())))
				return true;
			return false;
		}
	}

	private Set<CyNode> getNodePair(CyNode n1, CyNode n2) {
		Set<CyNode> mn = new HashSet<CyNode>(2);
		mn.add(n1);
		mn.add(n2);
		return mn;
	}

	protected abstract void proprocess(CyNetwork toNetwork);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CyNetwork mergeNetwork(final CyNetwork toNetwork, final List<CyNetwork> fromNetworks, final Operation op) {
		if (toNetwork == null || fromNetworks == null || op == null) {
			throw new java.lang.NullPointerException();
		}

		if (fromNetworks.isEmpty()) {
			throw new IllegalArgumentException("No merging network");
		}

		proprocess(toNetwork);

		// get node matching list
		Set<Set<CyNode>> matchedNodes = new HashSet<Set<CyNode>>();
		List<Map<CyNetwork, Set<CyNode>>> matchedNodeList = getMatchedList(fromNetworks, true, matchedNodes);

		matchedNodeList = selectMatchedGOList(matchedNodeList, op, fromNetworks);

		final Map<CyNode, CyNode> mapNN = new HashMap<CyNode, CyNode>();

		// merge nodes in the list
		taskMonitor.setStatusMessage("Merging nodes...");
		final double nNode = matchedNodeList.size();
		for (int i = 0; i < nNode; i++) {
			if (interrupted)
				return null;
			
			taskMonitor.setProgress(((double)(i + 1)/ nNode)*0.5d);

			final Map<CyNetwork, Set<CyNode>> mapNetNode = matchedNodeList.get(i);
			if (mapNetNode == null || mapNetNode.isEmpty())
				continue;

			CyNode node = toNetwork.addNode();
			mergeNode(mapNetNode, node, toNetwork);

			final Iterator<Set<CyNode>> itNodes = mapNetNode.values().iterator();
			while (itNodes.hasNext()) {
				final Set<CyNode> nodes_ori = itNodes.next();
				final Iterator<CyNode> itNode = nodes_ori.iterator();
				while (itNode.hasNext()) {
					final CyNode node_ori = itNode.next();
					mapNN.put(node_ori, node);
				}
			}
		}

		// match edges
		taskMonitor.setStatusMessage("Merging edges...");
		List<Map<CyNetwork, Set<CyEdge>>> matchedEdgeList = getMatchedList(fromNetworks, false, matchedNodes);
		matchedEdgeList = selectMatchedGOList(matchedEdgeList, op, fromNetworks);

		// merge edges
		final double nEdge = matchedEdgeList.size();
		for (int i = 0; i < nEdge; i++) {
			if (interrupted)
				return null;
			
			taskMonitor.setProgress(((double)(i + 1) / nEdge)*0.5d + 0.5d);

			final Map<CyNetwork, Set<CyEdge>> mapNetEdge = matchedEdgeList.get(i);
			if (mapNetEdge == null || mapNetEdge.isEmpty())
				continue;

			// get the source and target nodes in merged network
			final Iterator<Set<CyEdge>> itEdges = mapNetEdge.values().iterator();

			final Set<CyEdge> edgeSet = itEdges.next();
			if (edgeSet == null || edgeSet.isEmpty()) {
				throw new java.lang.IllegalStateException("Null or empty edge set");
			}

			final CyEdge edge_ori = edgeSet.iterator().next();
			final CyNode source = mapNN.get(edge_ori.getSource());
			final CyNode target = mapNN.get(edge_ori.getTarget());
			if (source == null || target == null) { // some of the node may be
													// exluded when intersection
													// or difference
				continue;
			}

			final boolean directed = edge_ori.isDirected();

			CyEdge edge = toNetwork.addEdge(source, target, directed);
			mergeEdge(mapNetEdge, edge, toNetwork);
		}

		return toNetwork;
	}

	/**
	 * Get a list of matched nodes/edges
	 * 
	 * @param networks
	 *            Networks to be merged
	 * @param isNode
	 *            true if for node
	 * @param matchedNode
	 *            store matched node pares
	 * 
	 * @return list of map from network to node/edge
	 */
	private <T extends CyIdentifiable> List<Map<CyNetwork, Set<T>>> getMatchedList(final List<CyNetwork> networks,
			final boolean isNode, Set<Set<CyNode>> matchedNodes) {
		if (networks == null)
			throw new java.lang.NullPointerException();

		if (networks.isEmpty()) {
			throw new java.lang.IllegalArgumentException("No merging network");
		}

		final List<Map<CyNetwork, Set<T>>> matchedList = new ArrayList<Map<CyNetwork, Set<T>>>();

		final int nNet = networks.size();

		for (int i = 0; i < nNet; i++) {

			final CyNetwork net1 = networks.get(i);
			
			final List<T> graphObjectList;
			if (isNode)
				graphObjectList = (List<T>) net1.getNodeList();
			else
				graphObjectList = (List<T>) net1.getEdgeList();

			for(T go1: graphObjectList) {
				if (interrupted)
					return null;
				
				// chech whether any nodes in the matchedNodeList match with
				// this node
				// if yes, add to the list, else add a new map to the list
				boolean matched = false;
				final int n = matchedList.size();
				int j = 0;
				for (; j < n; j++) {
					final Map<CyNetwork, Set<T>> matchedGO = matchedList.get(j);
					final Iterator<CyNetwork> itNet = matchedGO.keySet().iterator();
					while (itNet.hasNext()) {
						final CyNetwork net2 = itNet.next();
						// if (net1==net2) continue; // assume the same network
						// don't have nodes match to each other
						if (!withinNetworkMerge && net1 == net2)
							continue;

						final Set<T> gos2 = matchedGO.get(net2);
						if (gos2 != null) {
							CyIdentifiable go2 = gos2.iterator().next();
							if (isNode) { // NODE
								matched = matchNode(net1, (CyNode) go1, net2, (CyNode) go2);
								if (matched) {
									matchedNodes.add(getNodePair((CyNode) go1, (CyNode) go2));
								}
							} else {// EDGE
								matched = matchEdge(net1, net2, (CyEdge) go1, (CyEdge) go2, matchedNodes);
							}
							if (matched) {
								Set<T> gos1 = matchedGO.get(net1);
								if (gos1 == null) {
									gos1 = new HashSet<T>();
									matchedGO.put(net1, gos1);
								}
								gos1.add(go1);
								break;
							}
						}
					}
					if (matched) {
						break;
					}
				}
				if (!matched) { // no matched node found, add new map to the
								// list
					final Map<CyNetwork, Set<T>> matchedGO = new HashMap<CyNetwork, Set<T>>();
					Set<T> gos1 = new HashSet<T>();
					gos1.add(go1);
					matchedGO.put(net1, gos1);
					matchedList.add(matchedGO);
				}

			}

		}
		return matchedList;
	}

	/**
	 * Select nodes for merge according to different op
	 * 
	 * @param networks
	 *            Networks to be merged
	 * @param op
	 *            Operation
	 * @param size
	 *            Number of networks
	 * 
	 * @return list of matched nodes
	 */
	private <T extends CyIdentifiable> List<Map<CyNetwork, Set<T>>> selectMatchedGOList(
			final List<Map<CyNetwork, Set<T>>> matchedGOList, final Operation op, final List<CyNetwork> networks) {
		if (matchedGOList == null || op == null) {
			throw new java.lang.NullPointerException();
		}

		int nnet = networks.size();

		if (op == Operation.UNION) {
			return matchedGOList;
		} else if (op == Operation.INTERSECTION) {
			List<Map<CyNetwork, Set<T>>> list = new ArrayList<Map<CyNetwork, Set<T>>>();
			for (Map<CyNetwork, Set<T>> map : matchedGOList) {
				if (map.size() == nnet) {// if contained in all the networks
					list.add(map);
				}
			}

			return list;
		} else { // if (op==Operation.DIFFERENCE)
			List<Map<CyNetwork, Set<T>>> list = new ArrayList<Map<CyNetwork, Set<T>>>();
			if (nnet < 2)
				return list;

			CyNetwork net1 = networks.get(0);
			CyNetwork net2 = networks.get(1);

			for (Map<CyNetwork, Set<T>> map : matchedGOList) {
				if (map.containsKey(net1) && !map.containsKey(net2)) {
					list.add(map);
				}
			}

			return list;
		}
	}
}
