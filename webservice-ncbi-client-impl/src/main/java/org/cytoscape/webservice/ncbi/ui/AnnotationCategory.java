package org.cytoscape.webservice.ncbi.ui;

public enum AnnotationCategory {
	SUMMARY("Summary"), PUBLICATION("Publications"), PHENOTYPE("Phenotypes"), PATHWAY("Pathways"), GENERAL(
			"General Protein Information"), LINK("Additional Links");
	
	private final String name;

	private AnnotationCategory(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static AnnotationCategory getValue(String dispName) {
		for (AnnotationCategory ann : values()) {
			if (ann.name.equals(dispName)) {
				return ann;
			}
		}

		return null;
	}
}