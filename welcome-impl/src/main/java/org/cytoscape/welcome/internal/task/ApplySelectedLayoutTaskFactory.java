package org.cytoscape.welcome.internal.task;

import java.util.HashSet;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ApplySelectedLayoutTaskFactory extends AbstractTaskFactory {

	private final CyApplicationManager applicationManager;
	private final CyLayoutAlgorithmManager cyLayoutAlgorithmManager;

	private final CyServiceRegistrar registrar;

	public ApplySelectedLayoutTaskFactory(final CyServiceRegistrar registrar,
			final CyApplicationManager applicationManager, final CyLayoutAlgorithmManager cyLayoutAlgorithmManager) {

		this.applicationManager = applicationManager;
		this.cyLayoutAlgorithmManager = cyLayoutAlgorithmManager;
		this.registrar = registrar;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ApplySelectedLayoutTask(new HashSet<CyNetworkView>(), cyLayoutAlgorithmManager,
				applicationManager, registrar));
	}

}
