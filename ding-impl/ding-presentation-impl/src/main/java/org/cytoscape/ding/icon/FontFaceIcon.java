package org.cytoscape.ding.icon;

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
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

/**
 * Icon renderer for font face.
 */
public class FontFaceIcon extends VisualPropertyIcon<Font> {

	private static final long serialVersionUID = 4629615986711780878L;

	private static final String TEXT = "A";
	private static final int FONT_SIZE = 28;
	
	public FontFaceIcon(final Font value, int width, int height, String name) {
		super(value, width, height, name);
	}

	@Override
	public void paintIcon(final Component c, final Graphics g, int x, int y) {
		if (value != null) {
			// First get a large image from the text value
			final Font font = new Font(value.getFontName(), value.getStyle(), FONT_SIZE);
			final BufferedImage bi = createLargeImage(TEXT, font, c.getForeground());
			
			// Then down-sample the image to fit the required width and height
			downSample(bi, c, g, x, y);
		}
	}
	
	private BufferedImage createLargeImage(final String text, final Font font, final Color color) {
        final FontRenderContext frc = new FontRenderContext(null, true, true);
        final TextLayout layout = new TextLayout(text, font, frc);
        final Rectangle r = layout.getPixelBounds(null, 0, 0);
        
        final BufferedImage bi = new BufferedImage(r.width, r.height, BufferedImage.TYPE_INT_ARGB);
        
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
		
		final AffineTransform at = new AffineTransform();
		
		if (scale < 0)
			at.scale(scale, scale);
		
		final AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		final Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		final Rectangle2D r2d = scaleOp.getBounds2D(bi);
		double sh = r2d.getHeight(); // scaled height
		double sw = r2d.getWidth(); // scaled width
		int vpad = (int) (y + (height - sh) / 2.0);
		int hpad = (int) (x + (width - sw) / 2.0);
		
		g2d.drawImage(bi, scaleOp, hpad, vpad); // draw it centered
	}
}
