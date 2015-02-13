package org.cytoscape.tableimport.internal;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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


import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.tableimport.internal.ui.theme.IconManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;


public class LoadNoGuiTableReaderFactory extends AbstractTaskFactory {
	
	protected final StreamUtil streamUtil;
	private boolean fromURL;
	private final CyTableManager tableMgr;
	private final IconManager iconManager;

	/**
	 * Creates a new ImportAttributeTableReaderFactory object.
	 */
	public LoadNoGuiTableReaderFactory(final StreamUtil streamUtil,final CyTableManager tableMgr, boolean fromURL,
			final IconManager iconManager) {
		this.streamUtil = streamUtil;
		this.fromURL = fromURL;
		this.tableMgr = tableMgr;
		this.iconManager = iconManager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		LoadTableReaderTask readerTask = new LoadTableReaderTask(iconManager);
		
		if (fromURL) {
			return new TaskIterator(new SelectURLTableTask(readerTask, streamUtil, iconManager),
					readerTask, new AddLoadedTableTask(readerTask));
		} else {
			return new TaskIterator(new SelectFileTableTask(readerTask, streamUtil, iconManager),
					readerTask, new AddLoadedTableTask(readerTask));
		}
	}
	
	class AddLoadedTableTask extends AbstractTask {
		
		private final CyTableReader reader;
		
		AddLoadedTableTask(final CyTableReader reader){
			this.reader = reader;
		}
		
		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			if (this.reader != null && this.reader.getTables() != null)
				for (CyTable table : reader.getTables())
					tableMgr.addTable(table);
		}
	}
}
