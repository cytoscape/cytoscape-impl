package org.cytoscape.io.internal.write.graphics;

import java.io.OutputStream;
import java.util.Set;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.write.AbstractCyWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.PresentationWriterFactory;
import org.cytoscape.view.presentation.RenderingEngine;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

public class BitmapWriterFactory extends AbstractCyWriterFactory implements PresentationWriterFactory {

	public BitmapWriterFactory(final CyFileFilter bitmapFilter) {
		super(bitmapFilter);
	}

	@Override
	public CyWriter createWriter(OutputStream outputStream, RenderingEngine<?> re) {
		if (re == null)
			throw new NullPointerException("RenderingEngine is null");

		Set<String> contentTypes = getFileFilter().getContentTypes();
		
		if (contentTypes.size() == 1 && "image/png".equalsIgnoreCase(contentTypes.iterator().next()))
			return new PNGWriter(re, outputStream, getFileFilter().getExtensions());
		
		return new BitmapWriter(re, outputStream, getFileFilter().getExtensions());
	}
}
