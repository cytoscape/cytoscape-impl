package org.cytoscape.io.internal.read.nnf;

import java.util.ArrayList;
import java.util.Collection;

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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;

/**
 * Parser for NNF files.
 * 
 * @author kono, ruschein
 */
public class NNFParser {
	
	private static final String HAS_NESTED_NETWORK_ATTRIBUTE = "has_nested_network";
	
	// For performance, these fields will be reused.
	private String[] parts;
	private int length;

	// List of root network plus all nested networks.
	private final Set<CyNetwork> networks;
	private CyNetwork overviewwork;
	
	/** Map the original network name (as parsed from the file) to a network created by this parser instance. */
	private Map<String, CyNetwork> networksByName;
	
	private final CyRootNetwork rootNetwork;
	private final Map<Object, CyNode> nMap;

	private final CyNetworkNaming namingUtil;
	
	public NNFParser(final CyRootNetwork rootNetwork, final CyNetworkFactory cyNetworkFactory, Map<Object, CyNode> nMap,
			final CyServiceRegistrar serviceRegistrar) {
		namingUtil = serviceRegistrar.getService(CyNetworkNaming.class);
		
		this.nMap = nMap;
		networksByName = new HashMap<>();
		networks = new LinkedHashSet<>();
		
		if (rootNetwork != null) {
			this.rootNetwork = rootNetwork;
			overviewwork = this.rootNetwork.addSubNetwork();
		} else {
			overviewwork = cyNetworkFactory.createNetwork();
			this.rootNetwork = ((CySubNetwork) overviewwork).getRootNetwork();
		}
		
		networks.add(overviewwork);
	}

	private CyNetwork getNetworkByName(final String name) {
		return networksByName.get(name);
	}
	
	/**
	 * Parse an entry/line in an NNF file.
	 * 
	 * @param line
	 */
	public boolean parse(final String line) {
		// Split with white space chars
		parts = splitLine(line);
		length = parts.length;

		final String originalName = parts[0];
		CyNetwork network = getNetworkByName(originalName);
		
		if (network == null) {
			// Create network without view.  View will be created later.
			network = rootNetwork.addSubNetwork();
			
			final String actualName = namingUtil.getSuggestedNetworkTitle(originalName);
			network.getRow(network).set(CyNetwork.NAME, actualName);

			networksByName.put(originalName, network);
			networks.add(network);

			// Attempt to nest network within the node with the same name			
			CyNode parent = getNodeByName(originalName);
			
			if (parent != null)
				setNestedNetwork(network, parent, network);
			
			// is the node exist in the overview network
			parent = getNodefromOverview(originalName);
			
			if (parent != null)
				setNestedNetwork(network, parent, network);
		}

		if (length == 2) {
			CyNode node;
			
			if (this.nMap.get(parts[1]) == null){
				node = network.addNode();
				this.nMap.put(parts[1], this.rootNetwork.getNode(node.getSUID()));
			} else {
				node = this.nMap.get(parts[1]);
				CySubNetwork subnet = (CySubNetwork) network;
				subnet.addNode(node);
			}
			
			network.getRow(node).set(CyNetwork.NAME, parts[1]);
			final CyNetwork nestedNetwork = getNetworkByName(parts[1]);
			
			if (nestedNetwork != null) {
				setNestedNetwork(network, node, nestedNetwork);
			}
		} else if (length == 4) {
			CyNode source = null;
			// Check if the source node already existed in the network			
			Collection<CyRow> matchingRows = network.getDefaultNodeTable().getMatchingRows(CyNetwork.NAME, parts[1]);
			if (!matchingRows.isEmpty()){
				// source node already existed, get it
				CyRow row = matchingRows.iterator().next();
				Long suid = row.get("SUID", Long.class);
				if (suid != null){
					source = network.getNode(suid);
				}				
			}
			else {
				// source node does not existed yet, create one
				if (this.nMap.get(parts[1]) == null){
					source = network.addNode(); //Cytoscape.getCyNode(parts[1], true);
					CyNode newNode = this.rootNetwork.getNode(source.getSUID());
					this.nMap.put(parts[1], newNode);
				}
				else {
					CyNode newNode = this.nMap.get(parts[1]);
					CySubNetwork subNet = (CySubNetwork) network;
					subNet.addNode(newNode);
					source = network.getNode(newNode.getSUID());
				}
				
				network.getRow(source).set(CyNetwork.NAME, parts[1]);
			}
			
			CyNetwork nestedNetwork = networksByName.get(parts[1]);
			if (nestedNetwork != null) {
				setNestedNetwork(network, source, nestedNetwork);
			}

			CyNode target = null;
			
			// Check if the target already existed in the network
			Collection<CyRow> matchingRows2 = network.getDefaultNodeTable().getMatchingRows(CyNetwork.NAME, parts[3]);
			if (!matchingRows2.isEmpty()){
				// source node already existed, get it
				CyRow row = matchingRows2.iterator().next();

				Long suid = row.get("SUID", Long.class);
				if (suid != null){
					target = network.getNode(suid);
				}				
			}
			else {
				// source node does not existed yet, create one
				
				if (this.nMap.get(parts[3]) == null){
					target = network.addNode(); 
					CyNode newNode = this.rootNetwork.getNode(target.getSUID());
					this.nMap.put(parts[3], newNode);
				}
				else {
					CyNode newNode = this.nMap.get(parts[3]);
					CySubNetwork subNet = (CySubNetwork) network;
					subNet.addNode(newNode);
					target = network.getNode(newNode.getSUID());				
				}				
				//target = network.addNode();
				
				network.getRow(target).set(CyNetwork.NAME, parts[3]);
			}

			//
			nestedNetwork = networksByName.get(parts[3]);
			if (nestedNetwork != null) {
				setNestedNetwork(network, target, nestedNetwork);
			}

			CyEdge newEdge = network.addEdge(source, target, true);
			network.getRow(newEdge).set(CyEdge.INTERACTION, parts[2]);
			network.getRow(newEdge).set(CyNetwork.NAME, parts[1]+ " ("+parts[2]+") "+parts[3]);

		} else {
			// Invalid number of columns.
			return false;
		}

		return true;
	}
	
