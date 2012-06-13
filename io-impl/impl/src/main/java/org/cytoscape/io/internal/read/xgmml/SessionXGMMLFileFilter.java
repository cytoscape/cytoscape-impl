package org.cytoscape.io.internal.read.xgmml;

import java.io.InputStream;
import java.util.Set;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.util.session.SessionUtil;
import org.cytoscape.io.util.StreamUtil;

public class SessionXGMMLFileFilter extends GenericXGMMLFileFilter {
	
	public SessionXGMMLFileFilter(Set<String> extensions, Set<String> contentTypes,
			String description, DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
	}

	public SessionXGMMLFileFilter(String[] extensions, String[] contentTypes,
			String description, DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
	}
	
	@Override
	public boolean accepts(InputStream stream, DataCategory category) {
		if (category != this.category || !SessionUtil.isReadingSessionFile())
			return false;
		
		return getXGMMLRootElement(stream) != null;
	}
}
