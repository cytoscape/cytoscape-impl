package org.cytoscape.ding.impl;

import java.awt.Color;
import java.awt.Paint;

public abstract class AbstractViewDefaultSupport {
	
	protected final Color getTransparentColor(final Paint p, final int transparency) {
		
		if (p != null && p instanceof Color)
			return new Color(((Color) p).getRed(), ((Color) p).getGreen(), ((Color) p).getBlue(), transparency);
		else
			return (Color) p;
	}

}
