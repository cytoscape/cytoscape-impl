package org.cytoscape.ding.internal.charts;

import java.awt.Color;

import org.cytoscape.ding.internal.charts.util.ColorUtil;

/**
 * A control point defining a gradient
 */
public class ControlPoint {
	
	/** The color at this control point */
	public Color color;
	/** The position of this control point (0.0 -> 1.0) */
	public float position;
	
	/**
	 * Create a new control point
	 * 
	 * @param color The color at this control point
	 * @param position The position of this control point (0 -> 1)
	 */
	public ControlPoint(final Color color, final float position) {
		if (color == null)
			throw new IllegalArgumentException("'color' must not be null.");
		
		this.color = color;
		this.position = position;
	}
	
	/**
	 * Format: "position:color" (e.g. "0.5:#ffffff")
	 */
	@Override
	public String toString() {
		// Do not change it! It's used on visual property serialization.
		return position + ":" + ColorUtil.toHexString(color);
	}
	
	public static ControlPoint parse(final String input) {
		if (input == null || input.isEmpty())
			return null;
		
		final String[] components = input.split(":");
		
		if (components.length != 2)
			return null;

		final float position = Float.parseFloat(components[0]);
		final Color color = ColorUtil.parseColor(components[1]);
		
		return new ControlPoint(color, position);
	}
}
