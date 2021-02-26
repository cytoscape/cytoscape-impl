package org.cytoscape.cg.internal.paint;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import org.cytoscape.cg.internal.util.PaintFactory;

public class ColorPaintFactory implements PaintFactory {

	private Color color;

	public ColorPaintFactory(Color color) {
		this.color = color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return this.color;
	}

	@Override
	public Paint getPaint(Rectangle2D arg0) {
		return color;
	}
}
