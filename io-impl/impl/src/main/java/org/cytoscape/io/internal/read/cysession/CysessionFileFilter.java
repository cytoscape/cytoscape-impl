package org.cytoscape.io.internal.read.cysession;

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

	private static final Logger logger = LoggerFactory.getLogger(CysessionFileFilter.class);
	
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
		try {
			return accepts(uri.toURL().openStream(), category);
		} catch (IOException e) {
			logger.error("Error while opening stream: " + uri, e);
			return false;
		}
	}
}
