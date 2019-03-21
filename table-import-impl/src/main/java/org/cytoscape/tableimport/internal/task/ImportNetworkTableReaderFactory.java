package org.cytoscape.tableimport.internal.task;

import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskIterator;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

public class ImportNetworkTableReaderFactory extends AbstractInputStreamTaskFactory {

    private final CyServiceRegistrar serviceRegistrar;

    public ImportNetworkTableReaderFactory(final CyFileFilter filter, final CyServiceRegistrar serviceRegistrar) {
        super(filter);
        this.serviceRegistrar = serviceRegistrar;
    }

    @Override
    public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
		String fileFormat = FilenameUtils.getExtension(inputName);

		if (!fileFormat.isEmpty())
			fileFormat = "." + fileFormat; // "." is surprisingly required somewhere within CombineNetworkReaderAndMappingTask

		return new TaskIterator(new CombineNetworkReaderAndMappingTask(inputStream, fileFormat, inputName, serviceRegistrar));
	}
}
