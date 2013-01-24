package org.cytoscape.io.internal.read.xgmml;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.util.session.SessionUtil;
import org.cytoscape.io.util.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericXGMMLFileFilter extends BasicCyFileFilter {
	
	public static final Pattern XGMML_HEADER_PATTERN = Pattern
			.compile("<graph[^<>]+[\\'\"]http://www.cs.rpi.edu/XGMML[\\'\"][^<>]*>");
	
	private static final Logger logger = LoggerFactory.getLogger(GenericXGMMLFileFilter.class);
	

	public GenericXGMMLFileFilter(Set<String> extensions, Set<String> contentTypes,
			String description, DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
	}

	public GenericXGMMLFileFilter(String[] extensions, String[] contentTypes,
			String description, DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
	}

	@Override
	public boolean accepts(final InputStream stream, final DataCategory category) {
		if (category != this.category || SessionUtil.isReadingSessionFile())
			return false;
		
		return getXGMMLRootElement(stream) != null;
	}

	@Override
	public boolean accepts(final URI uri, final DataCategory category) {
		try {
			return accepts(uri.toURL().openStream(), category);
		} catch (IOException e) {
			logger.error("Error while opening stream: " + uri, e);
			return false;
		}
	}
	
	/**
	 * @param stream
	 * @return null if not an XGMML file
	 */
	protected String getXGMMLRootElement(final InputStream stream) {
		final String header = this.getHeader(stream, 20);
		final Matcher matcher = XGMML_HEADER_PATTERN.matcher(header);
		String root = null;
		
		if (matcher.find())
			root = matcher.group(0);
		
		return root;
	}
}
