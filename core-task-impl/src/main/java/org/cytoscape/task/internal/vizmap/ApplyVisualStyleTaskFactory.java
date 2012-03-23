package org.cytoscape.task.internal.vizmap;

import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;

public class ApplyVisualStyleTaskFactory extends AbstractNetworkViewTaskFactory {

	private final VisualMappingManager vmm;
	
	public ApplyVisualStyleTaskFactory(final VisualMappingManager vmm) {
		this.vmm = vmm;
	}
	
	@Override
	public TaskIterator createTaskIterator(final CyNetworkView networkView) {
		return new TaskIterator(new ApplyVisualStyleTask(networkView, vmm));
	}

}
