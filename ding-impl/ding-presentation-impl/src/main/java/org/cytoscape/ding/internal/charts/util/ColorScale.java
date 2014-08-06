package org.cytoscape.ding.internal.charts.util;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.renderer.PaintScale;

public class ColorScale implements PaintScale {
	
	private final double lowerBound;
	private final double upperBound;
	private final Color color1;
	private final Color color2;
	
	private static double EPSILON = 1e-30;

	public ColorScale(final double lowerBound, final double upperBound, final Color color1, final Color color2) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.color1 = color1;
		this.color2 = color2;
	}

	@Override
	public double getLowerBound() {
		return lowerBound;
	}

	@Override
	public double getUpperBound() {
		return upperBound;
	}
	
	public Color getColor1() {
		return color1;
	}
	
	public Color getColor2() {
		return color2;
	}

	@Override
	public Paint getPaint(double value) {
		final double denom = (upperBound - lowerBound);
		final double f = (denom < EPSILON && denom > -EPSILON ? 0 : (value - lowerBound) / denom);
        
        int r1 = color1.getRed();
        int g1 = color1.getGreen();
        int b1 = color1.getBlue();
        int a1 = color1.getAlpha();
        
        int r2 = color2.getRed();
        int g2 = color2.getGreen();
        int b2 = color2.getBlue();
        int a2 = color2.getAlpha();
        
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
