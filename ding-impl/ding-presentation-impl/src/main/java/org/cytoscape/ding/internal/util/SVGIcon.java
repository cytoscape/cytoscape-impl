package org.cytoscape.ding.internal.util;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.StringReader;

import javax.swing.Icon;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class SVGIcon implements Icon {

	private final int width;
	private final int height;
	
	private final SVGDiagram diagram;

	public SVGIcon(String svg, int width, int height) {
		if (svg == null || svg.isBlank())
			throw new IllegalArgumentException("'svg' must not be null or empty");
		
		this.width = width;
		this.height = height;
		
		var universe = new SVGUniverse();
		var is = new StringReader(svg);
		var uri = universe.loadSVG(is, "about");
		diagram = universe.getDiagram(uri);
		diagram.setIgnoringClipHeuristic(true);
	}
	
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		// Icon width/height
		var w = width;
		var h = height;
		// Image width/height
		var iw = (double) diagram.getWidth();
		var ih = (double) diagram.getHeight();
		// New width/height
		var nw = iw;
	    var nh = ih;
		
		if (w == 0 || h == 0 || iw == 0 || ih == 0)
			return;
		
		var g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		// Fit image to shape's bounds...
		// - first check if we need to scale width
	    if (iw > w) {
	        // scale width to fit
	        nw = w;
	        // scale height to maintain aspect ratio
	        nh = (nw * ih) / iw;
	    }

	    // - then check if we need to scale even with the new height
	    if (nh > h) {
	        // scale height to fit instead
	        nh = h;
	        // scale width to maintain aspect ratio
	        nw = (nh * iw) / ih;
	    }
	    
	    // Position image correctly and scale
	    g2.translate(x + (w - nw) / 2.0, y);
		g2.scale(nw / iw, nh / ih);
		
		try {
			diagram.render(g2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		g2.dispose();
	}

	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}
}
