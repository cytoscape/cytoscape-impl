package org.cytoscape.ding.customgraphics.vector;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.ding.customgraphics.paint.RadialGradientPaintFactory;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public class GradientOvalLayer extends GradientLayerCustomGraphics {
	
	// Name of this custom graphics.
	private static final String NAME = "Oval Gradient";
	
	
	public GradientOvalLayer(final Long id) {
		super(id, NAME);
	}
	
	protected void renderImage(Graphics graphics) {
		super.renderImage(graphics);
		
		final Graphics2D g2d = (Graphics2D) graphics;
		// Render
		update();
		g2d.setPaint(paintFactory.getPaint(shape.getBounds2D()));
		g2d.fillOval(rendered.getMinX(), rendered.getMinY(), 
				width, height);
	}
	
	public void update() {
		// First, remove all layers.
		layers.clear();
		shape = new Ellipse2D.Double(-width / 2, -height / 2, width, height);
		paintFactory = new RadialGradientPaintFactory(colorList, stopList);
		final CustomGraphicLayer cg = new PaintCustomGraphic(shape, paintFactory);
		
		layers.add(cg);
	}

}
