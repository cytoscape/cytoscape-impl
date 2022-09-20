package org.cytoscape.view.vizmap.gui.internal.controller;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.task.CopyContinuousMappingTask;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.puremvc.java.multicore.interfaces.INotification;
import org.puremvc.java.multicore.patterns.command.SimpleCommand;

public class CopyContinuousMappingCommand extends SimpleCommand {

	private final ServicesUtil servicesUtil;
	
	public CopyContinuousMappingCommand(ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
	}

	public static record Body(
		VisualStyle sourceStyle, 
		VisualProperty<?> sourceVP, 
		VisualStyle targetStyle, 
		VisualProperty<?> targetVP
	) { }
	
	@Override
	public void execute(INotification notification) {
		var body = (Body) notification.getBody();
		
		var task = new CopyContinuousMappingTask(body.sourceStyle, body.sourceVP, body.targetStyle, body.targetVP, servicesUtil);
		
		var taskManager = servicesUtil.get(DialogTaskManager.class);
		taskManager.execute(new TaskIterator(task));
	}
	
}
