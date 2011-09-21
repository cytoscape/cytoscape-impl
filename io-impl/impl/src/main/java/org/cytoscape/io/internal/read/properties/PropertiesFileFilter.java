package org.cytoscape.io.internal.read.properties;

import java.io.InputStream;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.util.StreamUtil;

public class PropertiesFileFilter extends BasicCyFileFilter {

	protected Matcher matcher;

	public PropertiesFileFilter(Set<String> extensions, Set<String> contentTypes,
			String description, DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);

		Pattern p = Pattern.compile("^.+=.+$", Pattern.DOTALL);
	 	matcher = p.matcher("");
	}

	public PropertiesFileFilter(String[] extensions, String[] contentTypes,
			String description, DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);

		Pattern p = Pattern.compile("^.+=.+$", Pattern.DOTALL);
	 	matcher = p.matcher("");
	}

	@Override
	public boolean accepts(InputStream stream, DataCategory category) {

		// Check data category
		if (category != this.category)
			return false;
		
		final String header = this.getHeader(stream,20);

		// These two tests are so that we don't mistakenly accept a cysession 
		// or bookmarks file, which might otherwise match the pattern above.
		if (header.contains("<cysession") && header.contains("xmlns"))
			return false;
		if (header.contains("<bookmarks") && header.contains("xmlns"))
			return false;

		// This is the real test to see if we're a .props file
		// TODO can we find a better test?
		if(matcher.reset(header).matches()) 
			return true;
		
		return false;
	}
}
