package org.cytoscape.io.internal.read.datatable;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Set;

import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.util.StreamUtil;

public class SessionTableFileFilter extends BasicCyFileFilter {

	public SessionTableFileFilter(Set<String> extensions, Set<String> contentTypes, String description, DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
	}
	
	public SessionTableFileFilter(String[] extensions, String[] contentTypes, String description, DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
	}
	
	@Override
	public boolean accepts(InputStream stream, DataCategory category) {
		if (category != DataCategory.TABLE) {
			return false;
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8").newDecoder()));
		try {
			// TODO: This is a really lame way to check for a CSV file
			String line = reader.readLine().trim();
			
			// Make sure we don't accept XML
			if (line.startsWith("<")) {
				return false;
			}
			return line.contains(",");
		} catch (IOException e) {
			return false;
		}
	}

}
