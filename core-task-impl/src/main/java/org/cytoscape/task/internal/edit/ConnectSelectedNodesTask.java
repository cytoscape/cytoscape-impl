package org.cytoscape.task.internal.edit;


import java.util.ArrayList;
import java.util.List;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;


public class ConnectSelectedNodesTask extends AbstractTask {
	// TODO: is it sufficient to create undirected edge only?
	static final String INTERACTION = "undirected";
	private final UndoSupport undoSupport;
	private final CyNetwork network;
	private final CyEventHelper eventHelper;

	public ConnectSelectedNodesTask(final UndoSupport undoSupport, final CyNetwork network,
	                                final CyEventHelper eventHelper)
	{
		this.undoSupport = undoSupport;
		if (network == null)
			throw new NullPointerException("Network is null.");
		this.network = network;
		this.eventHelper = eventHelper;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);
		final CyTable nodeTable = network.getDefaultNodeTable();
		final CyTable edgeTable = network.getDefaultEdgeTable();
		final List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);

		taskMonitor.setProgress(0.1);
		
		int selectedNodesCount = selectedNodes.size();
		int i=0;
		
		final List<CyEdge> newEdges = new ArrayList<CyEdge>();
		for (final CyNode source : selectedNodes) {
			for (final CyNode target : selectedNodes) {
				if (source != target) {
					final List<CyNode> sourceNeighborList = network.getNeighborList(source, Type.ANY);
					if (!sourceNeighborList.contains(target)) {
						// connect it
						final CyEdge newEdge = network.addEdge(source, target, false);
						newEdges.add(newEdge);
						network.getCyRow(newEdge).set(
							CyTableEntry.NAME,
							network.getCyRow(source).get(CyTableEntry.NAME, String.class) + " (" + INTERACTION + ") "
							+ network.getCyRow(target).get(CyTableEntry.NAME, String.class));
						network.getCyRow(newEdge).set(CyEdge.INTERACTION, INTERACTION);
					}
				}
			}
			
			i++;
			taskMonitor.setProgress(0.1+i/(double)selectedNodesCount*0.9);
		}

		undoSupport.postEdit(
			new ConnectSelectedNodesEdit(network, newEdges));
		
		taskMonitor.setProgress(1.0);
	}
}
