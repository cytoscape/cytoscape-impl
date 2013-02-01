package org.cytoscape.internal.layout.ui;

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
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.property.CyProperty;
import org.cytoscape.task.DynamicTaskFactoryProvisioner;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.swing.PanelTaskManager;

import java.awt.event.ActionEvent;


public class SettingsAction extends AbstractCyAction implements SetCurrentNetworkViewListener {
	private final static long serialVersionUID = 1202339874289357L;

	private CyLayoutAlgorithmManager cyl;
	private CySwingApplication desk;
	private PanelTaskManager tm;
	private CyProperty cytoscapePropertiesServiceRef;
	private CyApplicationManager appMgr;

	private LayoutSettingsDialog settingsDialog;

	public SettingsAction(final CyLayoutAlgorithmManager cyl, final CySwingApplication desk, final CyApplicationManager appMgr, final CyNetworkViewManager networkViewManager,
			final PanelTaskManager tm, CyProperty cytoscapePropertiesServiceRef, DynamicTaskFactoryProvisioner factoryProvisioner)
	{
		super("Settings...",appMgr,"networkAndView", networkViewManager);
		this.appMgr = appMgr;
		setPreferredMenu("Layout");
		setMenuGravity(3.0f);
		this.cyl = cyl;
		this.desk = desk;
		this.tm = tm;
		this.cytoscapePropertiesServiceRef = cytoscapePropertiesServiceRef;
		
		settingsDialog = new LayoutSettingsDialog(cyl, desk, appMgr, tm, this.cytoscapePropertiesServiceRef, factoryProvisioner );
	}

	public void actionPerformed(ActionEvent e) {
		settingsDialog.actionPerformed(e);
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		settingsDialog.setNetworkView(e.getNetworkView());
	}
}
