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

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.TunableRecorder;

public class DelegateTask extends AbstractTask {

	private final SyncTunableMutator stm;
	private final TunableRecorderManager trm;
	private final TaskIterator ti;
	private final TaskObserver observer;
	private final Map<String, Object> tunableValues;

	private volatile Task currentTask = null;

	public DelegateTask(SyncTunableMutator stm, TunableRecorderManager trm, TaskIterator ti,
			Map<String, Object> tunableValues, TaskObserver observer) {
		this.stm = stm;
		this.trm = trm;
		this.ti = ti;
		this.tunableValues = tunableValues;
		this.observer = observer;
	}

	public void run(TaskMonitor tm) throws Exception {
		// this ensures that we get a coherent task monitor
		DelegatingTaskMonitor dtm = new DelegatingTaskMonitor(tm, ti.getNumTasks());

		// this gives the tunable mutator what it needs to set
		// the tunables as the tasks get executed
		stm.setConfigurationContext(tunableValues);

		while (ti.hasNext()) {
			final Task task = ti.next();
			dtm.setTask(task);

			if (!setTunables(task))
				return;

			currentTask = task;
			task.run(dtm);
			if (currentTask instanceof ObservableTask && observer != null) {
				observer.taskFinished((ObservableTask)currentTask);
			}
			currentTask = null;
		}
		// We don't call allfinished() since we're delegating...
	}

	@Override
	public void cancel() {
		cancelled = true;

		// Call delegated task's cancel method.
		if (currentTask != null) {
			currentTask.cancel();
		}
	}

	private boolean setTunables(final Object task) throws Exception {
		if (task == null)
			return true;

		boolean ret = stm.validateAndWriteBack(task);

		for (TunableRecorder ti : trm.getRecorders())
			ti.recordTunableState(task);

		return ret;
	}
}
