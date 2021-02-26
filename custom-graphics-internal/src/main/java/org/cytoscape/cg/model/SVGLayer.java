package org.cytoscape.cg.model;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.StringReader;

import org.cytoscape.cg.internal.paint.TexturePaintFactory;
import org.cytoscape.cg.internal.util.MathUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.Cy2DGraphicLayer;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;

public class SVGLayer implements Cy2DGraphicLayer {

	private Rectangle2D bounds;
	private Rectangle2D scaledBounds;
	private BufferedImage img;
	private TexturePaint paint;
	
	private final String svg;
	private final SVGDiagram diagram;
	
	public SVGLayer(String svg) {
		this.svg = svg;
		
		var universe = new SVGUniverse();
		var is = new StringReader(svg);
		var uri = universe.loadSVG(is, "about");
		diagram = universe.getDiagram(uri);
		diagram.setIgnoringClipHeuristic(true);
		
		bounds = scaledBounds = new Rectangle2D.Float(0, 0, diagram.getWidth(), diagram.getHeight());
	}
	
	/**
	 * Use this constructor to create an instance that reuses an SVGDiagram but uses a transformed bounds,
	 * which should improve the overall performance.
	 */
	private SVGLayer(
			String svg,
			SVGDiagram diagram,
			Rectangle2D bounds,
			Rectangle2D scaledBounds
	) {
		this.svg = svg;
		this.diagram = diagram;
		this.bounds = bounds;
		this.scaledBounds = scaledBounds;
	}

	@Override
	public Rectangle2D getBounds2D() {
		return bounds;
	}

	@Override
	public CustomGraphicLayer transform(AffineTransform xform) {
		var newBounds = xform.createTransformedShape(bounds).getBounds2D();
		
		return new SVGLayer(svg, diagram, newBounds, scaledBounds);
	}

	@Override
	public void draw(Graphics2D g, Shape shape, CyNetworkView networkView, View<? extends CyIdentifiable> view) {
		draw(g, shape, bounds);
	}
	
	@Override
	public void draw(Graphics2D g, CyTableView tableView, CyColumn column, CyRow row) {
		draw(g, bounds, bounds);
	}
	
	public void draw(Graphics2D g, Shape shape, Rectangle2D bounds) {
		// Bounds dimensions
		var x = bounds.getX();
		var y = bounds.getY();
		var w = bounds.getWidth();
		var h = bounds.getHeight();
		// Original image width/height
		var iw = (double) diagram.getWidth();
		var ih = (double) diagram.getHeight();
		// New image width/height
		
		if (w == 0 || h == 0 || iw == 0 || ih == 0)
			return;
		
		var g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		// Fit image to shape's bounds...
		var scale = MathUtil.scaleToFit(iw, ih, w, h);
		var nw = iw * scale;
		var nh = ih * scale;
		
		// Scale factors
		g2.translate(x - nw / 2.0f, y - nh / 2.0f);
		g2.scale(scale, scale);
		
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
		
		draw(g2, r, b);
		
        return image;
	}
}
