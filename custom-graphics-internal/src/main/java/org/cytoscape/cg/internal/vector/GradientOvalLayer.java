package org.cytoscape.cg.internal.vector;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import org.cytoscape.cg.internal.paint.RadialGradientPaintFactory;

public class GradientOvalLayer extends GradientLayerCustomGraphics {
	
	// Name of this custom graphics.
	private static final String NAME = "Oval Gradient";
	
	
	public GradientOvalLayer(Long id) {
		super(id, NAME);
	}
	
	protected void renderImage(Graphics graphics) {
		super.renderImage(graphics);

		var g2d = (Graphics2D) graphics;
		
		update();
		g2d.setPaint(paintFactory.getPaint(shape.getBounds2D()));
		g2d.fillOval(rendered.getMinX(), rendered.getMinY(), width, height);
	}

	@SuppressWarnings("unchecked")
	public void update() {
		// First, remove all layers.
		layers.clear();
		shape = new Ellipse2D.Double(-width / 2, -height / 2, width, height);
		paintFactory = new RadialGradientPaintFactory(colorList, stopList);
		var cg = new PaintCustomGraphics(shape, paintFactory);

		layers.add(cg);
	}

	@Override
	public String toSerializableString() {
		return makeSerializableString(displayName);
	}
}
