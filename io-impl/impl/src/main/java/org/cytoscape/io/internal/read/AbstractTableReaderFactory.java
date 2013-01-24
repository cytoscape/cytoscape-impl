package org.cytoscape.io.internal.read;

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


import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;


public abstract class AbstractTableReaderFactory extends AbstractInputStreamTaskFactory {
	protected final CyTableFactory tableFactory;

	public AbstractTableReaderFactory(final CyFileFilter filter,
	                                  final CyTableFactory tableFactory)
	{
		super(filter);
		if (filter == null)
			throw new NullPointerException("filter is null.");

		if (tableFactory == null)
			throw new NullPointerException("tableFactory is null.");
		this.tableFactory = tableFactory;

	}
}
