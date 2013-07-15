package org.cytoscape.work.internal.sync;

/*
 * #%L
 * org.cytoscape.work-impl
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


import java.util.Map;

import org.cytoscape.work.AbstractTaskManager;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Uses Swing components to create a user interface for the <code>Task</code>.
 *
 * This will not work if the application is running in headless mode.
 */
public class SyncTaskManager extends AbstractTaskManager<Object, Map<String, Object>> implements
		SynchronousTaskManager<Object> {

	private static final Logger logger = LoggerFactory.getLogger(SyncTaskManager.class);


	private final SyncTunableMutator<?> syncTunableMutator;

	/**
	 * Construct with default behavior.
	 */
	public SyncTaskManager(final SyncTunableMutator<?> tunableMutator) {
		super(tunableMutator);
		this.syncTunableMutator = tunableMutator;
	}


	@Override 
	public void setExecutionContext(final Map<String,Object> o) {
		syncTunableMutator.setConfigurationContext(o);
	}


	@Override 
	public Object getConfiguration(TaskFactory factory, Object context) {
		throw new UnsupportedOperationException("There is no configuration available for a SyncrhonousTaskManager");	
	}


	@Override
	public void execute(final TaskIterator taskIterator) {
		// System.out.println("SyncTaskManager.execute");
		final LoggingTaskMonitor taskMonitor = new LoggingTaskMonitor();
		
		try {
			while (taskIterator.hasNext()) {
				final Task task = taskIterator.next();
				taskMonitor.setTask(task);

				if (!displayTunables(task))
					return;

				task.run(taskMonitor);
			}

		} catch (Exception exception) {
			taskMonitor.showException(exception);
		}
	}

	private boolean displayTunables(final Object task) throws Exception {
		if (task == null) {
			return true;
		}
		boolean ret = syncTunableMutator.validateAndWriteBack(task);

		for ( TunableRecorder ti : tunableRecorders ) 
			ti.recordTunableState(task);

		return ret;
	}
}

