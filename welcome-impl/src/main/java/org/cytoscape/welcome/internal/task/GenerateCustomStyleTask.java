package org.cytoscape.welcome.internal.task;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.analyze.AnalyzeNetworkCollectionTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.welcome.internal.VisualStyleBuilder;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class GenerateCustomStyleTask extends AbstractTask {

	private final AnalyzeNetworkCollectionTaskFactory analyzeNetworkCollectionTaskFactory;
	private final CyApplicationManager applicationManager;

	private final VisualStyleBuilder builder;
	private final VisualMappingManager vmm;

	GenerateCustomStyleTask(final AnalyzeNetworkCollectionTaskFactory analyzeNetworkCollectionTaskFactory,
			final CyApplicationManager applicationManager, final VisualStyleBuilder builder,
			final VisualMappingManager vmm) {
		this.analyzeNetworkCollectionTaskFactory = analyzeNetworkCollectionTaskFactory;
		this.applicationManager = applicationManager;
		this.builder = builder;
		this.vmm = vmm;

	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

		final List<CyNetworkView> selectedViews = applicationManager.getSelectedNetworkViews();
		final CyNetworkView currentView = applicationManager.getCurrentNetworkView();

		final Set<CyNetworkView> networkViews = new HashSet<CyNetworkView>(selectedViews);
		networkViews.add(currentView);

		final Set<CyNetwork> networks = new HashSet<CyNetwork>();
		networks.add(currentView.getModel());
		for (final CyNetworkView view : selectedViews)
			networks.add(view.getModel());

		final TaskIterator analyzeItr = analyzeNetworkCollectionTaskFactory.createTaskIterator(networks);
		final CreateCustomViewTask createCustomViewTask = new CreateCustomViewTask(
				networkViews, builder, vmm);
		
		insertTasksAfterCurrentTask(createCustomViewTask);
		insertTasksAfterCurrentTask(analyzeItr);
	}

}
