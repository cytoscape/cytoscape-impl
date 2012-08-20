
package org.cytoscape.work.internal.sync;


import java.util.Map;

import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;


/**
 */
public class TunableSetterImpl implements TunableSetter {  

	private final TunableRecorderManager trm;
	private SyncTunableMutatorFactory mutatorFactory; 

	public TunableSetterImpl(SyncTunableMutatorFactory mutatorFactory, TunableRecorderManager trm) {
		this.mutatorFactory = mutatorFactory;
		this.trm = trm;
	}

	public TaskIterator createTaskIterator(TaskIterator ti, Map<String,Object> tunableValues) {
		return new TaskIterator(ti.getNumTasks(), new DelegateTask(mutatorFactory.createMutator(),trm,ti,tunableValues) );
	}
	
	@Override
	public void applyTunables(Object object, Map<String, Object> tunableValues) {
		SyncTunableMutator<?> mutator = mutatorFactory.createMutator();
		mutator.setConfigurationContext(tunableValues);
		mutator.validateAndWriteBack(object);
	}
}

