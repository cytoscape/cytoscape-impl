package org.cytoscape.io.internal.read.graphml;

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

	public GraphMLFileFilter(final String[] extensions, String[] contentTypes, String description,
			DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
	}

	@Override
	public boolean accepts(InputStream stream, DataCategory category) {

		// Check data category
		if (category != this.category)
			return false;

		final String header = this.getHeader(stream, 20);
		if (header.contains(GRAPHML_NAMESPACE_STRING))
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
