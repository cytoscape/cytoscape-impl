package org.cytoscape.tableimport.internal.task;

import java.io.File;

import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedEvent;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.task.ImportTableDataTask.TableType;
import org.cytoscape.task.read.LoadTableFileTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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

public class LoadTableFileTaskFactoryImpl extends AbstractTaskFactory
		implements LoadTableFileTaskFactory, CytoPanelComponentSelectedListener {

	private final TableImportContext tableImportContext;
	private final CyServiceRegistrar serviceRegistrar;

	public LoadTableFileTaskFactoryImpl(TableImportContext tableImportContext, CyServiceRegistrar serviceRegistrar) {
		this.tableImportContext = tableImportContext;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(2, new LoadTableFileTask(tableImportContext, serviceRegistrar));
	}

	@Override
	public TaskIterator createTaskIterator(final File file) {
		final CyTableReaderManager tableReaderMgr = serviceRegistrar.getService(CyTableReaderManager.class);
		final CyTableReader reader = tableReaderMgr.getReader(file.toURI(), file.toURI().toString());

		return new TaskIterator(new CombineTableReaderAndMappingTask(reader, tableImportContext, serviceRegistrar));
	}

	@Override
	public void handleEvent(CytoPanelComponentSelectedEvent evt) {
		CytoPanel cytoPanel = evt.getCytoPanel();
		int idx = evt.getSelectedIndex();
		
		if (cytoPanel.getCytoPanelName() != CytoPanelName.SOUTH || idx < 0 || idx >= cytoPanel.getCytoPanelComponentCount())
			return;
		
		if (idx == cytoPanel.indexOfComponent("org.cytoscape.NodeTables"))
			tableImportContext.setTableType(TableType.NODE_ATTR);
		else if (idx == cytoPanel.indexOfComponent("org.cytoscape.EdgeTables"))
			tableImportContext.setTableType(TableType.EDGE_ATTR);
		else if (idx == cytoPanel.indexOfComponent("org.cytoscape.NetworkTables"))
			tableImportContext.setTableType(TableType.NETWORK_ATTR);
		else if (idx == cytoPanel.indexOfComponent("org.cytoscape.UnassignedTables"))
			tableImportContext.setTableType(null);
	}
}
