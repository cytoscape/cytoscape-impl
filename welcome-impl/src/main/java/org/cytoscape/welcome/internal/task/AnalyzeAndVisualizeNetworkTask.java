package org.cytoscape.welcome.internal.task;

import java.util.Collection;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkViewCollectionTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.welcome.internal.VisualStyleBuilder;
import org.cytoscape.work.TaskMonitor;

public class AnalyzeAndVisualizeNetworkTask extends AbstractNetworkViewCollectionTask {

	private final VisualStyleBuilder builder;
	private final VisualMappingManager vmm;

	public AnalyzeAndVisualizeNetworkTask(final Collection<CyNetworkView> networkViews,
			final VisualStyleBuilder builder, final VisualMappingManager vmm) {
		super(networkViews);
		this.builder = builder;
		this.vmm = vmm;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Visualizing Network");

		double progress = 0.0d;
		taskMonitor.setProgress(progress);
		final double increment = 1.0d / networkViews.size();

		for (final CyNetworkView networkView : networkViews) {
			final CyNetwork network = networkView.getModel();
			taskMonitor.setStatusMessage("Visualizing " + network.getRow(network).get(CyNetwork.NAME, String.class));

			final VisualStyle newStyle = builder.buildVisualStyle(networkView);
			vmm.addVisualStyle(newStyle);
			vmm.setCurrentVisualStyle(newStyle);
			newStyle.apply(networkView);
			networkView.updateView();

			progress += increment;
		}

	}
}
