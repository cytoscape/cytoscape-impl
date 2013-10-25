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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.enfin.mi.cluster.InteractionCluster;

// TODO Make this smarter
public class CyNetworkBuilder {

	private static final Logger logger = LoggerFactory.getLogger(CyNetworkBuilder.class);

	// Required number of columns in MITAB 2.5
	private static final int MINIMUM_COLUMN_COUNT = 15;

	private static final Pattern SPLITTER = Pattern.compile("\\|");
	private static final Pattern SPLITTER_NAME_SPACE = Pattern.compile("\\:");

	private final InteractionClusterMapper mapper;

	final String fisrtSeparator = "\t";
	final String secondSeparator = ",";

	private final CyNetworkFactory networkFactory;

	volatile boolean cancel = false;

	private Map<String, CyNode> nodeMap;

	public CyNetworkBuilder(final CyNetworkFactory networkFactory) {
		this.networkFactory = networkFactory;
		mapper = new InteractionClusterMapper();
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
		final Pattern pattern = Pattern.compile("\t");

		String line;
		int i = 0;
		while ((line = reader.readLine()) != null) {
			if (cancel) {
				logger.warn("Network bulilder interrupted.");
				network.getRow(network).set(CyNetwork.NAME, "<Incomplete!> " + networkTitle);
				break;
			}

			i++;
			final String[] parts = pattern.split(line);

			// Skip invalid lines
			if (parts.length < MINIMUM_COLUMN_COUNT) {
				continue;
			}

			// Extract unique ID

			// Bad entry check
			if (parts[0] == null || parts[1] == null) {
				continue;
			}

			final String sourceFirst = SPLITTER.split(parts[0])[0];
			final String targetFirst = SPLITTER.split(parts[1])[0];
			final String[] sourceParts = SPLITTER_NAME_SPACE.split(sourceFirst);
			final String[] targetParts = SPLITTER_NAME_SPACE.split(targetFirst);
			if (sourceParts.length < 2 || targetParts.length < 2) {
				continue;
			}
			final String sourceID = sourceParts[1];
			final String targetID = targetParts[1];

			final CyNode sourceNode = addNode(nodes, sourceID, network);
			final CyNode targetNode = addNode(nodes, targetID, network);

			mapper.mapNodeColumn(parts, network.getRow(sourceNode), network.getRow(targetNode));

			final CyEdge newEdge = network.addEdge(sourceNode, targetNode, true);
			mapper.mapEdgeColumn(parts, network.getRow(newEdge));

			// Create new attribute if cross species
			// processCrossSpeciesEdge(network.getRow(newEdge),
			// network.getRow(sourceNode), network.getRow(targetNode));
		}

		reader.close();
		return network;
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

	public Map<String, CyNode> addToNetwork(final InteractionCluster iC, CyNetworkView networkView,
			final View<CyNode> hubNode) {
		return process(iC, networkView.getModel(), networkView, hubNode);
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

			// Create new attribute if cross species
			processCrossSpeciesEdge(network.getRow(newEdge), network.getRow(sourceNode), network.getRow(targetNode));

		}
		logger.info("Import Done: " + network.getSUID());
		return nodeMap;
	}

	private void processCrossSpeciesEdge(final CyRow row, final CyRow source, final CyRow target) {
		final String sTax = source.get(TAXNOMY, String.class);
		final String tTax = target.get(TAXNOMY, String.class);

		if (sTax == null || tTax == null)
			return;

		if (sTax.equals(tTax) == false) {
			row.set(InteractionClusterMapper.CROSS_SPECIES_EDGE, true);
		}
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
		if (edgeTable.getColumn(InteractionClusterMapper.PUB_DB) == null)
			edgeTable.createListColumn(InteractionClusterMapper.PUB_DB, String.class, false);
		if (edgeTable.getColumn(InteractionClusterMapper.PUB_ID) == null)
			edgeTable.createListColumn(InteractionClusterMapper.PUB_ID, String.class, false);
		if (edgeTable.getColumn(InteractionClusterMapper.EXPERIMENT) == null)
			edgeTable.createListColumn(InteractionClusterMapper.EXPERIMENT, String.class, false);
		if (edgeTable.getColumn(InteractionClusterMapper.CROSS_SPECIES_EDGE) == null)
			edgeTable.createColumn(InteractionClusterMapper.CROSS_SPECIES_EDGE, Boolean.class, false);

	}

	public void cancel() {
		this.cancel = true;
	}
}
