package org.cytoscape.psi_mi.internal.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.DataCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PsiMiTabCyFileFilter implements CyFileFilter {

	private final Set<String> extensions;
	private final String description;
	private final Set<String> contentTypes;

	public PsiMiTabCyFileFilter() {
		extensions = new HashSet<String>();
		extensions.add("mitab");
		
		contentTypes = new HashSet<String>();
		contentTypes.add("text/psi-mi-tab");

		this.description = "PSI-MI TAB 2.5 file";
	}

	@Override
	public boolean accepts(URI uri, DataCategory category) {
		if (!category.equals(DataCategory.NETWORK))
			return false;
		
		final String ext = getExtension(uri.toString());
		if(extensions.contains(ext))
			return true;
		else
			return false;
	}


	@Override
	public boolean accepts(InputStream stream, DataCategory category) {
		if (!category.equals(DataCategory.NETWORK)) {
			return false;
		}
		try {
			return checkFirstLine(stream);
		} catch (IOException e) {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("Error while checking header", e);
			return false;
		}
	}
	
	private boolean checkFirstLine(InputStream stream) throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		final String line = reader.readLine();
		
		if (line != null) {
			String[] parts = line.split("\t");
			if(parts.length >= 15)
				return true;
			else {
				return false;
			}
		}
		return false;
	}
	
	private String getExtension(String filename) {
		if (filename != null) {
			int i = filename.lastIndexOf('.');
			if ((i > 0) && (i < (filename.length() - 1))) {
				return filename.substring(i + 1).toLowerCase();
			}
		}

		return null;
	}

	@Override
	public Set<String> getExtensions() {
		return extensions;
	}

	@Override
	public Set<String> getContentTypes() {
		return contentTypes;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public DataCategory getDataCategory() {
		return DataCategory.NETWORK;
	}
}
