package org.cytoscape.view.vizmap.gui.internal.controller;

import java.util.Set;

import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;
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

	public static record Body(VisualStyle style, Set<VisualMappingFunction<?, ?>> mappingFunctions) {}
	
	
	@Override
	public void execute(INotification notification) {
		var body = (Body) notification.getBody();
		
		var task = new RemoveVisualMappingsTask(body.mappingFunctions(), body.style(), servicesUtil);
		
		var taskManager = servicesUtil.get(DialogTaskManager.class);
		taskManager.execute(new TaskIterator(task));
	}
}
