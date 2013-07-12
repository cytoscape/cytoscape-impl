package org.cytoscape.view.vizmap.gui.internal.controller;

import org.cytoscape.view.vizmap.gui.internal.model.VizMapperProxy;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.puremvc.java.multicore.interfaces.INotification;
import org.puremvc.java.multicore.patterns.command.SimpleCommand;

/**
 * Asks the proxy to load all the visual styles.
 */
public class LoadVisualStylesCommand extends SimpleCommand {

	private final ServicesUtil servicesUtil;
	
	public LoadVisualStylesCommand(final ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
	}

	@Override
	public void execute(final INotification notification) {
		final VizMapperProxy proxy = (VizMapperProxy) getFacade().retrieveProxy(VizMapperProxy.NAME);
		proxy.loadVisualStyles();
	}
}
