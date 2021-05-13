package org.cytoscape.view.vizmap.gui.internal.controller;

import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.LOAD_DEFAULT_VISUAL_STYLES;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.LOAD_VISUAL_STYLES;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.REMOVE_LOCKED_VALUES;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.REMOVE_VISUAL_MAPPINGS;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.SET_LOCKED_VALUES;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.STARTUP;

import org.cytoscape.view.vizmap.gui.internal.model.AttributeSetProxy;
import org.cytoscape.view.vizmap.gui.internal.model.MappingFunctionFactoryProxy;
import org.cytoscape.view.vizmap.gui.internal.model.PropsProxy;
import org.cytoscape.view.vizmap.gui.internal.model.VizMapperProxy;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMediator;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMenuMediator;
import org.puremvc.java.multicore.interfaces.INotification;
import org.puremvc.java.multicore.patterns.command.SimpleCommand;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

/**
 * Initializes the view. Register mediators and proxies.
 */
public class StartupCommand extends SimpleCommand {

	private final VizMapperProxy vizMapperProxy;
	private final AttributeSetProxy attributeSetProxy;
	private final MappingFunctionFactoryProxy mappingFactoryProxy;
	private final PropsProxy propsProxy;
	private final VizMapperMediator vizMapperMediator;
	private final VizMapperMenuMediator vizMapperMenuMediator;
	private final ServicesUtil servicesUtil;
	
	public StartupCommand(final VizMapperProxy vizMapperProxy,
						  final AttributeSetProxy attributeSetProxy,
						  final MappingFunctionFactoryProxy mappingFactoryProxy,
						  final PropsProxy propsProxy,
						  final VizMapperMediator vizMapperMediator,
						  final VizMapperMenuMediator vizMapperMenuMediator,
						  final ServicesUtil servicesUtil) {
		this.vizMapperProxy = vizMapperProxy;
		this.attributeSetProxy = attributeSetProxy;
		this.mappingFactoryProxy = mappingFactoryProxy;
		this.propsProxy = propsProxy;
		this.vizMapperMediator = vizMapperMediator;
		this.vizMapperMenuMediator = vizMapperMenuMediator;
		this.servicesUtil = servicesUtil;
	}

	@Override
	public final void execute(final INotification notification) {
		// Register proxies
		getFacade().registerProxy(vizMapperProxy);
		getFacade().registerProxy(attributeSetProxy);
		getFacade().registerProxy(mappingFactoryProxy);
		getFacade().registerProxy(propsProxy);
		
		// Register mediators
		getFacade().registerMediator(vizMapperMediator);
		getFacade().registerMediator(vizMapperMenuMediator);
		
		// Register other commands
		getFacade().registerCommand(LOAD_DEFAULT_VISUAL_STYLES, new ImportDefaultVisualStylesCommand(servicesUtil));
		getFacade().registerCommand(LOAD_VISUAL_STYLES, new LoadVisualStylesCommand(servicesUtil));
		getFacade().registerCommand(REMOVE_VISUAL_MAPPINGS, new RemoveVisualMappingsCommand(servicesUtil));
		getFacade().registerCommand(REMOVE_LOCKED_VALUES, new RemoveLockedValuesCommand(servicesUtil));
		getFacade().registerCommand(SET_LOCKED_VALUES, new SetLockedValuesCommand(servicesUtil));

		// Remove the STARTUP command because it is not called more than once
		getFacade().removeCommand(STARTUP);
	}
}
