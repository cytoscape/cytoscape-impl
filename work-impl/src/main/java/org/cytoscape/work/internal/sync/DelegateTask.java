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

import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Task;
import org.cytoscape.work.TunableRecorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DelegateTask extends AbstractTask {
	private final SyncTunableMutator stm;
	private final TunableRecorderManager trm; 
	private final TaskIterator ti;
	private final Map<String,Object> tunableValues;

	public DelegateTask(SyncTunableMutator stm, TunableRecorderManager trm, TaskIterator ti, Map<String,Object> tunableValues) {
		this.stm = stm;
		this.trm = trm;
		this.ti = ti;
		this.tunableValues = tunableValues;
	}

	public void run(TaskMonitor tm) throws Exception {
		// this ensures that we get a coherent task monitor
		DelegatingTaskMonitor dtm = new DelegatingTaskMonitor(tm,ti.getNumTasks());

		// this gives the tunable mutator what it needs to set
		// the tunables as the tasks get executed 
		stm.setConfigurationContext(tunableValues);
	
		while (ti.hasNext()) {
			final Task task = ti.next();
			dtm.setTask(task);

			if (!setTunables(task))
				return;

			task.run(dtm);
		}
	}

	private boolean setTunables(final Object task) throws Exception {
		if (task == null) 
			return true;
		
		boolean ret = stm.validateAndWriteBack(task);

		for ( TunableRecorder ti : trm.getRecorders() ) 
			ti.recordTunableState(task);

		return ret;
	}
}

