package org.cytoscape.cg.internal.vector;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import org.cytoscape.cg.internal.paint.LinearGradientPaintFactory;

public class GradientRoundRectangleLayer extends GradientLayerCustomGraphics {

	// Name of this custom graphics.
	private static final String NAME = "Round Rectangle Gradient";
	private int r = 20;

	public GradientRoundRectangleLayer(Long id) {
		super(id, NAME);
	}

	protected void renderImage(Graphics graphics) {
		super.renderImage(graphics);

		var g2d = (Graphics2D) graphics;

		update();
		g2d.setPaint(paintFactory.getPaint(shape.getBounds2D()));
		g2d.fillRoundRect(rendered.getMinX(), rendered.getMinY(), rendered.getWidth(), rendered.getHeight(), r, r);
	}

	@SuppressWarnings("unchecked")
	public void update() {
		// First, remove all layers.
		layers.clear();

		r = (int) (Math.min(width, height) / 4f);
		shape = new RoundRectangle2D.Double(-width / 2, -height / 2, width, height, r, r);
		paintFactory = new LinearGradientPaintFactory(colorList, stopList);
		var cg = new PaintCustomGraphics(shape, paintFactory);
		layers.add(cg);
	}

	@Override
	public String toSerializableString() {
		return makeSerializableString(displayName);
	}
}
