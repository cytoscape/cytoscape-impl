package org.cytoscape.view.vizmap.gui.internal.task;

import java.io.IOException;
import java.util.Iterator;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.events.SetCurrentVisualStyleEvent;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateNewVisualStyleTask extends AbstractTask implements TunableValidator {

	private static final Logger logger = LoggerFactory.getLogger(CreateNewVisualStyleTask.class);
	
	@ProvidesTitle
	public String getTitle() {
		return "Create New Visual Style";
	}

	@Tunable(description = "Name of new Visual Style:")
	public String vsName;
	
	private final VisualStyleFactory vsFactory;
	private final VisualMappingManager vmm;
	private final CyEventHelper eventHelper;
	
	public CreateNewVisualStyleTask(final VisualStyleFactory vsFactory,
									final VisualMappingManager vmm,
									final CyEventHelper eventHelper) {
		super();
		this.vsFactory = vsFactory;
		this.vmm = vmm;
		this.eventHelper = eventHelper;
	}

	
	public void run(TaskMonitor tm) {
		if (vsName == null)
			return;

		// Create new style.  This method call automatically fire event.
		final VisualStyle style = vsFactory.createVisualStyle(vsName);
		vmm.addVisualStyle(style);
		logger.debug("CreateNewVisualStyleTask created new Visual Style: " + style);
		eventHelper.fireEvent(new SetCurrentVisualStyleEvent(this, style));
	}
	
	
	public ValidationState getValidationState(final Appendable errMsg){
		Iterator<VisualStyle> it = this.vmm.getAllVisualStyles().iterator();
		
		while(it.hasNext()){
			VisualStyle exist_vs = it.next();
			
			if (exist_vs.getTitle().equalsIgnoreCase(vsName)){
				try {
					errMsg.append("Visual style "+ vsName +" already existed!");
					return ValidationState.INVALID;
				} catch (IOException e) {
				}
			}
		}
		
		return ValidationState.OK;
	}
}