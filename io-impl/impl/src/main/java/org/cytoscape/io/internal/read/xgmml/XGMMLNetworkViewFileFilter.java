/*
 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.io.internal.read.xgmml;

import java.io.InputStream;
import java.util.Set;
import java.util.regex.Matcher;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.util.StreamUtil;

/**
 * Filters XGMML files that are used to save CyNetworkViews as part of a session file. 
 */
public class XGMMLNetworkViewFileFilter extends XGMMLFileFilter {

	public XGMMLNetworkViewFileFilter(Set<String> extensions, Set<String> contentTypes,
			String description, DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
	}

	public XGMMLNetworkViewFileFilter(String[] extensions, String[] contentTypes,
			String description, DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
	}
	
	@Override
	public boolean accepts(InputStream stream, DataCategory category) {
		// Check data category
		if (category != this.category)
			return false;
		
		final String header = this.getHeader(stream, 20);
		Matcher matcher = XGMML_HEADER_PATTERN.matcher(header);
		
		if (matcher.find()) {
			// It looks like an XGMML graph tag
			final String graph = matcher.group(0);
			
			// But it also needs to have the 'cy:view="1"' flag:
			matcher = XGMML_VIEW_ATTRIBUTE_PATTERN.matcher(graph);
			
			return (matcher.find());
		}
		
		return false;
	}
}
