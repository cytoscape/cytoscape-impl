package org.cytoscape.view.manual.internal;

import static org.cytoscape.view.manual.internal.Util.invokeOnEDTAndWait;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.manual.internal.control.ControlPanel;
import org.cytoscape.view.manual.internal.control.ControlPanelAction;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Manual Layout Impl (manual-layout-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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
	
	private ControlPanel controlPanel;
	private ControlPanelAction controlPanelAction;
	
	private static Logger logger = LoggerFactory.getLogger(CyActivator.class);
	
	@Override
	public void start(BundleContext bc) {
		final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		final CySwingApplication cySwingApplicationServiceRef = getService(bc, CySwingApplication.class);
		final CyApplicationManager cyApplicationManagerServiceRef = getService(bc, CyApplicationManager.class);
		final CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc, CyNetworkViewManager.class);

		invokeOnEDTAndWait(() -> {
			controlPanel = new ControlPanel(serviceRegistrar);
			controlPanelAction = new ControlPanelAction(controlPanel, cySwingApplicationServiceRef, cyApplicationManagerServiceRef, cyNetworkViewManagerServiceRef);
		}, logger);
		
		registerAllServices(bc, controlPanelAction, new Properties());
		registerAllServices(bc, controlPanel, new Properties());
	}
}
