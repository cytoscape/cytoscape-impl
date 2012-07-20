package org.cytoscape.welcome.internal.task;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.task.analyze.AnalyzeNetworkCollectionTaskFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.welcome.internal.VisualStyleBuilder;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class GenerateCustomStyleTaskFactory extends AbstractTaskFactory {

	private final AnalyzeNetworkCollectionTaskFactory analyzeNetworkCollectionTaskFactory;
	private final CyApplicationManager applicationManager;

	private final VisualStyleBuilder builder;
	private final VisualMappingManager vmm;

	public GenerateCustomStyleTaskFactory(
			final AnalyzeNetworkCollectionTaskFactory analyzeNetworkCollectionTaskFactory,
			final CyApplicationManager applicationManager, final VisualStyleBuilder builder,
			final VisualMappingManager vmm) {
		this.analyzeNetworkCollectionTaskFactory = analyzeNetworkCollectionTaskFactory;
		this.applicationManager = applicationManager;
		this.builder = builder;
		this.vmm = vmm;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new GenerateCustomStyleTask(analyzeNetworkCollectionTaskFactory, applicationManager,
				builder, vmm));
	}

}
