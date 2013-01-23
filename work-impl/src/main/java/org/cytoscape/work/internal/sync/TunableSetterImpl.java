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

