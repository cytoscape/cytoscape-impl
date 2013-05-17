package org.cytoscape.ding.icon;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class TextIcon extends VisualPropertyIcon<Object> {

	private static final long serialVersionUID = -4217147694751380332L;

	private static final Font DEFAULT_FONT = new Font("SansSerif", Font.BOLD, 18);

	public TextIcon(final Object value, final int width, final int height, final String name) {
		super(value, width, height, name);
	}

	@Override
	public void paintIcon(final Component c, final Graphics g, int x, int y) {
		final String text = value != null ? value.toString() : "";
		
		if (!text.isEmpty()) {
			// First get a large image from the text value
			final BufferedImage bi = createLargeImage(text, DEFAULT_FONT, c.getForeground());
			
			// Then down-sample the image to fit the required width and height
			downSample(bi, c, g, x, y);
		}
	}
	
	private BufferedImage createLargeImage(final String text, final Font font, final Color color) {
        final FontRenderContext frc = new FontRenderContext(null, true, true);
        final TextLayout layout = new TextLayout(text, font, frc);
        final Rectangle r = layout.getPixelBounds(null, 0, 0);
        
        final BufferedImage bi = new BufferedImage(r.width + 4, r.height + 4, BufferedImage.TYPE_INT_ARGB);
        
        final Graphics2D g2d = (Graphics2D) bi.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        layout.draw(g2d, 0, -r.y);
        g2d.dispose();
        
        return bi;
    }
	
	private void downSample(final BufferedImage bi, final Component c, final Graphics g, int x, int y) {
		int iw = bi.getWidth();
		int ih = bi.getHeight();
		double scale = Math.min( (double)width/(double)iw, (double)height/(double)ih );
		scale = scale > 1 ? 1 : scale;
		
		final AffineTransform at = new AffineTransform();
		at.scale(scale, scale);
		
		final AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		final Graphics2D g2d = (Graphics2D) g;
		
		double sh = scale*ih; // scaled height
		int vpad = (int) ((c.getHeight() - sh) / 2.0);

		g2d.drawImage(bi, scaleOp, x, y+vpad); // draw it centered
	}
}
