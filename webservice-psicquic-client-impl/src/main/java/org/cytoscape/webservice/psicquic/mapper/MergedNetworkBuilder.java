package org.cytoscape.webservice.psicquic.mapper;

import static org.cytoscape.webservice.psicquic.mapper.InteractionClusterMapper.TAXNOMY;
import static org.cytoscape.webservice.psicquic.mapper.InteractionClusterMapper.TAXNOMY_DB;
import static org.cytoscape.webservice.psicquic.mapper.InteractionClusterMapper.TAXNOMY_NAME;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.enfin.mi.cluster.InteractionCluster;

// TODO Make this smarter
public class MergedNetworkBuilder {

	private static final Logger logger = LoggerFactory.getLogger(MergedNetworkBuilder.class);

	private final InteractionClusterMapper mapper;

	final String fisrtSeparator = "\t";
	final String secondSeparator = ",";

	private final CyNetworkFactory networkFactory;

	volatile boolean cancel = false;

	private Map<String, CyNode> nodeMap;

	public MergedNetworkBuilder(final CyNetworkFactory networkFactory) {
		this.networkFactory = networkFactory;
		mapper = new InteractionClusterMapper();
	}

	public CyNetwork buildNetwork(final InteractionCluster iC) throws IOException {
		CyNetwork network = networkFactory.createNetwork();
		process(iC, network, null, null);
		return network;
	}

	public Map<String, CyNode> addToNetwork(final InteractionCluster iC, CyNetworkView networkView,
			final View<CyNode> hubNode) {
		return process(iC, networkView.getModel(), networkView, hubNode);
	}

	private final Map<String, CyNode> process(final InteractionCluster iC, CyNetwork network, CyNetworkView netView,
			final View<CyNode> hubNode) {
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

	private final void prepareColumns(CyNetwork network) {
		final CyTable nodeTable = network.getDefaultNodeTable();
		final CyTable edgeTable = network.getDefaultEdgeTable();
		if (nodeTable.getColumn(TAXNOMY) == null)
			nodeTable.createColumn(TAXNOMY, String.class, false);
		if (nodeTable.getColumn(TAXNOMY_NAME) == null)
			nodeTable.createColumn(TAXNOMY_NAME, String.class, false);
		if (nodeTable.getColumn(TAXNOMY_DB) == null)
			nodeTable.createColumn(TAXNOMY_DB, String.class, false);

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
		
	}

	public void cancel() {
		this.cancel = true;
	}
}
