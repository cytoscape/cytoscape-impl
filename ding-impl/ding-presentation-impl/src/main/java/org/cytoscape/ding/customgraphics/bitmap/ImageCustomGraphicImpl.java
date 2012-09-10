package org.cytoscape.ding.customgraphics.bitmap;

import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.cytoscape.view.presentation.customgraphics.CustomGraphic;
import org.cytoscape.view.presentation.customgraphics.ImageCustomGraphic;
import org.cytoscape.ding.customgraphics.paint.TexturePaintFactory;

public class ImageCustomGraphicImpl implements ImageCustomGraphic {
	private Rectangle2D bounds;
	private TexturePaintFactory pf;

	public ImageCustomGraphicImpl(Rectangle2D bounds, TexturePaintFactory factory) {
		this.bounds = bounds;
		this.pf = factory;
	}

	public Rectangle getBounds() { return bounds.getBounds(); }
	public Rectangle2D getBounds2D() { return bounds; }

	// TODO: at some point, we should just bring all of the TexturePaintFactory
	// stuff into here....
	@Override
	public TexturePaint getPaint(Rectangle2D bounds) {
		return pf.getPaint(bounds);
	}

	public CustomGraphic transform(AffineTransform xform) {
		Shape s = xform.createTransformedShape(bounds);
		return new ImageCustomGraphicImpl(s.getBounds2D(), pf);
	}
	
}
