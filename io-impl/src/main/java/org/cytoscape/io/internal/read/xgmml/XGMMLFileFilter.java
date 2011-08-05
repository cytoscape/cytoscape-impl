package org.cytoscape.io.internal.read.xgmml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.io.BasicCyFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XGMMLFileFilter extends BasicCyFileFilter {
	
	private static final String XGMML_NAMESPACE_STRING = "http://www.cs.rpi.edu/XGMML";
	private static final Logger logger = LoggerFactory.getLogger(XGMMLFileFilter.class);
	

	public XGMMLFileFilter(Set<String> extensions, Set<String> contentTypes,
			String description, DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
	}

	@Override
	public boolean accepts(InputStream stream, DataCategory category) {

		// Check data category
		if (category != this.category)
			return false;
		
		final String header = this.getHeader(stream,20);
		if(header.contains(XGMML_NAMESPACE_STRING))
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
