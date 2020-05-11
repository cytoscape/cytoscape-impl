package org.cytoscape.ding.customgraphics.image;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.StringReader;

import org.cytoscape.ding.customgraphics.paint.TexturePaintFactory;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.Cy2DGraphicLayer;

import com.kitfox.svg.SVGUniverse;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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

public class SVGLayer implements Cy2DGraphicLayer {

	private Rectangle2D bounds;
	private Rectangle2D scaledBounds;
	private BufferedImage img;
	private TexturePaint paint;
	private String svg;
	
	public SVGLayer(String svg) {
		this.svg = svg;
		var universe = new SVGUniverse();
		var is = new StringReader(svg);
		var uri = universe.loadSVG(is, "about");
		var diagram = universe.getDiagram(uri);
		diagram.setIgnoringClipHeuristic(true);
		
		bounds = scaledBounds = new Rectangle2D.Float(0, 0, diagram.getWidth(), diagram.getHeight());
	}

	@Override
	public Rectangle2D getBounds2D() {
		// IMPORTANT: If we decide that the CG must cache the SVG layer, we must reset the bounds x/y to 0
		// before returning it here, otherwise CustomGraphicsPositionCalculator will add a position transformation
		// repeatedly on these x/y values, which may move the image to an incorrect position.
		//     e.g. return new Rectangle2D.Double(0, 0, bounds.getWidth(), bounds.getHeight());
		return bounds;
	}

	@Override
	public CustomGraphicLayer transform(AffineTransform xform) {
		bounds = xform.createTransformedShape(bounds).getBounds2D();
		
		return this;
	}

	@Override
	public void draw(Graphics2D g, Shape shape, CyNetworkView networkView, View<? extends CyIdentifiable> view) {
		draw(g, shape, bounds, networkView, view);
	}
	
	public void draw(
			Graphics2D g,
			Shape shape,
			Rectangle2D bounds,
			CyNetworkView networkView,
			View<? extends CyIdentifiable> view
	) {
		var universe = new SVGUniverse();
		var is = new StringReader(svg);
		var uri = universe.loadSVG(is, "about");
		var diagram = universe.getDiagram(uri);
		diagram.setIgnoringClipHeuristic(true);
		
		// Bounds dimensions
		var x = bounds.getX();
		var y = bounds.getY();
		var w = bounds.getWidth();
		var h = bounds.getHeight();
		// Original image width/height
		var iw = (double) diagram.getWidth();
		var ih = (double) diagram.getHeight();
		// New image width/height
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
		
		// Scale factors
		var sx = nw / iw;
		var sy = nh / ih;
		g2.translate(x - nw / 2.0f, y - nh / 2.0f);
		g2.scale(sx, sy);
		
		try {
			diagram.render(g2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		g2.dispose();
	}
	
	@Override
	public TexturePaint getPaint(Rectangle2D r) {
		// If the bounds are the same as before, there is no need to recreate the "same" image again
		if (img == null || paint == null || !r.equals(scaledBounds)) {
			// Recreate and cache Image and TexturePaint
			img = createImage(r);
			paint = new TexturePaintFactory(img).getPaint(
					new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight()));
		}
		
		scaledBounds = r;
		
		return paint;
	}
	
	public BufferedImage createImage(Rectangle2D r) {
		var x = r.getX();
		var y = r.getY();
		var w = (int) r.getWidth();
		var h = (int) r.getHeight();
		var b = new Rectangle2D.Double(x + w / 2.0, y + h / 2.0, w, h);
		
		var image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		var g2 = (Graphics2D) image.getGraphics();
		
		draw(g2, r, b, null, null);
		
        return image;
	}
}
