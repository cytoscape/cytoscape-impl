package org.cytoscape.task.internal.edit;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

public class ConnectSelectedNodesTask extends AbstractTask {

	// TODO: is it sufficient to create undirected edge only?
	static final String INTERACTION = "undirected";

	private final UndoSupport undoSupport;
	private final CyNetwork network;
	private final CyEventHelper eventHelper;
	private final VisualMappingManager vmm;
	private final CyNetworkViewManager netViewMgr;

	public ConnectSelectedNodesTask(final UndoSupport undoSupport, final CyNetwork network,
			final CyEventHelper eventHelper, final VisualMappingManager vmm, final CyNetworkViewManager netViewMgr) {
		this.undoSupport = undoSupport;

		if (network == null)
			throw new NullPointerException("Network is null.");

		this.network = network;
		this.eventHelper = eventHelper;
		this.vmm = vmm;
		this.netViewMgr = netViewMgr;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);
		taskMonitor.setTitle("Connecting Selected Nodes");
		taskMonitor.setStatusMessage("Connecting nodes.  Please wait...");
		
		final List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);

		taskMonitor.setProgress(0.1);

		int selectedNodesCount = selectedNodes.size();
		int i = 0;

		final List<CyEdge> newEdges = new ArrayList<CyEdge>();

		for (final CyNode source : selectedNodes) {
			for (final CyNode target : selectedNodes) {
				if (source != target) {
					final List<CyNode> sourceNeighborList = network.getNeighborList(source, Type.ANY);

					if (!sourceNeighborList.contains(target)) {
						// connect it
						final CyEdge newEdge = network.addEdge(source, target, false);
						newEdges.add(newEdge);
						network.getRow(newEdge).set(
								CyNetwork.NAME,
								network.getRow(source).get(CyNetwork.NAME, String.class) + " (" + INTERACTION + ") "
										+ network.getRow(target).get(CyNetwork.NAME, String.class));
						network.getRow(newEdge).set(CyEdge.INTERACTION, INTERACTION);
					}
				}
			}

			i++;
			taskMonitor.setProgress(0.1 + i / (double) selectedNodesCount * 0.9);
		}

		undoSupport.postEdit(new ConnectSelectedNodesEdit(network, newEdges));

		// Apply visual style
		eventHelper.flushPayloadEvents(); // To make sure the edge views are created before applying the style

		for (final CyNetworkView view : netViewMgr.getNetworkViews(network)) {
			VisualStyle vs = vmm.getVisualStyle(view);
			vs.apply(view);
			view.updateView();
		}

		taskMonitor.setProgress(1.0);
	}
}
