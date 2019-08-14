package org.cytoscape.internal.view.help;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

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

public class HelpTourTask extends AbstractTask {
	
	private static final String TOUR_URL = "https://cytoscape.org/cytoscape-tutorials/protocols/tour-of-cytoscape/#/";
	
	private final CyServiceRegistrar serviceRegistrar;

	public HelpTourTask(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) {
		final OpenBrowser openBrowser = serviceRegistrar.getService(OpenBrowser.class);
		openBrowser.openURL(TOUR_URL);
	}

	@Override
	public void cancel() {
	}
}
