package org.cytoscape.ding.impl.strokes;

import java.awt.BasicStroke;

public class LongDashStroke extends BasicStroke implements WidthStroke {

	float width;

	public LongDashStroke(float width) {
		super(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 
		      10.0f, new float[]{width * 4f, width * 2f}, 0.0f);
		this.width = width;
	}

	public WidthStroke newInstanceForWidth(float w) {
		return new LongDashStroke(w);
	}


	@Override public String toString() { return this.getClass().getSimpleName() + " " + Float.toString(width); }
}


