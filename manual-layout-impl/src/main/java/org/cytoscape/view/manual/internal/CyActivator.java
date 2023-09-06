package org.cytoscape.view.manual.internal;

import static org.cytoscape.view.manual.internal.util.Util.invokeOnEDTAndWait;
import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_EXAMPLE_JSON;
import static org.cytoscape.work.ServiceProperties.COMMAND_LONG_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.COMMAND_SUPPORTS_JSON;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.manual.internal.control.actions.ControlPanelAction;
import org.cytoscape.view.manual.internal.control.view.LayoutToolsPanel;
import org.cytoscape.view.manual.internal.tasks.RotateTaskFactory;
import org.cytoscape.view.manual.internal.tasks.ScaleTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Manual Layout Impl (manual-layout-impl)
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

public class CyActivator extends AbstractCyActivator {
	
	private LayoutToolsPanel layoutToolsPanel;
	private ControlPanelAction controlPanelAction;
	
	private static Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	@Override
	public void start(BundleContext bc) {
		final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		final CySwingApplication swingApplication = getService(bc, CySwingApplication.class);
		final CyApplicationManager applicationManager = getService(bc, CyApplicationManager.class);
		final CyNetworkViewManager netViewManager = getService(bc, CyNetworkViewManager.class);

		{
			var props = new Properties();
			props.setProperty(COMMAND, "scale");
			props.setProperty(COMMAND_NAMESPACE, "layout");
			props.setProperty(COMMAND_DESCRIPTION, "Scale the layout.");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Scale the layout in either the X, Y, or both directions");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{ }");
			registerService(bc, new ScaleTaskFactory(serviceRegistrar), TaskFactory.class, props);
		}

		{
			var props = new Properties();
			props.setProperty(COMMAND, "rotate");
			props.setProperty(COMMAND_NAMESPACE, "layout");
			props.setProperty(COMMAND_DESCRIPTION, "Rotate the layout.");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Rotate the layout");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{ }");
			registerService(bc, new RotateTaskFactory(serviceRegistrar), TaskFactory.class, props);
		}

		invokeOnEDTAndWait(() -> {
			layoutToolsPanel = new LayoutToolsPanel(serviceRegistrar);
			controlPanelAction = new ControlPanelAction(layoutToolsPanel, swingApplication, applicationManager, netViewManager);
		}, logger);
		
		registerAllServices(bc, controlPanelAction);
		registerAllServices(bc, layoutToolsPanel);
	}
}
