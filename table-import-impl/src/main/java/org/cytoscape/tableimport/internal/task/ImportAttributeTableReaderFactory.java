package org.cytoscape.tableimport.internal.task;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.service.util.CyServiceRegistrar;
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

public class ImportAttributeTableReaderFactory extends AbstractTableReaderFactory {
	
	private final TableImportContext tableImportContext;

	/**
	 * Creates a new ImportAttributeTableReaderFactory object.
	 */
	public ImportAttributeTableReaderFactory(
			CyFileFilter filter,
			TableImportContext tableImportContext,
			CyServiceRegistrar serviceRegistrar
	) {
		super(filter, serviceRegistrar);
		this.tableImportContext = tableImportContext;
	}

	@Override
	public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
		int lastIndex = inputName.lastIndexOf('.');
		String fileFormat = lastIndex == -1 ? "" : inputName.substring(lastIndex);
		
		return new TaskIterator(new ImportAttributeTableReaderTask(inputStream, fileFormat, inputName,
				tableImportContext, serviceRegistrar));
	}
}
