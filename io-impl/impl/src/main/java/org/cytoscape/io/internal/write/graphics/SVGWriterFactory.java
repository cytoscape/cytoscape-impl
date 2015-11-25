package org.cytoscape.io.internal.write.graphics;

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
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.PresentationWriterFactory;
import java.io.OutputStream;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.io.internal.write.AbstractCyWriterFactory;

public class SVGWriterFactory extends AbstractCyWriterFactory implements
		PresentationWriterFactory {

	public SVGWriterFactory(final CyFileFilter fileFilter) {
		super(fileFilter);
	}

	@Override
	public CyWriter createWriter(OutputStream outputStream, RenderingEngine<?> re) {
		if (re == null)
			throw new NullPointerException("RenderingEngine is null");

		return new SVGWriter(re, outputStream);
	}
}