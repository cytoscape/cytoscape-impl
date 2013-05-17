package org.cytoscape.view.vizmap.gui.internal.controller;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.internal.model.VizMapperProxy;
import org.cytoscape.view.vizmap.gui.internal.task.ImportDefaultVizmapTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
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
		final ImportDefaultVizmapTaskFactory tf = new ImportDefaultVizmapTaskFactory(
				servicesUtil.get(VizmapReaderManager.class), servicesUtil.get(VisualMappingManager.class),
				servicesUtil.get(CyApplicationConfiguration.class), servicesUtil.get(RenderingEngineManager.class));
		servicesUtil.get(DialogTaskManager.class).execute(tf.createTaskIterator());
		
		final VizMapperProxy proxy = (VizMapperProxy) getFacade().retrieveProxy(VizMapperProxy.NAME);
		proxy.loadVisualStyles();
		proxy.setCurrentVisualStyle(servicesUtil.get(VisualMappingManager.class).getDefaultVisualStyle());
	}
}
