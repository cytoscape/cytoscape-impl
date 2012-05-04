package org.cytoscape.task.internal.vizmap;

import java.util.Collection;

import org.cytoscape.task.AbstractNetworkViewCollectionTaskFactory;
import org.cytoscape.task.visualize.ApplyVisualStyleTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;

public class ApplyVisualStyleTaskFactoryimpl extends AbstractNetworkViewCollectionTaskFactory implements ApplyVisualStyleTaskFactory {

	private final VisualMappingManager vmm;

	public ApplyVisualStyleTaskFactoryimpl(final VisualMappingManager vmm) {
		super();
		this.vmm = vmm;
	}


	@Override
	public TaskIterator createTaskIterator(Collection<CyNetworkView> networkViews) {
		return new TaskIterator(new ApplyVisualStyleTask(networkViews, vmm));
	}

}
