package org.cytoscape.io.internal.read.nnf;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import java.util.Collection;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import java.util.Iterator;

/**
 * Parser for NNF files.
 * 
 * @author kono, ruschein
 */
public class NNFParser {
	// For performance, these fields will be reused.
	private String[] parts;
	private int length;

	// List of root network plus all nested networks.
	private final List<CyNetwork> networks;
	private CyNetwork overviewwork;
	// Hash map from title to actual network
	private Map<String, CyNetwork> networkMap;
	private final CyRootNetwork rootNetwork;
	private final  Map<Object, CyNode> nMap;
	
	public NNFParser(CyRootNetwork rootNetwork,  Map<Object, CyNode> nMap) {
		this.rootNetwork = rootNetwork;
		this.nMap = nMap;
		
		networkMap = new HashMap<String, CyNetwork>();
		networks = new ArrayList<CyNetwork>();	
		this.overviewwork = this.rootNetwork.addSubNetwork(); //this.cyNetworkFactory.createNetwork();
	}


	/** Returns the first network with title "networkTitle" or null, if there is no network w/ this title.
	 */
	private CyNetwork getNetworkByTitle(final String networkTitle) {
		Iterator<CySubNetwork> it = this.rootNetwork.getSubNetworkList().iterator();
		
		while (it.hasNext()){
			CySubNetwork subnet = it.next();
			String title = subnet.getRow(subnet).get(CyNetwork.NAME, String.class);
			if (title != null && title.equals(networkTitle))
				return subnet;
		}
		return null;		
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

		CyNetwork network = networkMap.get(parts[0]);
		
		if (this.overviewwork.getRow(this.overviewwork).get(CyNetwork.NAME, String.class).equalsIgnoreCase(parts[0])){
			network = this.overviewwork;
		}
		
		if (network == null) {
			// Reuse existing networks, if possible:
			network = getNetworkByTitle(parts[0]);
			if (network == null) {
				// Create network without view.  View will be created later.
				network = this.rootNetwork.addSubNetwork(); 
				network.getRow(network).set(CyNetwork.NAME, parts[0]);
			}

			networkMap.put(parts[0], network);
			networks.add(network);

			// Attempt to nest network within the node with the same name			
			CyNode parent = getNodeByName(networkMap, parts[0]);
			if (parent != null) {
				parent.setNetworkPointer(network);
			}
			
			// is the node exist in the overview network
			parent = getNodefromOverview(parts[0]);
			if (parent != null) {
				parent.setNetworkPointer(network);
			}
		}

		if (length == 2) {
			
			CyNode node;
			if (this.nMap.get(parts[1]) == null){
				node = network.addNode();
				this.nMap.put(parts[1], this.rootNetwork.getNode(node.getSUID()));
			}
			else {
				node = this.nMap.get(parts[1]);
				CySubNetwork subnet = (CySubNetwork) network;
				subnet.addNode(node);
			}
			
			network.getRow(node).set(CyNetwork.NAME, parts[1]);
						
			final CyNetwork nestedNetwork = networkMap.get(parts[1]);
			if (nestedNetwork != null) {
				node.setNetworkPointer(nestedNetwork);
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
			
			CyNetwork nestedNetwork = networkMap.get(parts[1]);
			if (nestedNetwork != null) {
				source.setNetworkPointer(nestedNetwork);
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
			nestedNetwork = networkMap.get(parts[3]);
			if (nestedNetwork != null) {
				target.setNetworkPointer(nestedNetwork);
			}

			CyEdge newEdge = network.addEdge(source, target, true);
			network.getRow(newEdge).set("interaction", parts[2]);
			network.getRow(newEdge).set(CyNetwork.NAME, parts[1]+ " ("+parts[2]+") "+parts[3]);

		} else {
			// Invalid number of columns.
			return false;
		}

		return true;
	}
	
	
	private CyNode getNodeByName(Map<String, CyNetwork> networkMap, String nodeName){
		
		CyNode retNode = null;
		
		Iterator<String> it = networkMap.keySet().iterator();
		while (it.hasNext()){
			CyNetwork network = networkMap.get(it.next());
			
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
	
	
	public void setOverViewnetworkName(String overviewNetworkName){
		this.overviewwork.getRow(this.overviewwork).set(CyNetwork.NAME, overviewNetworkName);
		this.networks.add(this.overviewwork);
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

	protected List<CyNetwork> getNetworks() {
		return networks;
	}
}
