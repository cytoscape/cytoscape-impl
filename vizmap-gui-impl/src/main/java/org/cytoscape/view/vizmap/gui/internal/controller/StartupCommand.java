package org.cytoscape.view.vizmap.gui.internal.controller;

import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.LOAD_DEFAULT_VISUAL_STYLES;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.LOAD_VISUAL_STYLES;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.REMOVE_VISUAL_MAPPINGS;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.STARTUP;

import org.cytoscape.view.vizmap.gui.internal.model.AttributeSetProxy;
import org.cytoscape.view.vizmap.gui.internal.model.MappingFunctionFactoryProxy;
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
	private final AttributeSetProxy attributeSetProxy;
	private final MappingFunctionFactoryProxy mappingFactoryProxy;
	private final VizMapperMediator vizMapperMediator;
	private final  VizMapperMenuMediator vizMapperMenuMediator;
	private final ImportDefaultVisualStylesCommand importDefaultVisualStylesCommand;
	private final LoadVisualStylesCommand loadVisualStylesCommand;
	private final RemoveVisualMappingsCommand removeVisualMappingsCommand;
	
	public StartupCommand(final VizMapperProxy vizMapperProxy,
						  final AttributeSetProxy attributeSetProxy,
						  final MappingFunctionFactoryProxy mappingFactoryProxy,
						  final VizMapperMediator vizMapperMediator,
						  final VizMapperMenuMediator vizMapperMenuMediator,
						  final ImportDefaultVisualStylesCommand importDefaultVisualStylesCommand,
						  final LoadVisualStylesCommand loadVisualStylesCommand,
						  final RemoveVisualMappingsCommand removeVisualMappingsCommand) {
		this.vizMapperProxy = vizMapperProxy;
		this.attributeSetProxy = attributeSetProxy;
		this.mappingFactoryProxy = mappingFactoryProxy;
		this.vizMapperMediator = vizMapperMediator;
		this.vizMapperMenuMediator = vizMapperMenuMediator;
		this.importDefaultVisualStylesCommand = importDefaultVisualStylesCommand;
		this.loadVisualStylesCommand = loadVisualStylesCommand;
		this.removeVisualMappingsCommand = removeVisualMappingsCommand;
	}

	@Override
	public final void execute(final INotification notification) {
		// Register proxies
		getFacade().registerProxy(vizMapperProxy);
		getFacade().registerProxy(attributeSetProxy);
		getFacade().registerProxy(mappingFactoryProxy);
		
		// Register mediators
		getFacade().registerMediator(vizMapperMediator);
		getFacade().registerMediator(vizMapperMenuMediator);
		
		// Register other commands
		getFacade().registerCommand(LOAD_DEFAULT_VISUAL_STYLES, importDefaultVisualStylesCommand);
		getFacade().registerCommand(LOAD_VISUAL_STYLES, loadVisualStylesCommand);
		getFacade().registerCommand(REMOVE_VISUAL_MAPPINGS, removeVisualMappingsCommand);

		// Initialization of the visual styles list
		getFacade().sendNotification(LOAD_DEFAULT_VISUAL_STYLES);
		
		// Remove the STARTUP command because it is not called more than once
		getFacade().removeCommand(STARTUP);
	}
}
