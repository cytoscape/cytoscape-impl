package org.cytoscape.io.internal.read.gml;

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
