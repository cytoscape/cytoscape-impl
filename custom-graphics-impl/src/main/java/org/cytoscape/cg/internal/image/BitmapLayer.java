package org.cytoscape.cg.internal.image;

import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.cytoscape.cg.internal.paint.TexturePaintFactory;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.ImageCustomGraphicLayer;

public class BitmapLayer implements ImageCustomGraphicLayer {
	
	private Rectangle2D bounds;
	private TexturePaintFactory pf;

	public BitmapLayer(Rectangle2D bounds, TexturePaintFactory factory) {
		this.bounds = bounds;
		this.pf = factory;
	}

	@Override
	public Rectangle2D getBounds2D() {
		return bounds;
	}

	// TODO: at some point, we should just bring all of the TexturePaintFactory
	// stuff into here....
	@Override
	public TexturePaint getPaint(Rectangle2D bounds) {
		return pf.getPaint(bounds);
	}

	@Override
	public CustomGraphicLayer transform(AffineTransform xform) {
		var s = xform.createTransformedShape(bounds);
		return new BitmapLayer(s.getBounds2D(), pf);
	}
}
