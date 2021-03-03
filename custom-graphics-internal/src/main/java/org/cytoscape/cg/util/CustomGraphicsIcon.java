package org.cytoscape.cg.util;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.cytoscape.cg.internal.util.VisualPropertyIcon;
import org.cytoscape.cg.model.SVGCustomGraphics;
import org.cytoscape.cg.model.SVGLayer;
import org.cytoscape.view.presentation.customgraphics.Cy2DGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

@SuppressWarnings("serial")
public class CustomGraphicsIcon extends VisualPropertyIcon<CyCustomGraphics<?>> {

	private List<? extends Cy2DGraphicLayer> cy2DLayers;
	
	public CustomGraphicsIcon(CyCustomGraphics<?> value, int width, int height, String name) {
		super(value, width, height, name);
		var img = value.getRenderedImage();
		
		if (img != null)
			setImage(img);
	}
	
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		if (width <= 0 || height <= 0)
			return;
		
		var g2 = (Graphics2D) g.create();
		var cg = getValue();
		
		if (cg instanceof SVGCustomGraphics) {
			if (cy2DLayers == null)
				cy2DLayers = ((SVGCustomGraphics) cg).getLayers();
			
			var rect = new Rectangle2D.Float(x + width / 2.0f, y + height / 2.0f, width, height);
			
			for (var cgl : cy2DLayers) {
				// Much easier to use the SVGLayer draw method than have calculate and apply
				// the same scale factor and translation transform already done by the layer!
				if (cgl instanceof SVGLayer)
					((SVGLayer) cgl).draw(g2, rect, rect);
			}
		} else {
			var img = getImage();
			
			if (img != null) {
				// Bounds dimensions
				var w = width;
				var h = height;
				// Original image width/height
				var iw = img.getWidth(null);
				var ih = img.getHeight(null);
				// New image width/height
				var nw = iw;
				var nh = ih;
				
				if (iw > 0 && ih > 0) {
					// Fit image to shape's bounds...
					// - first check if we need to scale width
					if (iw > w) {
						nw = w; // scale width to fit
						nh = (nw * ih) / iw; // scale height to maintain aspect ratio
					}
					
					// - then check if we need to scale even with the new height
					if (nh > h) {
						nh = h; // scale height to fit instead
						nw = (nh * iw) / ih; // scale width to maintain aspect ratio
					}
					
					img = img.getScaledInstance(nw, nh, Image.SCALE_AREA_AVERAGING);
					
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g2.drawImage(img, (int) (x + (w - nw) / 2.0f), (int) (y + (h - nh) / 2.0f), nw, nh, c);
				}
			}
		}
		
		g2.dispose();
	}
}
