package org.cytoscape.task.internal.export.table;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractTableTaskFactory;
import org.cytoscape.task.write.ExportTableTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

public class ExportTableTaskFactoryImpl extends AbstractTableTaskFactory implements ExportTableTaskFactory {

	CyTable table;
	
	private final CyServiceRegistrar serviceRegistrar;

	public ExportTableTaskFactoryImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator(final CyTable table) {
		return new TaskIterator(2, new CyTableWriter(table, serviceRegistrar));
	}
	
	@Override
	public TaskIterator createTaskIterator(final CyTable table, final File file) {
		this.table = table;
		
		Map<String, Object> m = new HashMap<>();
		m.put("OutputFile", file);
		
		var writer = new CyTableWriter(table, serviceRegistrar);
		writer.setDefaultFileFormatUsingFileExt(file);
		
		return serviceRegistrar.getService(TunableSetter.class).createTaskIterator(new TaskIterator(2, writer), m);
	}
}
