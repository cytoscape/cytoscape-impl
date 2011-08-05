package org.cytoscape.webservice.ncbi;

import java.util.Set;

import org.cytoscape.webservice.ncbi.ui.AnnotationCategory;

public class NCBIQuery {

	final Set<AnnotationCategory> category;
	final Set<String> ids;
	
	public NCBIQuery(final Set<AnnotationCategory> category, final Set<String> ids) {
		this.category = category;
		this.ids = ids;
	}
	
	public Set<AnnotationCategory> getCategory() {
		return this.category;
	}
	
	public Set<String> getIds() {
		return this.ids;
	}
}
