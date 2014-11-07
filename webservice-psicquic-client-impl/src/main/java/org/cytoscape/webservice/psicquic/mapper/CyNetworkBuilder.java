package org.cytoscape.webservice.psicquic.mapper;

/*
 * #%L
 * Cytoscape PSIQUIC Web Service Impl (webservice-psicquic-client-impl)
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

import static org.cytoscape.webservice.psicquic.mapper.InteractionClusterMapper.TAXNOMY;
import static org.cytoscape.webservice.psicquic.mapper.InteractionClusterMapper.TAXNOMY_NAME;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.events.UpdateNetworkPresentationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.enfin.mi.cluster.InteractionCluster;

// TODO Make this smarter
public class CyNetworkBuilder {

	private static final Logger logger = LoggerFactory.getLogger(CyNetworkBuilder.class);

	// Required number of columns in MITAB 2.5
	private static final int MINIMUM_COLUMN_COUNT = 15;
	

	// Most widly used ID sets.  Pick any of these if available.
	private static final String UNIPROT_AC = "uniprotkb_accession";
	private static final String NCBI_GENE = "ncbi_gene_id";
	private static final String CHEBI = "chebi_id";
	private static final String REFSEQ= "refseq_id";
	

	private static final Pattern SPLITTER_TAB = Pattern.compile("\t");
	
	private static final Pattern SPLITTER = Pattern.compile("\\|");
	private static final Pattern SPLITTER_NAME_SPACE = Pattern.compile("\\:");
	private static final Pattern NCBI = Pattern.compile("^d+$");
	private static final Pattern UNIPROT_PATTERN = 
			Pattern.compile("^([A-N,R-Z][0-9][A-Z][A-Z, 0-9][A-Z, 0-9][0-9])|([O,P,Q][0-9][A-Z, 0-9][A-Z, 0-9][A-Z, 0-9][0-9])(.d+)?$");

	private final InteractionClusterMapper mapper;

	final String fisrtSeparator = "\t";
	final String secondSeparator = ",";

	private final CyNetworkFactory networkFactory;

	volatile boolean cancel = false;

	private Map<String, CyNode> nodeMap;

	public CyNetworkBuilder(final CyNetworkFactory networkFactory) {
		this.networkFactory = networkFactory;
		mapper = new InteractionClusterMapper();
		mapper.ensureInitialized();
	}

	public CyNetwork buildNetwork(final InteractionCluster iC) throws IOException {
		CyNetwork network = networkFactory.createNetwork();
		process(iC, network, null, null);
		return network;
	}

	/**
	 * Simple Build a new network from list of interactions.
	 * 
	 * @param interactions
	 * @return
	 * @throws IOException
	 */
	public CyNetwork buildNetwork(final BufferedReader reader, final String networkTitle) throws IOException {

		// Create empty network even if there is no result.
		final CyNetwork network = networkFactory.createNetwork();
		network.getDefaultNetworkTable().createColumn("source", String.class, true);
		network.getRow(network).set("source", networkTitle);
		network.getRow(network).set(CyNetwork.NAME, networkTitle);

		prepareColumns(network);

		final Map<String, CyNode> nodes = new HashMap<String, CyNode>();

		String line;
		while ((line = reader.readLine()) != null) {
			if (cancel) {
				logger.warn("Network bulilder interrupted.");
				network.getRow(network).set(CyNetwork.NAME, "<Incomplete!> " + networkTitle);
				break;
			}

			// Actual entries for an interaction.
			final String[] parts = SPLITTER_TAB.split(line);

			// Skip invalid lines
			if (parts.length < MINIMUM_COLUMN_COUNT) {
				continue;
			}
			
			
			// Find primary ID
			final String[] sourceIds = SPLITTER.split(parts[0]);
			final String[] targetIds = SPLITTER.split(parts[1]);
		
			String sourceFirst = sourceIds[0];
			String targetFirst = targetIds[0];
			// Check self-edge
			if (targetFirst.equals("-")) {
				// This is for self-interaction
				targetIds[0] = sourceFirst;
			} else if (sourceFirst.equals("-")) {
				sourceIds[0] = targetFirst;
			}

			// Priority: Uniprot, NCBI, chebi, and others.
			final String[] sourceID = getID(sourceIds);
			final String[] targetID = getID(targetIds);
			final CyNode sourceNode = addNode(nodes, sourceID[1], network);
			final CyNode targetNode = addNode(nodes, targetID[1], network);
			if(network.getDefaultNodeTable().getColumn(sourceID[0]) == null) {
				network.getDefaultNodeTable().createColumn(sourceID[0], String.class, false);
			}
			network.getRow(sourceNode).set(sourceID[0], sourceID[1]);

			if(network.getDefaultNodeTable().getColumn(targetID[0]) == null) {
				network.getDefaultNodeTable().createColumn(targetID[0], String.class, false);
			}
			network.getRow(targetNode).set(targetID[0], targetID[1]);
			
			mapper.mapNodeColumn(parts, network.getRow(sourceNode), network.getRow(targetNode));

			final CyEdge newEdge = network.addEdge(sourceNode, targetNode, true);
			mapper.mapEdgeColumn(parts, network.getRow(newEdge), newEdge, sourceID[1], targetID[1]);
		}

		reader.close();
		return network;
	}
	
	private final String[] getID(final String[] nodeIDs) {
		final String[] firstParts= mapper.parseValues(nodeIDs[0]);
		final Map<String, String> nodes = new HashMap<String, String>();
		for(final String entry: nodeIDs) {
			final String[] nodeParts= mapper.parseValues(entry);
			nodes.put(nodeParts[0], nodeParts[1]);
		}
		
		final String[] primaryID = new String[2];
		if(nodes.get("uniprotkb") != null) {
			primaryID[0] = UNIPROT_AC;
			primaryID[1] = nodes.get("uniprotkb");
		} else if(nodes.get("entrez gene/locuslink") != null) {
			primaryID[0] = NCBI_GENE;
			primaryID[1] = nodes.get("entrez gene/locuslink");
		} else if(nodes.get("chebi") != null) {
			primaryID[0] = CHEBI;
			primaryID[1] = nodes.get("chebi");
		} else {
			primaryID[0] = firstParts[0] + "_id";
			primaryID[1] = firstParts[1]; 
		}
		return primaryID;
	}

	private final CyNode addNode(final Map<String, CyNode> nodes, final String id, final CyNetwork network) {
		CyNode node = nodes.get(id);
		if (node == null) {
			node = network.addNode();
			network.getRow(node).set(CyNetwork.NAME, id);
			nodes.put(id, node);
		}
		return node;
	}

	/**
	 * Add new edges to the existing network.
	 * 
	 * @param iC
	 * @param networkView
	 * @param hubNode
	 * @return
	 */
	public void addToNetwork(final InteractionCluster iC, CyNetworkView networkView, final View<CyNode> hubNode) {
		final CyNetwork network = networkView.getModel();

		// Hub node to be expanded.
		final CyNode hub = hubNode.getModel();
		String hubName = network.getRow(hub).get(CyNetwork.NAME, String.class);
		CyTable hubNodeTable = network.getDefaultNodeTable();
		if( hubNodeTable.getColumn("identifier") != null )
			hubName = network.getRow(hub).get("identifier",String.class);
		mapper.ensureInitialized();
		nodeMap = new HashMap<String, CyNode>();
		nodeMap.put(hubName, hub);
		network.getRow(hub).set(CyNetwork.SELECTED, true);

		for (final CyNode existingNode : network.getNodeList())
			nodeMap.put(network.getRow(existingNode).get(CyNetwork.NAME, String.class), existingNode);

		// Merged interactions. TODO: Interactive UI for merge?
		final Map<Integer, EncoreInteraction> interactions = iC.getInteractionMapping();

		prepareColumns(network);

		for (final Integer interactionKey : interactions.keySet()) {
			if (cancel) {
				logger.warn("Network bulilder interrupted.");
			}
			final EncoreInteraction interaction = interactions.get(interactionKey);
			final String source = interaction.getInteractorA();
			final String target = interaction.getInteractorB();

			// Check this is an interaction from the query hub node.
			if(source.equals(hubName) == false && target.equals(hubName) == false) {
				// Not the edge from the query node.
				continue;
			}
			
			final String newNodeName;
			if(source.equals(hubName)) {
				newNodeName = target;
			} else {
				newNodeName = source;
			}

			CyNode newNode = nodeMap.get(newNodeName);
			if (newNode == null) {
				newNode= network.addNode();
				network.getRow(newNode).set(CyNetwork.NAME, newNodeName);
				network.getRow(newNode).set(CyNetwork.SELECTED, true);
				nodeMap.put(newNodeName, newNode);
			}
			mapper.mapNodeColumn(interaction, network.getRow(newNode), null);

			final CyEdge newEdge = network.addEdge(hub, newNode, true);
			mapper.mapEdgeColumn(interaction, network.getRow(newEdge));
		}
	}


	private final Map<String, CyNode> process(final InteractionCluster iC, CyNetwork network, CyNetworkView netView,
			final View<CyNode> hubNode) {
		mapper.ensureInitialized();
		nodeMap = new HashMap<String, CyNode>();
		if (hubNode != null) {
			nodeMap.put(network.getRow(hubNode.getModel()).get(CyNetwork.NAME, String.class), hubNode.getModel());
			if (netView != null)
				network.getRow(hubNode.getModel()).set(CyNetwork.SELECTED, true);
		}

		if (network.getNodeCount() != 0) {
			for (CyNode existingNode : network.getNodeList())
				nodeMap.put(network.getRow(existingNode).get(CyNetwork.NAME, String.class), existingNode);
		}

		final Map<Integer, EncoreInteraction> interactions = iC.getInteractionMapping();

		prepareColumns(network);

		for (final Integer interactionKey : interactions.keySet()) {
			if (cancel) {
				logger.warn("Network bulilder interrupted.");
				network = null;
			}
			final EncoreInteraction interaction = interactions.get(interactionKey);

			final String source = interaction.getInteractorA();
			CyNode sourceNode = nodeMap.get(source);
			if (sourceNode == null) {
				sourceNode = network.addNode();
				network.getRow(sourceNode).set(CyNetwork.NAME, source);
				if (netView != null)
					network.getRow(sourceNode).set(CyNetwork.SELECTED, true);
				nodeMap.put(source, sourceNode);
			}
			final String target = interaction.getInteractorB();
			CyNode targetNode = nodeMap.get(target);
			if (targetNode == null) {
				targetNode = network.addNode();
				network.getRow(targetNode).set(CyNetwork.NAME, target);
				if (netView != null)
					network.getRow(targetNode).set(CyNetwork.SELECTED, true);
				nodeMap.put(target, targetNode);
			}
			mapper.mapNodeColumn(interaction, network.getRow(sourceNode), network.getRow(targetNode));

			final CyEdge newEdge = network.addEdge(sourceNode, targetNode, true);
			mapper.mapEdgeColumn(interaction, network.getRow(newEdge));

		}
		logger.info("Import Done: " + network.getSUID());
		return nodeMap;
	}


	/**
	 * Create minimum set of columns supported by MITAB 2.5.
	 * 
	 * @param network
	 */
	private final void prepareColumns(final CyNetwork network) {
		final CyTable nodeTable = network.getDefaultNodeTable();
		final CyTable edgeTable = network.getDefaultEdgeTable();
		if (nodeTable.getColumn(TAXNOMY) == null)
			nodeTable.createColumn(TAXNOMY, String.class, false);
		if (nodeTable.getColumn(TAXNOMY_NAME) == null)
			nodeTable.createColumn(TAXNOMY_NAME, String.class, false);

		// Prepare label column
		if (nodeTable.getColumn(InteractionClusterMapper.PREDICTED_GENE_NAME) == null)
			nodeTable.createColumn(InteractionClusterMapper.PREDICTED_GENE_NAME, String.class, false);

		// Prepare edge column
		if (edgeTable.getColumn(InteractionClusterMapper.AUTHOR) == null)
			edgeTable.createListColumn(InteractionClusterMapper.AUTHOR, String.class, false);
		if (edgeTable.getColumn(InteractionClusterMapper.PRIMARY_INTERACTION_TYPE) == null)
			edgeTable.createColumn(InteractionClusterMapper.PRIMARY_INTERACTION_TYPE, String.class, false);
		if (edgeTable.getColumn(InteractionClusterMapper.INTERACTION_TYPE) == null)
			edgeTable.createListColumn(InteractionClusterMapper.INTERACTION_TYPE, String.class, false);
		if (edgeTable.getColumn(InteractionClusterMapper.PUB_DB) == null)
			edgeTable.createListColumn(InteractionClusterMapper.PUB_DB, String.class, false);
		if (edgeTable.getColumn(InteractionClusterMapper.PUB_ID) == null)
			edgeTable.createListColumn(InteractionClusterMapper.PUB_ID, String.class, false);
		if (edgeTable.getColumn(InteractionClusterMapper.DETECTION_METHOD_ID) == null)
			edgeTable.createListColumn(InteractionClusterMapper.DETECTION_METHOD_ID, String.class, false);
		if (edgeTable.getColumn(InteractionClusterMapper.DETECTION_METHOD_NAME) == null)
			edgeTable.createListColumn(InteractionClusterMapper.DETECTION_METHOD_NAME, String.class, false);
		if (edgeTable.getColumn(InteractionClusterMapper.SOURCE_DB) == null)
			edgeTable.createColumn(InteractionClusterMapper.SOURCE_DB, String.class, false);
	}

	public void cancel() {
		this.cancel = true;
	}
}