	private void setNestedNetwork(CyNetwork sourceNetwork, CyNode node, CyNetwork targetNetwork) {
		// TODO: We should consider exposing a nested network API so we don't
		// have to do this everywhere we establish this link.
		node.setNetworkPointer(targetNetwork);
		
		CyTable nodeTable = sourceNetwork.getDefaultNodeTable();
		boolean attributeExists = nodeTable.getColumn(HAS_NESTED_NETWORK_ATTRIBUTE) != null;
		
		if (targetNetwork == null && attributeExists) {
			nodeTable.getRow(node.getSUID()).set(HAS_NESTED_NETWORK_ATTRIBUTE, false);
		} else if (targetNetwork != null) {
			if (!attributeExists) {
				nodeTable.createColumn(HAS_NESTED_NETWORK_ATTRIBUTE, Boolean.class, false);
			}
			CyRow row = nodeTable.getRow(node.getSUID());
			row.set(HAS_NESTED_NETWORK_ATTRIBUTE, true);
		}
	}

	private CyNode getNodeByName(String nodeName){
		CyNode retNode = null;
		Iterator<String> it = networksByName.keySet().iterator();
		
		while (it.hasNext()){
			CyNetwork network = networksByName.get(it.next());
			
			Collection<CyRow> matchingRows = network.getDefaultNodeTable().getMatchingRows(CyNetwork.NAME, nodeName);
			if (matchingRows.isEmpty()){
				continue;
			}
			else {
				CyRow row = matchingRows.iterator().next();

				Long suid = row.get("SUID", Long.class);
				if (suid != null){
					return network.getNode(suid);					
				}
			}
		}
		
		return retNode;
	}

	private CyNode getNodefromOverview(String nodeName) {
		Collection<CyRow> matchingRows = this.overviewwork.getDefaultNodeTable().getMatchingRows(CyNetwork.NAME, nodeName);
		if (matchingRows.isEmpty()){
			return null;
		}
		
		CyRow row = matchingRows.iterator().next();
		Long suid = row.get("SUID", Long.class);
		return this.overviewwork.getNode(suid);
	}
	
	public void setOverViewnetworkName(final String desiredName){
		final String actualName = namingUtil.getSuggestedNetworkTitle(desiredName);
		overviewwork.getRow(overviewwork).set(CyNetwork.NAME, actualName);
		networksByName.put(desiredName, overviewwork);
	}
	
	static public String[] splitLine(final String line) {
		final List<String> parts = new ArrayList<String>();
		boolean escaped = false;
		StringBuilder part = null;
		for (int i = 0; i < line.length(); ++i) {
			final char ch = line.charAt(i);
			if (escaped) {
				escaped = false;
				if (part == null)
					part = new StringBuilder();
				part.append(ch);
			} else if (ch == '\\')
				escaped = true;
			else if (ch == ' ' || ch == '\t') {
				if (part != null) {
					parts.add(part.toString());
					part = null;
				}
			} else {
				if (part == null)
					part = new StringBuilder();
				part.append(ch);
			}
		}

		if (part != null)
			parts.add(part.toString());

		final String[] array = new String[parts.size()];
		return parts.toArray(array);
	}

	protected Set<CyNetwork> getNetworks() {
		return networks;
	}
}
