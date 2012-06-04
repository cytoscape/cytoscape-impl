package org.cytoscape.webservice.psicquic.mapper;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.enfin.mi.cluster.InteractionCluster;

// TODO Make this smarter
public class MergedNetworkBuilder {

	private static final Logger logger = LoggerFactory.getLogger(MergedNetworkBuilder.class);

	// TODO create utility to provide default set of column names from
	// identifiers.org

	private static final String TAXNOMY = "taxonomy";
	private static final String TAXNOMY_NAME = "taxonomy.name";
	private static final String TAXNOMY_DB = "taxonomy.db";

	final String fisrtSeparator = "\t";
	final String secondSeparator = ",";

	private final CyNetworkFactory networkFactory;

	volatile boolean cancel = false;

	private Map<String, CyNode> nodeMap;

	public MergedNetworkBuilder(final CyNetworkFactory networkFactory) {
		this.networkFactory = networkFactory;

	}

	public CyNetwork buildNetwork(final InteractionCluster iC) throws IOException {
		CyNetwork network = networkFactory.createNetwork();

		nodeMap = new HashMap<String, CyNode>();
		Map<Integer, EncoreInteraction> interactions = iC.getInteractionMapping();
		Map<String, List<Integer>> interactorMapping = iC.getInteractorMapping();
		Map<String, String> interactorSynonyms = iC.getSynonymMapping();

		prepareColumns(network);

		for (final Integer interactionKey : interactions.keySet()) {
			if (cancel) {
				logger.warn("Network bulilder interrupted.");
				network = null;
				return null;
			}

			final EncoreInteraction interaction = interactions.get(interactionKey);
			
			final String source = interaction.getInteractorA();
			CyNode sourceNode = nodeMap.get(source);
			if (sourceNode == null) {
				sourceNode = network.addNode();
				network.getRow(sourceNode).set(CyNetwork.NAME, source);
				nodeMap.put(source, sourceNode);
			}
			final String target = interaction.getInteractorB();
			CyNode targetNode = nodeMap.get(target);
			if (targetNode == null) {
				targetNode = network.addNode();
				network.getRow(targetNode).set(CyNetwork.NAME, target);
				nodeMap.put(target, targetNode);
			}

			mapNodeAnnotations(interaction, network, sourceNode, targetNode);

			network.addEdge(sourceNode, targetNode, true);
		}

		logger.info("Import Done: " + network.getSUID());
		return network;
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
	}

	private void mapNodeAnnotations(final EncoreInteraction interaction, CyNetwork network, CyNode source, CyNode target) {
		final CyRow sourceRow = network.getRow(source);
		final CyRow targetRow = network.getRow(target);

		final Map<String, String> accsSource = interaction.getInteractorAccsA();
		final Map<String, String> accsTarget = interaction.getInteractorAccsB();
		processNames(sourceRow, accsSource);
		processNames(targetRow, accsTarget);

		final Map<String, List<String>> otherSource = interaction.getOtherInteractorAccsA();
		final Map<String, List<String>> otherTarget = interaction.getOtherInteractorAccsB();
//		processOtherNames(sourceRow, otherSource);
//		processOtherNames(targetRow, otherTarget);
		
		final Collection<CrossReference> speciesSource = interaction.getOrganismsA();
		final Collection<CrossReference> speciesTarget = interaction.getOrganismsB();

//		CrossReference speciesSourceFirst = speciesSource.iterator().next();
//		CrossReference speciesTargetFirst = speciesTarget.iterator().next();
//		processSpecies(sourceRow, speciesSourceFirst);
//		processSpecies(targetRow, speciesTargetFirst);
	}

	private void processNames(CyRow row, final Map<String, String> accs) {
		for (String dbName : accs.keySet()) {
			if (row.getTable().getColumn(dbName) == null)
				row.getTable().createColumn(dbName, String.class, true);
			row.set(dbName, accs.get(dbName));
		}

	}
	
	private void processOtherNames(CyRow row, final Map<String, List<String>> accs) {
		for (String dbName : accs.keySet()) {
			if (row.getTable().getColumn(dbName) == null)
				row.getTable().createColumn("other." + dbName, String.class, true);
			
			final List<String> names = accs.get(dbName);
			StringBuilder builder = new StringBuilder();
			for(String name: names) {
				builder.append(name + ",");
			}
			String longName = builder.toString();
			longName = longName.substring(0, longName.length()-2);
			row.set(dbName, longName);
			
		}
	}

	private void processSpecies(CyRow row, CrossReference ref) {
		if (ref != null) {
			final String name = ref.getText();
			final String speciesID = ref.getIdentifier();
			final String db = ref.getDatabase();

			row.set(TAXNOMY, speciesID);
			row.set(TAXNOMY_NAME, name);
			row.set(TAXNOMY_DB, db);
		}
	}

	public void cancel() {
		this.cancel = true;
	}
}
