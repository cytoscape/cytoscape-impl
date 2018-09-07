package org.cytoscape.work.internal.view;

import static org.cytoscape.work.internal.tunables.utils.ViewUtil.invokeOnEDT;
import static org.cytoscape.work.internal.tunables.utils.ViewUtil.invokeOnEDTAndWait;

import java.util.Properties;

import org.cytoscape.application.events.CyStartEvent;
import org.cytoscape.application.events.CyStartListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.FinishStatus.Type;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.internal.task.TaskHistory;
import org.cytoscape.work.swing.StatusBarPanelFactory;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
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

public class TaskMediator implements CyStartListener {

	private TaskStatusBar taskStatusBar;
	private TaskHistoryWindow window;
	
	private final TaskHistory taskHistory;
	private final CyServiceRegistrar serviceRegistrar;

	public TaskMediator(TaskHistory taskHistory, CyServiceRegistrar serviceRegistrar) {
		this.taskHistory = taskHistory;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public void handleEvent(CyStartEvent e) {
		if (taskStatusBar == null) {
			// We have to initialize and register the status bar here,
			// after we know the correct Look And Feel has already been set
			invokeOnEDTAndWait(() -> taskStatusBar = new TaskStatusBar(serviceRegistrar));
			
			taskStatusBar.addPropertyChangeListener(TaskStatusBar.TASK_HISTORY_CLICK, evt -> {
				// Don't need to wrap this method in a SwingUtilities.invokeLater,
				// because it will only be called on the EDT anyway
				if (window == null) 
					window = new TaskHistoryWindow(taskHistory);
				
				window.open();
			});
			
			Properties props = new Properties();
			props.setProperty("type", "TaskStatus");
			serviceRegistrar.registerService(taskStatusBar, StatusBarPanelFactory.class, props);
		}
	}

	public void setTitle(Level level, String title) {
		if (taskStatusBar != null)
			invokeOnEDT(() -> taskStatusBar.setTitle(level, title));
	}

	public void setTitle(Type type, String title) {
		if (taskStatusBar != null)
			invokeOnEDT(() -> taskStatusBar.setTitle(type, title));
	}
}
