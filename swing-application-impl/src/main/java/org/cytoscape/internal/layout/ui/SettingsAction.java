package org.cytoscape.internal.layout.ui;

import static org.cytoscape.internal.util.ViewUtil.invokeOnEDT;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.ActionEnableSupport;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkViewManager;
/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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

@SuppressWarnings("serial")
public class SettingsAction extends AbstractCyAction implements SetCurrentNetworkViewListener {
	
	private LayoutSettingsDialog settingsDialog;
	
	private final LayoutSettingsManager layoutSettingsMgr;
	private final CyServiceRegistrar serviceRegistrar;
	
	public SettingsAction(final LayoutSettingsManager layoutSettingsMgr, final CyServiceRegistrar serviceRegistrar) {
		super(
				"Settings...",
				serviceRegistrar.getService(CyApplicationManager.class),
				ActionEnableSupport.ENABLE_FOR_ALWAYS,
				serviceRegistrar.getService(CyNetworkViewManager.class)
		);
		this.layoutSettingsMgr = layoutSettingsMgr;
		this.serviceRegistrar = serviceRegistrar;
		
		setPreferredMenu("Layout");
		setMenuGravity(3.0f);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (settingsDialog == null) {
			final CySwingApplication swingApplication = serviceRegistrar.getService(CySwingApplication.class);
			Window owner = null;
			
			if (evt.getSource() instanceof JMenuItem) {
				if (swingApplication.getJMenuBar() != null)
					owner = SwingUtilities.getWindowAncestor(swingApplication.getJMenuBar());
			} else if (evt.getSource() instanceof Component) {
				owner = SwingUtilities.getWindowAncestor((Component) evt.getSource());
			}
			
			if (owner == null)
				owner = swingApplication.getJFrame();
			
			settingsDialog = new LayoutSettingsDialog(owner, layoutSettingsMgr, serviceRegistrar);
			
			settingsDialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					settingsDialog = null;
				}
			});
		}
		
		settingsDialog.actionPerformed(evt);
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		if (settingsDialog != null) {
			invokeOnEDT(() -> {
				settingsDialog.setNetworkView(e.getNetworkView());
			});
		}
	}
	
    public void addLayout(final CyLayoutAlgorithm layout, Map<?, ?> props) {
    	if (settingsDialog != null) {
			invokeOnEDT(() -> {
				settingsDialog.addLayout(layout);
			});
		}
    }
    
    public void removeLayout(final CyLayoutAlgorithm layout, Map<?, ?> props) {
    	if (settingsDialog != null) {
			invokeOnEDT(() -> {
				settingsDialog.removeLayout(layout);
			});
		}
    }
}
