package org.cytoscape.psi_mi.internal.plugin;

import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.DataCategory;

public class PsiMiTabCyFileFilter implements CyFileFilter {

	private final Set<String> extensions;
	private final String description;
	private final Set<String> contentTypes;

	public PsiMiTabCyFileFilter() {
		extensions = new HashSet<String>();
		extensions.add("mitab");
		
		contentTypes = new HashSet<String>();
		contentTypes.add("text/psi-mi-tab");

		this.description = "PSI-MI TAB file";
	}

	@Override
	public boolean accepts(URI uri, DataCategory category) {
		if (!category.equals(DataCategory.NETWORK))
			return false;
		
		if(extensions.contains(getExtension(uri.toString())))
			return true;
		else
			return false;
	}


	@Override
	public boolean accepts(InputStream stream, DataCategory category) {
		// Not supported.
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
