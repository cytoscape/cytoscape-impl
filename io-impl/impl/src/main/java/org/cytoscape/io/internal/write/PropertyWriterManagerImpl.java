package org.cytoscape.io.internal.write;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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



import org.cytoscape.io.write.CyPropertyWriterManager;
import org.cytoscape.io.write.CyPropertyWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.CyFileFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;


public final class PropertyWriterManagerImpl extends AbstractWriterManager<CyPropertyWriterFactory> 
	implements CyPropertyWriterManager
{
	public PropertyWriterManagerImpl() {
		super(DataCategory.PROPERTIES);		
	}

	public CyWriter getWriter(Object property, CyFileFilter filter, File outFile) throws Exception {
		return getWriter(property,filter,new FileOutputStream(outFile));
	}

	public CyWriter getWriter(Object property, CyFileFilter filter, OutputStream os) throws Exception {
		CyPropertyWriterFactory tf = getMatchingFactory(filter);
		if (tf == null)
			throw new NullPointerException("Couldn't find matching factory for filter: " + filter);
		return tf.createWriter(os,property);
	}
}

