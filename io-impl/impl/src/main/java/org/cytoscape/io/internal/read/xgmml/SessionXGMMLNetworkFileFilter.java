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

import static org.cytoscape.io.internal.read.xgmml.SessionXGMMLNetworkViewFileFilter.SESSION_XGMML_VIEW_PATTERN;

import java.io.InputStream;
import java.util.Set;
import java.util.regex.Matcher;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.util.session.SessionUtil;
import org.cytoscape.io.util.StreamUtil;

/**
 * Filters XGMML files that are used to save CyNetworks as part of a session file. 
 */
public class SessionXGMMLNetworkFileFilter extends GenericXGMMLFileFilter {

	public SessionXGMMLNetworkFileFilter(Set<String> extensions, Set<String> contentTypes,
			String description, DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
	}

	public SessionXGMMLNetworkFileFilter(String[] extensions, String[] contentTypes,
			String description, DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
	}
	
	@Override
	public boolean accepts(InputStream stream, DataCategory category) {
		if (category != this.category || !SessionUtil.isReadingSessionFile())
			return false;
		
		final String root = getXGMMLRootElement(stream);
		
		if (root != null) {
			// It looks like an XGMML file, but it cannot have the 'cy:view="1"' flag,
			// which would mean it's a Cy3 CyNetworkView file
			final Matcher matcher = SESSION_XGMML_VIEW_PATTERN.matcher(root);
			
			return !matcher.find();
		}
		
		return false;
	}
}
