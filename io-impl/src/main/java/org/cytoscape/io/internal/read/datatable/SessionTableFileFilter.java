package org.cytoscape.io.internal.read.datatable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.util.StreamUtil;

public class SessionTableFileFilter extends BasicCyFileFilter {

	public SessionTableFileFilter(Set<String> extensions, Set<String> contentTypes, String description, DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
	}
	
	@Override
	public boolean accepts(InputStream stream, DataCategory category) {
		if (category != DataCategory.TABLE) {
			return false;
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
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
