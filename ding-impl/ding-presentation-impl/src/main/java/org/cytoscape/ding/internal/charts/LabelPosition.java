package org.cytoscape.ding.internal.charts;

public enum LabelPosition {
	STANDARD("Standard"),
	DOWN_45("Down 45\u00B0"),
	DOWN_90("Down 90\u00B0"),
	UP_45("Up 45\u00B0"),
	UP_90("Up 90\u00B0");
	
	private String label;

	private LabelPosition(final String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
}
