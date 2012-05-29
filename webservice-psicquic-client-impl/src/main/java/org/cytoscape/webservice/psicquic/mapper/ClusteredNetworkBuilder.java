package org.cytoscape.webservice.psicquic.mapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.enfin.mi.cluster.InteractionCluster;


// TODO Make this smarter
public class ClusteredNetworkBuilder {

	private static final Logger logger = LoggerFactory.getLogger(ClusteredNetworkBuilder.class);

	final String fisrtSeparator = "\t";
    final String secondSeparator = ",";
	
	private final CyNetworkFactory networkFactory;

	volatile boolean cancel = false;

	private Map<String, CyNode> nodeMap;

	public ClusteredNetworkBuilder(final CyNetworkFactory networkFactory) {
		this.networkFactory = networkFactory;

	}

	public CyNetwork buildNetwork(final InteractionCluster iC) throws IOException {
		CyNetwork network = networkFactory.createNetwork();

		nodeMap = new HashMap<String, CyNode>();
		Map<Integer, EncoreInteraction> interactions = iC.getInteractionMapping();
		Map<String, List<Integer>> interactorMapping = iC.getInteractorMapping();
		Map<String, String> interactorSynonyms = iC.getSynonymMapping();

		for (Integer interactionKey : interactions.keySet()) {
			if (cancel) {
				logger.warn("Network bulilder interrupted.");
				network = null;
				return null;
			}
			
			
			EncoreInteraction val = interactions.get(interactionKey);
			
			String source = val.getInteractorA();
			CyNode sourceNode = nodeMap.get(source);
			if (sourceNode == null) {
				sourceNode = network.addNode();
				network.getRow(sourceNode).set(CyNetwork.NAME, source);
				nodeMap.put(source, sourceNode);
			}
			String target = val.getInteractorB();
			CyNode targetNode = nodeMap.get(target);
			if (targetNode == null) {
				targetNode = network.addNode();
				network.getRow(targetNode).set(CyNetwork.NAME, target);
				nodeMap.put(target, targetNode);
			}
			
			network.addEdge(sourceNode, targetNode, true);
		}

		logger.info("Import Done: " + network.getSUID());
		return network;
	}

	public void cancel() {
		this.cancel = true;
	}
}
