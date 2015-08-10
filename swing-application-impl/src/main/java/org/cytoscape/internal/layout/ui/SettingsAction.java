package org.cytoscape.internal.layout.ui;

import java.awt.event.ActionEvent;
import java.util.Map;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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


import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.DynamicTaskFactoryProvisioner;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.properties.TunablePropertySerializerFactory;
import org.cytoscape.work.swing.PanelTaskManager;


public class SettingsAction extends AbstractCyAction implements SetCurrentNetworkViewListener {
	
	private final static long serialVersionUID = 1202339874289357L;


	private LayoutSettingsDialog settingsDialog;

	public SettingsAction(
			final CyLayoutAlgorithmManager cyl,
			final CySwingApplication desk,
			final CyApplicationManager appMgr,
			final CyServiceRegistrar serviceRegistrar,
			final TunablePropertySerializerFactory serializerFactory,
			final CyNetworkViewManager networkViewManager,
			final PanelTaskManager tm,
			final CyProperty cytoscapePropertiesServiceRef,
			final DynamicTaskFactoryProvisioner factoryProvisioner
	) {
		super("Settings...", appMgr, "networkAndView", networkViewManager);
		setPreferredMenu("Layout");
		setMenuGravity(3.0f);
		
		settingsDialog = new LayoutSettingsDialog(cyl, desk, appMgr, serviceRegistrar, serializerFactory, tm, cytoscapePropertiesServiceRef, factoryProvisioner);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		settingsDialog.actionPerformed(e);
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		settingsDialog.setNetworkView(e.getNetworkView());
	}
	
    public void addLayout(final CyLayoutAlgorithm layout, Map props) {
    	settingsDialog.addLayout(layout);
    }
    
    public void removeLayout(final CyLayoutAlgorithm layout, Map props) {
    	settingsDialog.removeLayout(layout);
    }
}
