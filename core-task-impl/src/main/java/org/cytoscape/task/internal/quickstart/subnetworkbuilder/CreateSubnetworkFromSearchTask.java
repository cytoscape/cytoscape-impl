package org.cytoscape.task.internal.quickstart.subnetworkbuilder;


import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.task.internal.select.SelectFirstNeighborsTask;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CreateSubnetworkFromSearchTask extends AbstractTask {
	private static final Logger logger = LoggerFactory.getLogger(CreateSubnetworkFromSearchTask.class);

	static final String QUERY_GENE_ATTR_NAME = "Gene Type";
	static final String SEARCH_GENE_ATTR_NAME = "Search Term";

	private final SubnetworkBuilderUtil util;
	private final SubnetworkBuilderState state;

	public CreateSubnetworkFromSearchTask(final SubnetworkBuilderUtil util, final SubnetworkBuilderState state) {
		this.util = util;
		this.state = state;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Searching related genes in parent network...");
		taskMonitor.setProgress(-1);
		
		selectGenes(new ArrayList<String>());

		taskMonitor.setProgress(1.0);
	}

	protected void selectGenes(final List<String> geneList) {
		final CyNetwork target = util.appManager.getCurrentNetwork();
		final CyTable nodeTable = target.getDefaultNodeTable();

		if (nodeTable.getColumn(QUERY_GENE_ATTR_NAME) == null)
			nodeTable.createColumn(QUERY_GENE_ATTR_NAME, String.class, false);
		if (nodeTable.getColumn(SEARCH_GENE_ATTR_NAME) == null)
			nodeTable.createColumn(SEARCH_GENE_ATTR_NAME, String.class, false);

		boolean found = false;
		List<CyNode> nodeList = target.getNodeList();
		for (final CyNode node : nodeList) {
			final String nodeName = target.getRow(node).get(CyTableEntry.NAME, String.class);

			if (geneList.contains(nodeName) && state.getDiseaseGenes().contains(nodeName)) {
				target.getRow(node).set(CyNetwork.SELECTED, true);
				target.getRow(node).set(QUERY_GENE_ATTR_NAME, "query and disease");
				target.getRow(node).set(SEARCH_GENE_ATTR_NAME, state.getSearchTerms());
				found = true;
			} else if (geneList.contains(nodeName)) {
				target.getRow(node).set(CyNetwork.SELECTED, true);
				target.getRow(node).set(QUERY_GENE_ATTR_NAME, "query");
				found = true;
			} else if (state.getDiseaseGenes().contains(nodeName)) {
				target.getRow(node).set(CyNetwork.SELECTED, true);
				target.getRow(node).set(QUERY_GENE_ATTR_NAME, "disease");
				target.getRow(node).set(SEARCH_GENE_ATTR_NAME, state.getSearchTerms());
			}
		}

		if (!found) {
			logger.error("Query genes were not found in the interactome.");
			return;
		}

		this.insertTasksAfterCurrentTask(new BuildVisualStyleTask(util));

		this.insertTasksAfterCurrentTask(util.getApplLayoutTask());

		Task createNetworkTask = util.getNewNetworkSelectedNodesOnlyTask(target);
		this.insertTasksAfterCurrentTask(createNetworkTask);

		SelectFirstNeighborsTask nextTask =
			new SelectFirstNeighborsTask(util.getUndoSupport(), target, util.networkViewManager,
			                             util.eventHelper, CyEdge.Type.ANY);
		this.insertTasksAfterCurrentTask(nextTask);
	}
}
