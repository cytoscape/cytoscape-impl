package org.cytoscape.io.internal.write.datatable.csv;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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


import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.write.AbstractCyWriterFactory;
import org.cytoscape.io.write.CyTableWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyTable;


public class CSVTableWriterFactory extends AbstractCyWriterFactory implements CyTableWriterFactory {
	private final boolean writeSchema;
	private final boolean handleEquations;
	private final boolean includeVirtualColumns;

	public CSVTableWriterFactory(final CyFileFilter fileFilter, final boolean writeSchema,
				     final boolean handleEquations, final boolean includeVirtualColumns)
	{
		super(fileFilter);
		this.writeSchema     = writeSchema;
		this.handleEquations = handleEquations;
		this.includeVirtualColumns = includeVirtualColumns;
	}
	
	@Override
	public CyWriter createWriter(OutputStream outputStream, CyTable table) {
		return new CSVCyWriter(outputStream, table, writeSchema, handleEquations, includeVirtualColumns, "UTF-8");
	}
}
