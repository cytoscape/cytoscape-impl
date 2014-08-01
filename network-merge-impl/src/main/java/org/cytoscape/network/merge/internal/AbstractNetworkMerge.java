package org.cytoscape.network.merge.internal;

/*
 * #%L
 * Cytoscape Merge Impl (network-merge-impl)
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

import java.util.ArrayList;
import java.util.Collection;
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
import org.cytoscape.model.CyRow;

import org.cytoscape.work.TaskMonitor;

/**
 * NetworkMerge implement
 */
public abstract class AbstractNetworkMerge implements NetworkMerge {
	
	protected boolean withinNetworkMerge = false;
	protected final TaskMonitor taskMonitor;
	//Maps a node and its position in the match list
	protected Map<CyNode,Integer> mapNodesIndex;
	//There are two different maps to differentiate directed and undirected edges
	//Each map does a first map based on type of interactions and then a second map that maps
	//a Long index made of combination of two integer indexes from the two nodes and the
	//index of that edge in the matched list
	//There is also a second set of maps for the case when the edges do not have a value 
	//in the interaction column. This would be a special case that needs to be considered too
	protected Map<String,Map<Long,Integer>> mapEdgeDirectedInteractions;
	protected Map<String,Map<Long,Integer>> mapEdgeInteractions;
	protected Map<Long,Integer> mapEdgeNoInteractions;
	protected Map<Long,Integer> mapEdgeDirectedNoInteractions;
	
	// For canceling task
	private volatile boolean interrupted;

