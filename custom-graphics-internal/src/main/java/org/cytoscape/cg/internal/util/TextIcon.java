package org.cytoscape.cg.internal.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

@SuppressWarnings("serial")
public class TextIcon extends VisualPropertyIcon<Object> {

	private static final Font DEFAULT_FONT = new Font("SansSerif", Font.BOLD, 28);
	private static final int MAX_TEXT_LEN = 5;
	
	public TextIcon(Object value, int width, int height, String name) {
		super(value, width, height, name);
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		String text = value != null ? value.toString() : "";
		
		if (!text.isEmpty()) {
			if (!(value instanceof Number) && text.length() > MAX_TEXT_LEN)
				text = text.substring(0, MAX_TEXT_LEN - 1) + "...";
			
			// First get a large image from the text value
			var bi = createLargeImage(text, DEFAULT_FONT, c.getForeground());
			
			// Then down-sample the image to fit the required width and height
			downSample(bi, c, g, x, y);
		}
	}
	
	private BufferedImage createLargeImage(String text, Font font, Color color) {
		var frc = new FontRenderContext(null, true, true);
		var layout = new TextLayout(text, font, frc);
		var r = layout.getPixelBounds(null, 0, 0);
        
        // Note: Add a few pixels to width and height to prevent clipping the image when the text is scaled down
        // (test with text = "5", for example)
		var bi = new BufferedImage(r.width + 4, r.height + 1, BufferedImage.TYPE_INT_ARGB);
        
		var g2d = (Graphics2D) bi.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        layout.draw(g2d, 0, -r.y);
        g2d.dispose();
        
        return bi;
    }
	
	private void downSample(BufferedImage bi, Component c, Graphics g, int x, int y) {
		int iw = bi.getWidth();
		int ih = bi.getHeight();
		double scale = Math.min( (double)width/(double)iw, (double)height/(double)ih );
		scale = scale > 1 ? 1 : scale;
		
		var at = new AffineTransform();
		at.scale(scale, scale);
		
		var scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		var g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		var r2d = scaleOp.getBounds2D(bi);
		double sh = r2d.getHeight(); // scaled height
		double sw = r2d.getWidth(); // scaled width
		int vpad = (int) (1 + y + (height - sh) / 2.0);
		int hpad = (int) (x + (width - sw) / 2.0);
		
		g2d.drawImage(bi, scaleOp, hpad, vpad); // draw it centered
		
		g2d.dispose();
	}
}
