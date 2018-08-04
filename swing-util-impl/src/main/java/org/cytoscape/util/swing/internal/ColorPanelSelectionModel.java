package org.cytoscape.util.swing.internal;

import javax.swing.colorchooser.DefaultColorSelectionModel;

import org.cytoscape.util.color.Palette;


public class ColorPanelSelectionModel extends DefaultColorSelectionModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Palette palette;

	public void setPalette(Palette palette) {
		if (this.palette != palette)
			super.setSelectedColor(null);
		this.palette = palette;
		fireStateChanged();
	}

	public Palette getPalette() {
		return palette;
	}
}
