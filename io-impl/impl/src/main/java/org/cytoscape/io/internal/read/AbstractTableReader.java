package org.cytoscape.io.internal.read;

import java.io.InputStream;

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public abstract class AbstractTableReader extends AbstractTask implements CyTableReader {

	protected CyTable[] cyTables;
	
	protected final InputStream inputStream;
	protected final CyServiceRegistrar serviceRegistrar;

	public AbstractTableReader(final InputStream inputStream, final CyServiceRegistrar serviceRegistrar) {
		if (inputStream == null)
			throw new NullPointerException("InputStream is null");
		if (serviceRegistrar == null)
			throw new NullPointerException("CyServiceRegistrar is null");
		
		this.inputStream = inputStream;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public CyTable[] getTables() {
		return cyTables;
	}
}
