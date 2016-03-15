package org.cytoscape.view.vizmap.gui.internal.controller;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.internal.model.VizMapperProxy;
import org.cytoscape.view.vizmap.gui.internal.task.ImportDefaultVizmapTask;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;
import org.puremvc.java.multicore.interfaces.INotification;
import org.puremvc.java.multicore.patterns.command.SimpleCommand;

/**
 * Import the default visual styles into the proxy.
 */
public class ImportDefaultVisualStylesCommand extends SimpleCommand {

	private final ServicesUtil servicesUtil;
	
	public ImportDefaultVisualStylesCommand(final ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
	}

	@Override
	public void execute(final INotification notification) {
		final TaskIterator iterator = new TaskIterator(new ImportDefaultVizmapTask(servicesUtil));
		final DialogTaskManager taskManager = servicesUtil.get(DialogTaskManager.class);
		
		final VizMapperProxy proxy = (VizMapperProxy) getFacade().retrieveProxy(VizMapperProxy.NAME);
		proxy.setIgnoreStyleEvents(true);
		
		taskManager.execute(iterator, new TaskObserver() {
			@Override
			public void taskFinished(ObservableTask task) {
			}
			@Override
			public void allFinished(FinishStatus finishStatus) {
				proxy.setIgnoreStyleEvents(false);
				proxy.loadVisualStyles();
				proxy.setCurrentVisualStyle(servicesUtil.get(VisualMappingManager.class).getDefaultVisualStyle());
			}
		});
	}
}
