package org.cytoscape.ding.internal.charts;

import java.awt.Color;

import org.cytoscape.ding.internal.charts.util.ColorUtil;

/**
 * A control point defining a gradient
 */
public class ControlPoint {
	
	/** The color at this control point */
	private Color color;
	/** The position of this control point (0.0 -> 1.0) */
	private float position;
	
	public ControlPoint() {
		this(Color.WHITE, 0.0f);
	}
	
	/**
	 * Create a new control point
	 * 
	 * @param color The color at this control point
	 * @param position The position of this control point (0 -> 1)
	 */
	public ControlPoint(final Color color, final float position) {
		if (color == null)
			throw new IllegalArgumentException("'color' must not be null.");
		
		this.setColor(color);
		this.setPosition(position);
	}
	
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public float getPosition() {
		return position;
	}

	public void setPosition(float position) {
		this.position = position;
	}

	@Override
	public String toString() {
		return getPosition() + ":" + ColorUtil.toHexString(getColor());
	}
}
