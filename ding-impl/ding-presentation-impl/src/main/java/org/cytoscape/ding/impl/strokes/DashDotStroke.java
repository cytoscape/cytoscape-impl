package org.cytoscape.ding.impl.strokes;

import java.awt.BasicStroke;

public class DashDotStroke extends BasicStroke implements WidthStroke {

	private float width;

	public DashDotStroke(float width) {
		super(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f,
				new float[] { width * 4f, width * 2f, width, width * 2f }, 0.0f);

		this.width = width;
	}

	public WidthStroke newInstanceForWidth(float w) {
		return new DashDotStroke(w);
	}


	public String toString() {
		return this.getClass().getSimpleName() + " " + Float.toString(width);
	}
}