	public AbstractNetworkMerge(final TaskMonitor taskMonitor) {
		this.taskMonitor = taskMonitor;
		interrupted = false;
		mapNodesIndex = new HashMap<CyNode,Integer>();
		mapEdgeDirectedInteractions = new HashMap<String,Map<Long,Integer>>();
		mapEdgeInteractions = new HashMap<String,Map<Long,Integer>>();
		mapEdgeNoInteractions = new HashMap<Long,Integer>();
		mapEdgeDirectedNoInteractions = new HashMap<Long,Integer>();
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
	 * Check whether an edge match the other edges already considered, if so it will 
	 * return the position in the match list
	 * 
	 * @param network1 The source network of the edge to evaluate
	 * @param e1 The edge to check if it has a match
	 * @param position The position in the match list that the new edge belong if no match is found
	 * 
	 * @return the index in the match list where this edge has found a match or -1 if no match found
	 */
	protected int matchEdge( CyNetwork network1, CyEdge e1, int position) {
		
		int index = -1;
		long id1, id2 = 0;
		Map<Long,Integer> mapNodesEdges = null;
		Map<Long,Integer> mapNodesDirectedEdges = null;
		if (e1 == null ) {
			throw new NullPointerException();
		}
		
		String i1 = network1.getRow(e1).get("interaction", String.class);
		
		
		CyNode source = e1.getSource();
		CyNode target = e1.getTarget();
		
		if (source == null || target == null ) {
			throw new NullPointerException();
		}
		
		int iSource = mapNodesIndex.get(source);
		
		int iTarget = mapNodesIndex.get(target);
		
		if (e1.isDirected())
		{
			if(i1 == null)
				mapNodesDirectedEdges = mapEdgeDirectedNoInteractions;
			else
				mapNodesDirectedEdges = mapEdgeDirectedInteractions.get(i1);
			id1 = getUniqueIdNumber(iSource, iTarget);
			if( mapNodesDirectedEdges != null)
			{	
				//System.out.println("same interaction directed edge: " + i1);
				if(mapNodesDirectedEdges.get(id1) != null)
					index = mapNodesDirectedEdges.get(id1);
			}
				
		}
		else
		{
			if(i1 == null)
				mapNodesEdges = mapEdgeNoInteractions;
			else
				mapNodesEdges = mapEdgeInteractions.get(i1);
			id1 = getUniqueIdNumber(iSource, iTarget);
			id2 = getUniqueIdNumber(iTarget,iSource);
			if(mapNodesEdges != null)
			{
				//System.out.println("same interaction edge: " + i1);
				if(mapNodesEdges.get(id1) != null && mapNodesEdges.get(id2) != null && mapNodesEdges.get(id1) == mapNodesEdges.get(id2))
					index = mapNodesEdges.get(id1);
			}
		}
		
		
		if(index == -1)
		{			
			if (e1.isDirected())
			{
				if( mapNodesDirectedEdges != null)
					mapNodesDirectedEdges.put(id1, position);
				else
				{
					mapNodesDirectedEdges = new HashMap<Long,Integer>();
					mapNodesDirectedEdges.put(id1, position);
					mapEdgeDirectedInteractions.put(i1, mapNodesDirectedEdges);
				}
			}
			else
			{
				if( mapNodesEdges != null)
				{
					mapNodesEdges.put(id1, position);
					mapNodesEdges.put(id2, position);
				}
				else
				{
					mapNodesEdges = new HashMap<Long,Integer>();
					mapNodesEdges.put(id1, position);
					mapNodesEdges.put(id2, position);
					mapEdgeInteractions.put(i1, mapNodesEdges);
				}
				
			}
		}
		
		return index;
	}
	
	private long getUniqueIdNumber(int a, int b)
	{
		long id;
		if(a > b)
			id = b | ((long) a << 32);
		else
			id = a | ((long) b << 32);
		
		return id;
	}

	protected abstract void proprocess(CyNetwork toNetwork);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CyNetwork mergeNetwork(final CyNetwork mergedNetwork, final List<CyNetwork> fromNetworks, final Operation op) {
		// Null checks for required fields...
		if (mergedNetwork == null || fromNetworks == null || op == null) {
			throw new NullPointerException("Required parameter is missing.");
		}
		if (fromNetworks.isEmpty()) {
			throw new IllegalArgumentException("No source networks!");
		}

		//long startTime = System.currentTimeMillis();
		proprocess(mergedNetwork);
		
		mapNodesIndex.clear();
		mapEdgeDirectedInteractions.clear();
		mapEdgeInteractions.clear();
		mapEdgeDirectedNoInteractions.clear();
		mapEdgeNoInteractions.clear();
		// get node matching list
		List<Map<CyNetwork, Set<CyNode>>> matchedNodeList = getMatchedList(fromNetworks, true);
		
		// Check cancel status
		if(interrupted) {
			return null;
		}
		
		matchedNodeList = selectMatchedGOList(matchedNodeList, op, fromNetworks);
		final Map<CyNode, CyNode> mapNN = new HashMap<CyNode, CyNode>();

		// merge nodes in the list
		taskMonitor.setStatusMessage("Merging nodes...");
		final long nNode = matchedNodeList.size();
		for (int i = 0; i < nNode; i++) {
			if (interrupted)
				return null;
			
			taskMonitor.setProgress(((double)(i + 1)/ nNode)*0.5d);

			final Map<CyNetwork, Set<CyNode>> mapNetNode = matchedNodeList.get(i);
			if (mapNetNode == null || mapNetNode.isEmpty())
				continue;

			CyNode node = mergedNetwork.addNode();
			mergeNode(mapNetNode, node, mergedNetwork);

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
		List<Map<CyNetwork, Set<CyEdge>>> matchedEdgeList = getMatchedList(fromNetworks, false);

		// Check cancel status
		if(interrupted) {
			return null;
		}
		matchedEdgeList = selectMatchedGOList(matchedEdgeList, op, fromNetworks);

		// merge edges
		final double nEdge = matchedEdgeList.size();
		
		// Only for difference
		final Map<String, CyNode> differenceNodeMap = new HashMap<String, CyNode>();
		
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
				throw new IllegalStateException("Null or empty edge set");
			}

			final CyEdge originalEdge = edgeSet.iterator().next();
			CyNode source = mapNN.get(originalEdge.getSource());
			CyNode target = mapNN.get(originalEdge.getTarget());
			
			if(op == Operation.DIFFERENCE) {
				// For difference, need to create nodes if necessary.
				
				if(source == null) {
					CyNode originalSource = originalEdge.getSource();
					String name = findName(fromNetworks, originalSource);
					source = differenceNodeMap.get(name);
					if(source == null) {
						source = mergedNetwork.addNode();
						differenceNodeMap.put(name, source);
					}
					mergedNetwork.getRow(source).set(CyNetwork.NAME, name);
				}
				if(target == null) {
					CyNode originalTarget = originalEdge.getTarget();
					String name = findName(fromNetworks, originalTarget);
					target = differenceNodeMap.get(name);
					if(target == null) {
						target = mergedNetwork.addNode();
						differenceNodeMap.put(name, target);
					}
					mergedNetwork.getRow(target).set(CyNetwork.NAME, name);
				}
			} else if (source == null || target == null) { // some of the node may be
													// exluded when intersection
													// or difference
				continue;
			}

			final boolean directed = originalEdge.isDirected();

			CyEdge edge = mergedNetwork.addEdge(source, target, directed);
			mergeEdge(mapNetEdge, edge, mergedNetwork);
		}
		//System.out.println("Run time: " + (System.currentTimeMillis() - startTime));

		return mergedNetwork;
	}
	
