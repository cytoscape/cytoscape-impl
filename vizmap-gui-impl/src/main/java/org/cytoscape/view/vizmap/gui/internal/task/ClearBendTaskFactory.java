package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.model.CyEdge;
import org.cytoscape.task.AbstractEdgeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.work.TaskIterator;

public class ClearBendTaskFactory extends AbstractEdgeViewTaskFactory {

	private final SelectedVisualStyleManager selectedManager;
	private final BendFactory bendFactory;
	
	public ClearBendTaskFactory(final SelectedVisualStyleManager selectedManager, final BendFactory bendFactory) {
		this.selectedManager = selectedManager;
		this.bendFactory = bendFactory;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyEdge> edgeView, CyNetworkView netView) {
		return new TaskIterator(new ClearBendTask(edgeView, netView, selectedManager, bendFactory));
	}
}
