package org.cytoscape.io.internal.read.xgmml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.util.ReadCache;
import org.cytoscape.io.util.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class GenericXGMMLFileFilter extends BasicCyFileFilter {
	
	public static final Pattern XGMML_HEADER_PATTERN = Pattern
			.compile("<graph[\\s]+[^<>]*[\\'\"]http://www.cs.rpi.edu/XGMML[\\'\"][^<>]*>|"     // XGMML namespace
					+ "<!DOCTYPE[\\s]+graph[\\s]+[^<>]*[\\'\"][^<>]*xgmml.dtd[\\'\"][^<>]*>"); // or XGMML DTD
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	protected final ReadCache cache;
	
	public GenericXGMMLFileFilter(
			Set<String> extensions,
			Set<String> contentTypes,
			String description,
			DataCategory category,
			ReadCache cache,
			StreamUtil streamUtil
	) {
		super(extensions, contentTypes, description, category, streamUtil);
		this.cache = cache;
	}

	public GenericXGMMLFileFilter(
			String[] extensions,
			String[] contentTypes,
			String description,
			DataCategory category,
			ReadCache cache,
			StreamUtil streamUtil
	) {
		super(extensions, contentTypes, description, category, streamUtil);
		this.cache = cache;
	}

	@Override
	public boolean accepts(InputStream stream, DataCategory category) {
		if (category != this.category || cache.isReadingSessionFile())
			return false;
		
		return getXGMMLRootElement(stream) != null;
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
	
	/**
	 * @param stream
	 * @return null if not an XGMML file
	 */
	protected String getXGMMLRootElement(InputStream stream) {
		final String header = this.getHeader(stream, 20);
		final Matcher matcher = XGMML_HEADER_PATTERN.matcher(header);
		String root = null;
		
		if (matcher.find())
			root = matcher.group(0);
		
		return root;
	}
}
