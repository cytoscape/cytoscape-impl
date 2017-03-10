package org.cytoscape.ding.customgraphicsmgr.internal.action;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphicsmgr.internal.ui.CustomGraphicsBrowser;
import org.cytoscape.ding.customgraphicsmgr.internal.ui.CustomGraphicsManagerDialog;
import org.cytoscape.service.util.CyServiceRegistrar;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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
public class CustomGraphicsManagerAction extends AbstractCyAction {
	
	private final CustomGraphicsManager cgManager;
	private final CustomGraphicsBrowser browser;
	private final CyServiceRegistrar serviceRegistrar;

	public CustomGraphicsManagerAction(
			final CustomGraphicsManager cgManager,
	        final CustomGraphicsBrowser browser,
	        final CyServiceRegistrar serviceRegistrar
	) {
		super("Open Image Manager...");
		setPreferredMenu("View");
		setMenuGravity(10.0f);
		
		this.cgManager = cgManager;
		this.browser = browser;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
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
		
		final CustomGraphicsManagerDialog dialog =
				new CustomGraphicsManagerDialog(owner, cgManager, browser, serviceRegistrar);
		dialog.setVisible(true);
	}
}
