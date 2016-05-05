package org.cytoscape.task.internal.table;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.AbstractTableTaskFactory;
import org.cytoscape.task.edit.MapGlobalToLocalTableTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

public final class MapGlobalToLocalTableTaskFactoryImpl extends AbstractTableTaskFactory implements MapGlobalToLocalTableTaskFactory {
	
	private final CyTableManager tableManager;
	private final CyNetworkManager networkManager;
	
	private final TunableSetter tunableSetter; 

	
	public MapGlobalToLocalTableTaskFactoryImpl(final CyTableManager tableManager, final CyNetworkManager networkManager, TunableSetter tunableSetter) {
		this.tableManager = tableManager;
		this.networkManager = networkManager;
		this.tunableSetter = tunableSetter;
	}


	@Override
	public TaskIterator createTaskIterator(CyTable globalTable) {
		return new TaskIterator(new MapGlobalToLocalTableTask(globalTable, tableManager, networkManager) );
	}

	@Override
	public TaskIterator createTaskIterator(CyTable globalTable,
			Collection<CyTable> localTables) {
		final Map<String, Object> m = new HashMap<>();

		List<String> localTableTitles = new ArrayList<>();
		for(CyTable local: localTables){
			localTableTitles.add(local.getTitle());
		}
		
		ListMultipleSelection<String> localTablesTunable = new ListMultipleSelection<>(localTableTitles);
		localTablesTunable.setSelectedValues(localTableTitles);

		m.put("localTables", localTablesTunable);

		return tunableSetter.createTaskIterator(this.createTaskIterator(globalTable), m); 

	}
}
