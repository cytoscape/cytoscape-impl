package org.cytoscape.task.internal.vizmap;

import java.util.Collection;

import org.cytoscape.model.CyEdge;
import org.cytoscape.task.AbstractNetworkViewCollectionTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskMonitor;

public class ClearEdgeBendTask extends AbstractNetworkViewCollectionTask {

	public ClearEdgeBendTask(Collection<CyNetworkView> networkViews) {
		super(networkViews);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		for (CyNetworkView networkView : networkViews) {
			final Collection<View<CyEdge>> edgeViews = networkView.getEdgeViews();
			for (final View<CyEdge> edgeView : edgeViews) {
				edgeView.setVisualProperty(BasicVisualLexicon.EDGE_BEND, null);
				edgeView.clearValueLock(BasicVisualLexicon.EDGE_BEND);
			}
			
			networkView.updateView();
		}
	}
}
