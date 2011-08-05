package org.cytoscape.ding.customgraphics.paint;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import org.cytoscape.graph.render.stateful.PaintFactory;

public class ColorPaintFactory implements PaintFactory {

	private Color color;
	
	public ColorPaintFactory(final Color color) {
		this.color = color;
	}
	
	public void setColor(final Color color) {
		this.color = color;
	}
	
	public Color getColor() {
		return this.color;
	}
	

	public Paint getPaint(Rectangle2D arg0) {
		return color;
	}

}
