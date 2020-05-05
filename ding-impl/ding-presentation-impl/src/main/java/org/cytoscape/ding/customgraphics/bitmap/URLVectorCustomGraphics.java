package org.cytoscape.ding.customgraphics.bitmap;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.cytoscape.ding.customgraphics.paint.TexturePaintFactory;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.Cy2DGraphicLayer;

import com.kitfox.svg.SVGDiagram;
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

/**
 * Render vector images (e.g. SVG) created from a URL.
 */
public class URLVectorCustomGraphics extends AbstractURLImageCustomGraphics<Cy2DGraphicLayer> {

	private static final String DEF_TAG = "vector image";
	private static final String DEF_SVG = "TODO"; // TODO
	
	private String svg;
	
	public URLVectorCustomGraphics(Long id, String url) throws IOException {
		super(id, url);
		
		tags.add(DEF_TAG);
		buildCustomGraphics();
	}
	
	public URLVectorCustomGraphics(Long id, String name, String svg) throws IOException {
		super(id, name);
		
		if (svg == null || svg.isBlank())
			throw new IllegalArgumentException("'svg' must not be null or empty");
		
		this.svg = svg;
		tags.add(DEF_TAG);
		layers.add(new SVGLayer(svg));
	}

	@Override
	public Image getRenderedImage() {
		if (!layers.isEmpty())
			return ((SVGLayer) layers.get(0)).createImage(new Rectangle(width, height));
		
		return null;
	}
	
	public String getSVG() {
		return svg;
	}
	
	private void buildCustomGraphics() {
		layers.clear();
		
		try {
			var sourceUrl = getSourceURL();
			
			try (var in = new BufferedReader(new InputStreamReader(sourceUrl.openStream()))) {
				var sb = new StringBuilder();
				String line = null;
				
				while ((line = in.readLine()) != null) {
		            sb.append(line);
		            sb.append("\n");
		        }
				
				svg = sb.toString();
			} catch (Exception e) {
				svg = DEF_SVG;
			}
		} catch (Exception e) {
			svg = DEF_SVG;
		}
		
		if (svg == null)
			svg = DEF_SVG;
		
		var cg = new SVGLayer(svg);
		layers.add(cg);
	}
	
	private class SVGLayer implements Cy2DGraphicLayer {

		private Rectangle2D bounds;
		private Rectangle2D scaledBounds;
		private BufferedImage img;
		private TexturePaint paint;
		private SVGDiagram diagram;
		
		public SVGLayer(String svg) {
			var universe = new SVGUniverse();
			var is = new StringReader(svg);
			var uri = universe.loadSVG(is, "about");
			diagram = universe.getDiagram(uri);
			diagram.setIgnoringClipHeuristic(true);
			
			width = Math.round(diagram.getWidth());
			height = Math.round(diagram.getHeight());
			bounds = scaledBounds = new Rectangle2D.Float(0, 0, diagram.getWidth(), diagram.getHeight());
		}

		@Override
		public Rectangle2D getBounds2D() {
			return bounds;
		}

		@Override
		public CustomGraphicLayer transform(AffineTransform xform) {
			var shape = xform.createTransformedShape(bounds);
			bounds = shape.getBounds2D();
			
			return this;
		}

		@Override
		public void draw(Graphics2D g, Shape shape, CyNetworkView networkView, View<? extends CyIdentifiable> view) {
			// Shape width/height
			var sw = bounds.getWidth();
			var sh = bounds.getHeight();
			// Image width/height
			var iw = (double) diagram.getWidth();
			var ih = (double) diagram.getHeight();
			// New width/height
			var nw = iw;
		    var nh = ih;
			
			if (sw == 0 || sh == 0 || iw == 0 || ih == 0)
				return;
			
			var g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
			if (sw != iw || sh != ih) {
				// Fit image to shape's bounds...
				// - first check if we need to scale width
			    if (iw > sw) {
			        // scale width to fit
			        nw = sw;
			        // scale height to maintain aspect ratio
			        nh = (nw * ih) / iw;
			    }
	
			    // - then check if we need to scale even with the new height
			    if (nh > sh) {
			        // scale height to fit instead
			        nh = sh;
			        // scale width to maintain aspect ratio
			        nw = (nh * iw) / ih;
			    }
				
				g2.scale(nw / iw, nh / ih);
			}
			
			if (view != null) // Adjust to align (center) with the node view
				g2.translate(-iw / 2.0, -ih / 2.0);
			
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
		
		private BufferedImage createImage(Rectangle2D r) {
			var image = new BufferedImage((int) r.getWidth(), (int) r.getHeight(), BufferedImage.TYPE_INT_ARGB);
			var g2 = (Graphics2D) image.getGraphics();
			draw(g2, r, null, null);
			
	        return image;
		}
	}
}
