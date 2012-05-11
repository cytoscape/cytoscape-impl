
package org.cytoscape.work.internal.sync;


import java.util.Map;

import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskFactory;


/**
 */
public class TunableSetterImpl implements TunableSetter {  

	private final SyncTunableMutator stm;
	private final TunableRecorderManager trm; 

	public TunableSetterImpl(SyncTunableMutator stm, TunableRecorderManager trm) {
		this.stm = stm;
		this.trm = trm;
	}

	public TaskIterator createTaskIterator(TaskIterator ti, Map<String,Object> tunableValues) {
		return new TaskIterator(ti.getNumTasks(), new DelegateTask(stm,trm,ti,tunableValues) );
	}
}

