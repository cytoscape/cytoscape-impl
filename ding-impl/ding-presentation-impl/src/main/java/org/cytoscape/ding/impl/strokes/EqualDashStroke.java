

package org.cytoscape.ding.impl.strokes;

import java.awt.BasicStroke;

public class EqualDashStroke extends BasicStroke implements WidthStroke {

	private float width;

	public EqualDashStroke(float width) {
		super(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 
		      10.0f, new float[]{width * 2f,width * 2f}, 0.0f);

		this.width = width;
	}

	public WidthStroke newInstanceForWidth(float w) {
		return new EqualDashStroke(w);
	}

	@Override public String toString() { return this.getClass().getSimpleName() + " " + Float.toString(width); }
}


