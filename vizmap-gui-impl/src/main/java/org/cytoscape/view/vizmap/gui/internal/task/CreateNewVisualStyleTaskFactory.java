package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.gui.internal.util.VizMapperUtil;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class CreateNewVisualStyleTaskFactory implements TaskFactory {
	
	private final VizMapperUtil vizMapperUtil;
	private final VisualStyleFactory vsFactory;
	private final VisualMappingManager vmm;
	
	public CreateNewVisualStyleTaskFactory(final VizMapperUtil vizMapperUtil,
	final VisualStyleFactory vsFactory, final VisualMappingManager vmm) {
		this.vizMapperUtil = vizMapperUtil;
		this.vsFactory = vsFactory;
		this.vmm = vmm;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new CreateNewVisualStyleTask(vizMapperUtil, vsFactory, vmm));
	}

}
