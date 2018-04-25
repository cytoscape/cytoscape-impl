package org.cytoscape.io.internal.read.cysession;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.io.BasicCyFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CysessionFileFilter extends BasicCyFileFilter {

	private static final Logger logger = LoggerFactory.getLogger("org.cytoscape.application.userlog");
	
	public CysessionFileFilter(Set<String> extensions, Set<String> contentTypes,
			String description, DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
	}

	public CysessionFileFilter(String[] extensions, String[] contentTypes,
			String description, DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
	}

	@Override
	public boolean accepts(InputStream stream, DataCategory category) {

		if (category != this.category) 
			return false;
		
		final String header = this.getHeader(stream,20);
		if(header.contains("<cysession"))
			return true;
		
		return false;
	}

	@Override
	public boolean accepts(URI uri, DataCategory category) {
		try (InputStream is = uri.toURL().openStream()) {
			return accepts(is, category);
		} catch (IOException e) {
			logger.error("Error while opening stream: " + uri, e);
			return false;
		}
	}
}
