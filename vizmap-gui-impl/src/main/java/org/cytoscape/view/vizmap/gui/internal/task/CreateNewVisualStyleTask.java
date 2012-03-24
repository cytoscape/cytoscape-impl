package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateNewVisualStyleTask extends AbstractTask {

	private static final Logger logger = LoggerFactory.getLogger(CreateNewVisualStyleTask.class);
	
	@ProvidesTitle
	public String getTitle() {
		return "Create New Visual Style";
	}

	@Tunable(description = "Name of new Visual Style:")
	public String vsName;
	
	private final VisualStyleFactory vsFactory;
	private final VisualMappingManager vmm;
	
	public CreateNewVisualStyleTask(final VisualStyleFactory vsFactory, final VisualMappingManager vmm) {
		super();
		this.vsFactory = vsFactory;
		this.vmm = vmm;
	}

	
	public void run(TaskMonitor tm) {
		if (vsName == null)
			return;

		// Create new style.  This method call automatically fire event.
		final VisualStyle newStyle = vsFactory.createVisualStyle(vsName);
		vmm.addVisualStyle(newStyle);
		logger.info("CreateNewVisualStyleTask created new Visual Style: " + newStyle.getTitle());
	}
}