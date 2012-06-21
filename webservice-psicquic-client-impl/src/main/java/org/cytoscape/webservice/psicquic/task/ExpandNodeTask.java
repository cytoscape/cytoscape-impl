package org.cytoscape.webservice.psicquic.task;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient.SearchMode;
import org.cytoscape.webservice.psicquic.ui.ResultDialog;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.enfin.mi.cluster.InteractionCluster;

public class ExpandNodeTask extends AbstractTask {
	private static final Logger logger = LoggerFactory.getLogger(ImportNetworkFromPSICQUICTask.class);

	private final PSICQUICRestClient client;

	private final String query;

	private Collection<String> targetServices;
	private final SearchRecoredsTask searchTask;

	private final CyNetworkView netView;
	private final View<CyNode> nodeView;

	private final CyEventHelper eh;
	private final VisualMappingManager vmm;

	private volatile boolean canceled = false;

	private HashMap<String, CyNode> nodeMap;

	public ExpandNodeTask(final String query, final PSICQUICRestClient client, final SearchRecoredsTask searchTask,
			final CyNetworkView parentNetworkView, final View<CyNode> nodeView, final CyEventHelper eh,
			final VisualMappingManager vmm) {
		this.client = client;
		this.query = query;
		this.netView = parentNetworkView;
		this.nodeView = nodeView;
		this.eh = eh;
		this.vmm = vmm;
		this.searchTask = searchTask;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Expanding by PSICQUIC Services");
		taskMonitor.setStatusMessage("Loading interaction from remote service...");
		taskMonitor.setProgress(0.01d);

		processSearchResult();

		if (query == null)
			throw new NullPointerException("Query is null");
		if (targetServices == null)
			throw new NullPointerException("Target service set is null");

		// Switch task type based on the user option.
		InteractionCluster ic = client.importNeighbours(query, targetServices, SearchMode.INTERACTOR, taskMonitor);

		if (canceled) {
			ic = null;
			return;
		}

		expand(ic);
		
		
	}

	private void expand(final InteractionCluster iC) {

		nodeMap = new HashMap<String, CyNode>();
		final CyNode parentNode = this.nodeView.getModel();
		final CyNetwork parentNetwork = netView.getModel();
		nodeMap.put(parentNetwork.getDefaultNodeTable().getRow(parentNode.getSUID()).get(CyNetwork.NAME, String.class),
				parentNode);

		Map<Integer, EncoreInteraction> interactions = iC.getInteractionMapping();
		Map<String, List<Integer>> interactorMapping = iC.getInteractorMapping();
		Map<String, String> interactorSynonyms = iC.getSynonymMapping();

		for (final Integer interactionKey : interactions.keySet()) {

			final EncoreInteraction interaction = interactions.get(interactionKey);

			final String source = interaction.getInteractorA();
			CyNode sourceNode = nodeMap.get(source);
			if (sourceNode == null) {
				sourceNode = parentNetwork.addNode();
				parentNetwork.getRow(sourceNode).set(CyNetwork.NAME, source);
				nodeMap.put(source, sourceNode);
			}
			final String target = interaction.getInteractorB();
			CyNode targetNode = nodeMap.get(target);
			if (targetNode == null) {
				targetNode = parentNetwork.addNode();
				parentNetwork.getRow(targetNode).set(CyNetwork.NAME, target);
				nodeMap.put(target, targetNode);
			}

			parentNetwork.getRow(sourceNode).set(CyNetwork.SELECTED, true);
			parentNetwork.getRow(targetNode).set(CyNetwork.SELECTED, true);
			// mapNodeAnnotations(interaction, network, sourceNode, targetNode);

			parentNetwork.addEdge(sourceNode, targetNode, true);
		}

		eh.flushPayloadEvents();

		for (CyNode n : nodeMap.values()) {
			final View<CyNode> nv = netView.getNodeView(n);
			nv.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION,
					nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION));
			nv.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION,
					nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION));
		}

		// Apply visual style
		VisualStyle vs = vmm.getVisualStyle(netView);
		vs.apply(netView);
		netView.updateView();

	}

	@Override
	public void cancel() {
		this.canceled = true;
		client.cancel();
	}

	private void processSearchResult() {
		final Map<String, Long> rs = searchTask.getResult();
		targetServices = new HashSet<String>();
		
		for (final String sourceURL : rs.keySet()) {
			final Long interactionCount = rs.get(sourceURL);
			if (interactionCount <= 0 || interactionCount>200)
				continue;

			//System.out.println(sourceURL + " = " + interactionCount);
			targetServices.add(sourceURL);
		}
	}
}
