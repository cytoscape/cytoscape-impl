package org.cytoscape.io.internal.read.graphml;

/*
 * #%L
 * Cytoscape GraphML Impl (graphml-impl)
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.util.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphMLFileFilter extends BasicCyFileFilter {

	private static final Logger logger = LoggerFactory.getLogger(GraphMLFileFilter.class);

	private static final String GRAPHML_NAMESPACE_STRING = "http://graphml.graphdrawing.org/xmlns";
	private static final String GRAPHML_TAG = "<graphml>";

	public GraphMLFileFilter(final String[] extensions, String[] contentTypes, String description,
			DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
	}

	@Override
	public boolean accepts(InputStream stream, DataCategory category) {

		// Check data category
		if(category != this.category)
			return false;

		final String header = this.getHeader(stream, 20);
		if (header.contains(GRAPHML_NAMESPACE_STRING))
			return true;
		
		if(header.contains(GRAPHML_TAG))
			return true;

		return false;
	}

	@Override
	public boolean accepts(URI uri, DataCategory category) {
		try {
			if(super.accepts(uri, category))
				return accepts(uri.toURL().openStream(), category);
			else
				return false;
		} catch (IOException e) {
			logger.error("Error while opening stream: " + uri, e);
			return false;
		}
	}
}