	private final String findName(Collection<CyNetwork> fromNetworks, CyNode originalNode) {
		String name = null;
		for(CyNetwork originalNetwork: fromNetworks) {
			if(originalNetwork.containsNode(originalNode) == false)
				continue;
			
			CyRow row = originalNetwork.getRow(originalNode);
			if(row != null) {
				name = row.get(CyNetwork.NAME, String.class);
				break;
			}
		}
		return name;
	}

	/**
	 * Get a list of matched nodes/edges
	 * 
	 * @param networks
	 *            Networks to be merged
	 * @param isNode
	 *            true if for node
	 * 
	 * 
	 * @return list of map from network to node/edge
	 */
	private <T extends CyIdentifiable> List<Map<CyNetwork, Set<T>>> getMatchedList(final List<CyNetwork> networks,
			final boolean isNode) {
		int index = 0;
		if (networks == null)
			throw new NullPointerException();

		if (networks.isEmpty()) {
			throw new IllegalArgumentException("No merging network");
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
				// this node if yes, add to the list, else add a new map to the list
				boolean matched = false;
				final int n = matchedList.size();
				//The search for a match has been split for nodes and edges. Edges don't need to go through the loop
				//since they can take advantage of node's information in the previous found node match list
				if(isNode)
				{
					
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
							if (gos2 != null) 
							{
								CyIdentifiable go2 = gos2.iterator().next();
								
								matched = matchNode(net1, (CyNode) go1, net2, (CyNode) go2);
								
								if (matched) {
									index = j;
									mapNodesIndex.put((CyNode) go1, index);
									break;
								}
							}
						}
						if (matched) {
							break;
						}
					}
				}
				else
				{
					index = matchEdge(net1,(CyEdge) go1,n);
					if(index >= 0)
					{
						//check if the edge belongs to the same network
						//if so, the match is not valid
						if(matchedList.get(index).containsKey(net1) && matchedList.get(index).keySet().size() == 1 && !withinNetworkMerge)
							matched = false;
						else
							matched = true;
					}
					else
						matched = false;
				}
				if (!matched) {
					// no matched node/edge found, add new map to the list
					final Map<CyNetwork, Set<T>> matchedGO = new HashMap<CyNetwork, Set<T>>();
					Set<T> gos1 = new HashSet<T>();
					gos1.add(go1);
					matchedGO.put(net1, gos1);
					if(isNode)
						mapNodesIndex.put((CyNode) go1, n);
					matchedList.add(matchedGO);
				}
				else
				{
					Set<T> gos1 = matchedList.get(index).get(net1);
					if (gos1 == null) {
						gos1 = new HashSet<T>();
						matchedList.get(index).put(net1, gos1);
					}
					gos1.add(go1);
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
		if (matchedGOList == null || op == null)
			throw new NullPointerException();

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
		} else { 
			// For Operation.DIFFERENCE
			final List<Map<CyNetwork, Set<T>>> list = new ArrayList<Map<CyNetwork, Set<T>>>();
			if (nnet < 2)
				return list;

			final CyNetwork net1 = networks.get(0);
			final CyNetwork net2 = networks.get(1);
			for (Map<CyNetwork, Set<T>> map : matchedGOList) {
				if ((map.containsKey(net1) && !map.containsKey(net2)) ||
						(!map.containsKey(net1) && map.containsKey(net2)))
					list.add(map);
			}

			return list;
		}
	}
}
