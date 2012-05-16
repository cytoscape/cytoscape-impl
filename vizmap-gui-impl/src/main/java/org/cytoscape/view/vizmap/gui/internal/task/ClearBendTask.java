package org.cytoscape.view.vizmap.gui.internal.task;

import javax.swing.SwingUtilities;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class ClearBendTask extends AbstractTask {

	private final View<CyEdge> edgeView;
	private final CyNetworkView netView;
	private final BendFactory bendFactory;

	private final SelectedVisualStyleManager selectedManager;

	ClearBendTask(View<CyEdge> edgeView, CyNetworkView netView, final SelectedVisualStyleManager selectedManager,
			final BendFactory bendFactory) {
		this.edgeView = edgeView;
		this.netView = netView;
		this.selectedManager = selectedManager;
		this.bendFactory = bendFactory;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				final VisualStyle style = selectedManager.getCurrentVisualStyle();
				final VisualMappingFunction<?, Bend> mapping = style
						.getVisualMappingFunction(BasicVisualLexicon.EDGE_BEND);
				if (mapping != null) {
					edgeView.setVisualProperty(BasicVisualLexicon.EDGE_BEND, bendFactory.createBend());
				} else {
					style.setDefaultValue(BasicVisualLexicon.EDGE_BEND, bendFactory.createBend());
					selectedManager.getCurrentVisualStyle().apply(netView);
				}
				netView.updateView();
			}
		});
	}
}
