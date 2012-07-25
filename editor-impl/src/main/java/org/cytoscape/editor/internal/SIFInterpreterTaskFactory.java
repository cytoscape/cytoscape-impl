package org.cytoscape.editor.internal;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.work.TaskIterator;

public class SIFInterpreterTaskFactory extends AbstractNetworkViewTaskFactory {

	private final VisualMappingManager vmm;
	private final CyEventHelper eh;

	public SIFInterpreterTaskFactory(final VisualMappingManager vmm, final CyEventHelper eh) {
		this.vmm = vmm;
		this.eh = eh;
	}

	public TaskIterator createTaskIterator(final CyNetworkView view) {
		return new TaskIterator(new SIFInterpreterTask(view, vmm, eh));
	}
}

