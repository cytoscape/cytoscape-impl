package org.cytoscape.view.vizmap.gui.internal.controller;

import java.util.Set;

import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.CurrentTableService;
import org.cytoscape.view.vizmap.gui.internal.task.RemoveVisualMappingsTask;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.puremvc.java.multicore.interfaces.INotification;
import org.puremvc.java.multicore.patterns.command.SimpleCommand;

/**
 * Removes the specified {@link VisualMappingFunction} objects from the current {@link VisualStyle}.
 */
public class RemoveVisualMappingsCommand extends SimpleCommand {

	private final ServicesUtil servicesUtil;
	
	public RemoveVisualMappingsCommand(ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void execute(INotification notification) {
		var set = (Set<VisualMappingFunction<?, ?>>) notification.getBody();
		
		var currentService = servicesUtil.get(CurrentTableService.class);
		var style = currentService.getCurrentVisualStyle();
		
		var iterator = new TaskIterator(new RemoveVisualMappingsTask(set, style, servicesUtil));
		var taskManager = servicesUtil.get(DialogTaskManager.class);
		taskManager.execute(iterator);
	}
}
