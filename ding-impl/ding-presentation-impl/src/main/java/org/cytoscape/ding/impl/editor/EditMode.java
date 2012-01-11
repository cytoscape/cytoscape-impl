package org.cytoscape.ding.impl.editor;

public class EditMode {
	
	private static boolean mode = false;
	
	public static final boolean isDirectMode() {
		return mode;
	}
	
	public static final void setMode(final boolean newMode) {
		mode = newMode;
	}
	
}
