package org.cytoscape.io.internal.read.gml;

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
import java.io.StreamTokenizer;
import java.net.URI;
import java.util.Set;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.util.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GMLFileFilter extends BasicCyFileFilter {

	private static final int DEFAULT_WORDS_TO_SAMPLE = 10;
	private StreamUtil streamUtil;

	public GMLFileFilter(Set<String> extensions, Set<String> contentTypes,
			String description, DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
		this.streamUtil = streamUtil;
	}

	public GMLFileFilter(String[] extensions, String[] contentTypes,
			String description, DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
		this.streamUtil = streamUtil;
	}

	@Override
	public boolean accepts(InputStream stream, DataCategory category) {
		if (!category.equals(DataCategory.NETWORK)) {
			return false;
		}
		
		StreamTokenizer tokenizer = GMLParser.createTokenizer(stream);
		try {
			try {
				int wordCount = 0;
				int type = tokenizer.nextToken();
				String lastWord = null;
				while (type != StreamTokenizer.TT_EOF && wordCount < DEFAULT_WORDS_TO_SAMPLE) {
					// Look for the token sequence { "graph", "[" }
					if (type == StreamTokenizer.TT_WORD && "[".equals(tokenizer.sval) && "graph".equals(lastWord)) {
						return true;
					}
					if (type == StreamTokenizer.TT_WORD) {
						lastWord = tokenizer.sval;
						wordCount++;
					}
					type = tokenizer.nextToken();
				}
				return false;
			} finally {
				stream.close();
			}
		} catch (IOException e) {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("Error parsing header", e);
			return false;
		}
	}
	
	@Override
	public boolean accepts(URI uri, DataCategory category) {
		if (!category.equals(DataCategory.NETWORK)) {
			return false;
		}
		try {
			return accepts(streamUtil.getInputStream(uri.toURL()), category);
		} catch (IOException e) {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("Error while reading header", e);
			return false;
		}
	}
}
