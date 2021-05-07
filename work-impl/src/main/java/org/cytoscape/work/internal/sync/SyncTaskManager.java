package org.cytoscape.work.internal.sync;

import java.util.Map;

import org.cytoscape.work.AbstractTaskManager;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.TunableRecorder;

/*
 * #%L
 * org.cytoscape.work-impl
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

/**
 * Uses Swing components to create a user interface for the <code>Task</code>.
 *
 * This will not work if the application is running in headless mode.
 */
public class SyncTaskManager extends AbstractTaskManager<Object, Map<String, Object>> implements
		SynchronousTaskManager<Object> {

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
	public void execute(final TaskIterator taskIterator, TaskObserver observer) {
		// System.out.println("SyncTaskManager.execute");
		final LoggingTaskMonitor taskMonitor = new LoggingTaskMonitor();
		
        Task task = null;
		try {
			while (taskIterator.hasNext()) {
				task = taskIterator.next();
				taskMonitor.setTask(task);

				if (!displayTunables(task)) {
                    if (observer != null) observer.allFinished(FinishStatus.newCancelled(task));
					return;
                }

				task.run(taskMonitor);

				if (task instanceof ObservableTask && observer != null) {
					observer.taskFinished((ObservableTask)task);
				} 
			}
            if (observer != null) observer.allFinished(FinishStatus.getSucceeded());

		} catch (Exception exception) {
			taskMonitor.showException(exception);
            if (observer != null && task != null) observer.allFinished(FinishStatus.newFailed(task, exception));
		}
	}

	@Override
	public void execute(final TaskIterator taskIterator) {
		execute(taskIterator, null);
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

