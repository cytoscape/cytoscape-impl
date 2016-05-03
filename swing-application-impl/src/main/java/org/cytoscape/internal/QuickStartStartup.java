package org.cytoscape.internal;

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


import javax.swing.SwingUtilities;

import org.cytoscape.application.events.CyStartEvent;
import org.cytoscape.application.events.CyStartListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;

/*
 * This class listens CytoscapeStartEvent and pop up QuickStart dialog
 */
public class QuickStartStartup implements CyStartListener {

	private final TaskFactory quickStartTaskFactory;
	private final DialogTaskManager guiTaskManager;
	private final CySwingApplication swingApp;

	public QuickStartStartup(final TaskFactory quickStartTaskFactory, final DialogTaskManager guiTaskManager,
			final CySwingApplication swingApp) {
		this.quickStartTaskFactory = quickStartTaskFactory;
		this.guiTaskManager = guiTaskManager;
		this.swingApp = swingApp;
	}

	
	@Override
	public void handleEvent(CyStartEvent e) {
		
		SwingUtilities.invokeLater(() -> {
            guiTaskManager.setExecutionContext(swingApp.getJFrame());
            guiTaskManager.execute(quickStartTaskFactory.createTaskIterator());
        });
	}
}
