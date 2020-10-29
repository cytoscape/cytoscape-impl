package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.view.vizmap.TableVisualMappingManager;
import org.cytoscape.view.vizmap.gui.internal.CurrentTableService;
import org.cytoscape.view.vizmap.gui.internal.model.VizMapperProxy;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class ClearColumnStyleTask extends AbstractTask {

	public static final String TITLE = "Clear Current Style";
	
	private final ServicesUtil servicesUtil;
	
	public ClearColumnStyleTask(ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
	}
	
	@Override
	public void run(TaskMonitor tm) {
		var vmProxy = (VizMapperProxy) servicesUtil.getProxy(VizMapperProxy.NAME);
		var column = servicesUtil.get(CurrentTableService.class).getCurrentColumn();
		if(column == null)
			return;
		
		var colView = vmProxy.getColumnView(column);
		if(colView == null)
			return;
		
		var tvmm = servicesUtil.get(TableVisualMappingManager.class);
		tvmm.setVisualStyle(colView, null);
	}

}
