package org.cytoscape.io.internal.read.datatable;

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


import java.io.InputStream;

import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.work.TaskIterator;


public class CSVCyReaderFactory extends AbstractInputStreamTaskFactory {
	private final boolean readSchema;
	private final boolean handleEquations;
	private final CyTableFactory tableFactory;
	private final EquationCompiler compiler;

	public CSVCyReaderFactory(final CyFileFilter filter, final boolean readSchema,
				  final boolean handleEquations, final CyTableFactory tableFactory,
				  final EquationCompiler compiler)
	{
		super(filter);
		this.readSchema      = readSchema;
		this.handleEquations = handleEquations;
		this.tableFactory    = tableFactory;
		this.compiler        = compiler;
	}
	
	@Override
	public TaskIterator createTaskIterator(InputStream stream, String inputName) {
		return new TaskIterator(new CSVCyReader(stream, readSchema, handleEquations, tableFactory, compiler, "UTF-8"));
	}
}
