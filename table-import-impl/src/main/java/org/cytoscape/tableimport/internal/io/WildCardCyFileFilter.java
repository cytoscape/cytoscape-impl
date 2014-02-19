package org.cytoscape.tableimport.internal.io;

import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.util.StreamUtil;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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
public class WildCardCyFileFilter extends BasicCyFileFilter {

	private List<String> blackList = new ArrayList<String>();

	/**
	 * Creates a file filter from the specified arguments. Note that a "."
	 * before the extension is not needed and will be ignored.
	 *
	 * @param extensions   The set of valid extensions for this filter.
	 * @param contentTypes The set of valid MIME content types that this filter should
	 *                     recognize.
	 * @param description  A human readable description of the filter.
	 * @param category     The type of data this filter is meant to support.
	 * @param streamUtil   An instance of the StreamUtil service.
	 */
	public WildCardCyFileFilter(String[] extensions, String[] contentTypes, String description, DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
	}

	/**
	 * Creates a file filter from the specified arguments. Note that a "."
	 * before the extension is not needed and will be ignored.
	 *
	 * @param extensions   The set of valid extensions for this filter.
	 * @param contentTypes The set of valid MIME content types that this filter should
	 *                     recognize.
	 * @param description  A human readable description of the filter.
	 * @param category     The type of data this filter is meant to support.
	 * @param streamUtil   An instance of the StreamUtil service.
	 */
	public WildCardCyFileFilter(Set<String> extensions, Set<String> contentTypes, String description, DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
	}

	public void setBlacklist(String... extensions)
	{
		blackList = Arrays.asList(extensions);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean accepts(URI uri, DataCategory category) {
		// Check data category
		if (category != this.category)
			return false;
		return extensionsMatch(uri);
	}

	private boolean extensionsMatch(URI uri) {
		final String extension = getExtension(uri.toString());
		if(blackList.contains(extension))
			return false;

		return extensions.contains("") || extensions.contains(extension);
	}
}
