package org.cytoscape.view.vizmap.gui.internal.controller;

import static org.cytoscape.view.vizmap.gui.internal.ApplicationFacade.LOAD_DEFAULT_VISUAL_STYLES;
import static org.cytoscape.view.vizmap.gui.internal.ApplicationFacade.LOAD_VISUAL_STYLES;

import org.cytoscape.view.vizmap.gui.internal.ApplicationFacade;
import org.cytoscape.view.vizmap.gui.internal.model.VizMapperProxy;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMediator;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMenuMediator;
import org.puremvc.java.multicore.interfaces.INotification;
import org.puremvc.java.multicore.patterns.command.SimpleCommand;

/**
 * Initializes the view. Register mediators and proxies.
 */
public class StartupCommand extends SimpleCommand {

	private final VizMapperProxy vizMapperProxy;
	private final VizMapperMediator vizMapperMediator;
	private final  VizMapperMenuMediator vizMapperMenuMediator;
	private final ImportDefaultVisualStylesCommand importDefaultVisualStylesCommand;
	private final LoadVisualStylesCommand loadVisualStylesCommand;
	
	public StartupCommand(final VizMapperProxy vizMapperProxy,
						  final VizMapperMediator vizMapperMediator,
						  final VizMapperMenuMediator vizMapperMenuMediator,
						  final ImportDefaultVisualStylesCommand importDefaultVisualStylesCommand,
						  final LoadVisualStylesCommand loadVisualStylesCommand) {
		this.vizMapperProxy = vizMapperProxy;
		this.vizMapperMediator = vizMapperMediator;
		this.vizMapperMenuMediator = vizMapperMenuMediator;
		this.importDefaultVisualStylesCommand = importDefaultVisualStylesCommand;
		this.loadVisualStylesCommand = loadVisualStylesCommand;
	}

	@Override
	public final void execute(final INotification notification) {
		// Register proxies
		getFacade().registerProxy(vizMapperProxy);
		// Register mediators
		getFacade().registerMediator(vizMapperMediator);
		getFacade().registerMediator(vizMapperMenuMediator);
		// Register other commands
		getFacade().registerCommand(LOAD_DEFAULT_VISUAL_STYLES, importDefaultVisualStylesCommand);
		getFacade().registerCommand(LOAD_VISUAL_STYLES, loadVisualStylesCommand);

		// Initialization of the visual styles list
		getFacade().sendNotification(ApplicationFacade.LOAD_DEFAULT_VISUAL_STYLES);
		
		// Remove the STARTUP command because it is not called more than once
		getFacade().removeCommand(ApplicationFacade.STARTUP);
	}
}
