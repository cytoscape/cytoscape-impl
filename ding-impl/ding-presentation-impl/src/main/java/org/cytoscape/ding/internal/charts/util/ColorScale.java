package org.cytoscape.ding.internal.charts.util;

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

	public ColorScale(final double lowerBound,
					  final double upperBound,
					  final Color lowerColor,
					  final Color zeroColor,
					  final Color upperColor,
					  final Color nanColor) {
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
		
		final boolean hasZero = lowerBound < 0 && upperBound > 0 && zeroColor != null;
		
		if (hasZero && value < EPSILON && value > -EPSILON)
			return zeroColor;
		
		final double denom;
		
		if (hasZero)
			denom = value < 0 ? -lowerBound : upperBound;
		else
			denom = upperBound - lowerBound;
		
		final double f = (denom < EPSILON && denom > -EPSILON) ? 0 : (value - lowerBound) / denom;
		
		final Color c1 = hasZero && value > 0 ? zeroColor : lowerColor;
		final Color c2 = hasZero && value < 0 ? zeroColor : upperColor;
        
        int r1 = c1.getRed();
        int g1 = c1.getGreen();
        int b1 = c1.getBlue();
        int a1 = c1.getAlpha();
        
        int r2 = c2.getRed();
        int g2 = c2.getGreen();
        int b2 = c2.getBlue();
        int a2 = c2.getAlpha();
        
        double q = 1.0 - f;
        
        int r = (int) Math.round(r1*q + r2*f);
        int g = (int) Math.round(g1*q + g2*f);
        int b = (int) Math.round(b1*q + b2*f);
        int a = (int) Math.round(a1*q + a2*f);
        
        r = Math.min(Math.max(r, 0), 255);
        g = Math.min(Math.max(g, 0), 255);
        b = Math.min(Math.max(b, 0), 255);
        a = Math.min(Math.max(a, 0), 255);
		
        return new Color(r, g, b, a);        
	}
}
