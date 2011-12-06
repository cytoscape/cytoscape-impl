package org.cytoscape.webservice.psicquic;

public enum PSICQUICReturnType {

	XML25("psi-mi/xml25"), MITAB25("psi-mi/tab25"), COUNT("count");

	private String typeName;

	private PSICQUICReturnType(String typeName) {
		this.typeName = typeName;
	}

	public String getTypeName() {
		return typeName;
	}
}
