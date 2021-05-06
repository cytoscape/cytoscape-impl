package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class RefreshStyleTask extends AbstractTask {

	public static final String TITLE = "Refresh Style";
	
	private final ServicesUtil servicesUtil;

	
	public RefreshStyleTask(ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
	}

	public String getTitle() {
		return TITLE;
	}
	
	@Override
	public void run(TaskMonitor monitor) {
		CyNetworkView view = servicesUtil.get(CyApplicationManager.class).getCurrentNetworkView();
		if(view != null) {
			VisualStyle style = servicesUtil.get(VisualMappingManager.class).getVisualStyle(view);
			if(style != null) {
				style.apply(view);
				view.updateView();
			}
		}
	}
	
}
