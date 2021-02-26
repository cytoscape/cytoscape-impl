package org.cytoscape.cg.internal.util;

import java.awt.BasicStroke;
import java.awt.Stroke;

public class EqualDashStroke extends BasicStroke {

	private float width;

	public EqualDashStroke(float width) {
		super(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { width * 2f, width * 2f }, 0.0f);

		this.width = width;
	}

	public Stroke newInstanceForWidth(float w) {
		return new EqualDashStroke(w);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " " + Float.toString(width);
	}
}
