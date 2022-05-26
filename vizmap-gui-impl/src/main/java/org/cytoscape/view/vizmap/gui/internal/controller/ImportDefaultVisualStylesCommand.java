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
 * Import the default visual styles into the proxy.
 */
public class ImportDefaultVisualStylesCommand extends SimpleCommand {

	private final ServicesUtil servicesUtil;
	
	public ImportDefaultVisualStylesCommand(ServicesUtil servicesUtil) {
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
				proxy.setCurrentNetworkVisualStyle(servicesUtil.get(VisualMappingManager.class).getDefaultVisualStyle());
			}
		});
	}
}
