package org.cytoscape.view.table.internal.util;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.renderer.PaintScale;

public class ColorScale implements PaintScale {
	
	private final double lowerBound;
	private final double upperBound;
	private final Color lowerColor;
	private final Color zeroColor;
	private final Color upperColor;
	private final Color nanColor;
	
	private static double EPSILON = 1e-30;

	public ColorScale(
			double lowerBound,
			double upperBound,
			Color lowerColor,
			Color zeroColor,
			Color upperColor,
			Color nanColor
	) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.lowerColor = lowerColor;
		this.zeroColor = zeroColor;
		this.upperColor = upperColor;
		this.nanColor = nanColor;
	}

	@Override
	public double getLowerBound() {
		return lowerBound;
	}

	@Override
	public double getUpperBound() {
		return upperBound;
	}
	
	public Color getLowerColor() {
		return lowerColor;
	}
	
	public Color getZeroColor() {
		return zeroColor;
	}
	
	public Color getUpperColor() {
		return upperColor;
	}
	
	public Color getNanColor() {
		return nanColor;
	}

	@Override
	public Paint getPaint(double value) {
		if (Double.isNaN(value))
			return nanColor;
		
		return getPaint(value, lowerBound, upperBound, lowerColor, zeroColor, upperColor);
	}
	
	public static Paint getPaint(
			double value,
			double lowerBound,
			double upperBound,
			Color lowerColor,
			Color zeroColor,
			Color upperColor
	) {
		boolean hasZero = lowerBound < -EPSILON && upperBound > EPSILON && zeroColor != null;
		
		if (hasZero && value < EPSILON && value > -EPSILON)
			return zeroColor;
		
		var color = value < 0.0 ? lowerColor : upperColor;
		
		// Linearly interpolate the value
		double f = value < 0.0 ?
				MathUtil.invLinearInterp(value, lowerBound, 0) : MathUtil.invLinearInterp(value, 0, upperBound);
		float t = (float) (value < 0.0 ? MathUtil.linearInterp(f, 0.0, 1.0) : MathUtil.linearInterp(f, 1.0, 0.0));
		
		// Make sure it's between 0.0-1.0
		t = Math.max(0.0f, t);
		t = Math.min(1.0f, t);
				
		return org.jdesktop.swingx.color.ColorUtil.interpolate(zeroColor, color, t);
	}
}
