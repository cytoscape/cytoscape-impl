package org.cytoscape.ding.customgraphics.vector;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import org.cytoscape.ding.customgraphics.IDGenerator;
import org.cytoscape.ding.customgraphics.paint.GradientPaintFactory;
import org.cytoscape.ding.impl.customgraphics.DLayer;
import org.cytoscape.graph.render.stateful.CustomGraphic;

public class GradientOvalLayer extends GradientLayerCustomGraphics {
	
	// Name of this custom graphics.
	private static final String NAME = "Glossy Oval Layer";

	
	public GradientOvalLayer() {
		this(IDGenerator.getIDGenerator().getNextId(), NAME);
	}
	
	
	public GradientOvalLayer(Long id, String name) {
		super(id, name);
	}
	
	protected void renderImage(Graphics graphics) {
		super.renderImage(graphics);
		
		final Graphics2D g2d = (Graphics2D) graphics;
		// Render
		update();
		g2d.setPaint(paintFactory.getPaint(bound.getBounds2D()));
		g2d.fillOval(rendered.getMinX(), rendered.getMinY(), 
				width, height);
	}
	
	public void update() {
		// First, remove all layers.
		layers.clear();
		bound = new Ellipse2D.Double(-width / 2, -height / 2, width, height);
		paintFactory = new GradientPaintFactory(c1.getValue(), c2.getValue());
		final CustomGraphic cg = new CustomGraphic(bound, paintFactory);
		
		DLayer layer = new DLayer(cg, 1);
		layers.add(layer);
	}

}
