package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.gui.internal.util.VizMapperUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateNewVisualStyleTask extends AbstractTask {

	private static final Logger logger = LoggerFactory.getLogger(CreateNewVisualStyleTask.class);
	
	private final VisualStyleFactory vsFactory;
	private final VizMapperUtil vizMapperUtil;
	
	private final VisualMappingManager vmm;
	
	public CreateNewVisualStyleTask(final VizMapperUtil vizMapperUtil,
			final VisualStyleFactory vsFactory, final VisualMappingManager vmm) {
		super();
		this.vizMapperUtil = vizMapperUtil;
		this.vsFactory = vsFactory;
		this.vmm = vmm;
	}

	
	public void run(TaskMonitor tm) {
		final String title = vizMapperUtil.getStyleName(null, null);

		if (title == null)
			return;

		// Create new style.  This method call automatically fire event.
		final VisualStyle newStyle = vsFactory.createVisualStyle(title);
		vmm.addVisualStyle(newStyle);
		logger.info("CreateNewVisualStyleTask created new Visual Style: " + newStyle.getTitle());
	}
}