package org.cytoscape.app.internal;

public enum Category {
	CORE("Core"), ANALYSIS("Analysis"), NETWORK_ATTRIBUTE_IO(
			"Network and Attribute I/O"), NETWORK_INFERENCE(
			"Network Inference"), FUNCTIONAL_ENRICHMENT(
			"Functional Enrichment"), COMMUNICATION_SCRIPTING(
			"Communication/Scripting"), THEME("Theme"),
			NONE("Uncategorized"), OUTDATED("Outdated")
			;

	private String catText;

	private Category(String type) {
		catText = type;
	}

	public String toString() {
		return catText;
	}

	public String getCategoryText() {
		return toString();
	}
}

	
