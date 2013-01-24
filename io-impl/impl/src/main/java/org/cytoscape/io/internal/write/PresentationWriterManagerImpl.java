package org.cytoscape.io.internal.write;

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



import org.cytoscape.io.write.PresentationWriterManager;
import org.cytoscape.io.write.PresentationWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.CyFileFilter;
//import org.cytoscape.task.internal.io.ViewWriter;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.RenderingEngine;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;


public class PresentationWriterManagerImpl extends AbstractWriterManager<PresentationWriterFactory> 
	implements PresentationWriterManager {

	public PresentationWriterManagerImpl() {
		super(DataCategory.IMAGE);
	}

	public CyWriter getWriter(View<?> view, RenderingEngine<?> re, CyFileFilter filter, File outFile) throws Exception {
		return getWriter(view,re,filter,new FileOutputStream(outFile));
	}

	public CyWriter getWriter(View<?> view, RenderingEngine<?> re, CyFileFilter filter, OutputStream os) throws Exception {
		PresentationWriterFactory tf = getMatchingFactory(filter);
		if ( tf == null )
			throw new NullPointerException("Couldn't find matching factory for filter: " + filter);
		return tf.createWriter(os,re);
	}
}
