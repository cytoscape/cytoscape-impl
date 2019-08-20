package org.cytoscape.internal;

import java.awt.Desktop;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.SwingUtilities;

import org.cytoscape.application.CyShutdown;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.internal.view.help.AboutDialog;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

public class MacCyActivator extends AbstractCyActivator {
	
	private AboutDialog aboutDialog;
	
	@Override
	public void start(BundleContext context) throws Exception {
		final CyServiceRegistrar serviceRegistrar = getService(context, CyServiceRegistrar.class);
		final CyShutdown shutdown = getService(context, CyShutdown.class);
		
		final CyShutdownEvent[] lastShutdownEvent = new CyShutdownEvent[1];
		CyShutdownListener listener = evt -> {
			lastShutdownEvent[0] = evt;
		};
		registerService(context, listener, CyShutdownListener.class);
		
		Desktop desktop = Desktop.getDesktop();
		
		desktop.setQuitHandler((evt, response) -> {
			shutdown.exit(0);
			
			if (lastShutdownEvent[0] != null && !lastShutdownEvent[0].actuallyShutdown())
				response.cancelQuit();
		});
		desktop.setAboutHandler(evt -> {
			SwingUtilities.invokeLater(() -> {
				if (aboutDialog == null) { // Prevents more than one about dialog
					aboutDialog = new AboutDialog(serviceRegistrar);
					aboutDialog.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosed(WindowEvent e) {
							aboutDialog = null;
						}
					});
				}
				
				aboutDialog.pack();
				aboutDialog.setLocationRelativeTo(aboutDialog.getOwner());
				aboutDialog.setVisible(true);
			});
		});
	}
}
