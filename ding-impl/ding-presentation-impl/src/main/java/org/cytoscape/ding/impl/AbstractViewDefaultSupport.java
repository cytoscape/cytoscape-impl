package org.cytoscape.ding.impl;

import java.awt.Color;
import java.awt.Paint;

import org.cytoscape.view.model.VisualProperty;

public abstract class AbstractViewDefaultSupport {
	
	/**
	 * Set the default value for a visual property
	 */
	protected abstract <V> void setViewDefault(VisualProperty<V> vp, V value);
	
	protected final Color getTransparentColor(final Paint p, final int transparency) {
		if (p != null && p instanceof Color)
			return new Color(((Color) p).getRed(), ((Color) p).getGreen(), ((Color) p).getBlue(), transparency);
		else
			return (Color) p;
	}

}
