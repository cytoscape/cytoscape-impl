package org.cytoscape.view.vizmap.gui.internal.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;

import org.cytoscape.view.presentation.property.values.VisualPropertyValue;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;

public final class VisualPropertyUtil {

	public static String getDisplayString(final Object value) {
		String s = null;
		
		if (value instanceof VisualPropertyValue) {
			s = ((VisualPropertyValue)value).getDisplayName();
		} else if (value instanceof Font) {
			s  = ((Font)value).getFontName();
		} else if (value instanceof Color) {
			final Color c  = (Color)value;
			int r = c.getRed();
			int g = c.getGreen();
			int b = c.getBlue();
			s = String.format("R:%s G:%s B:%s - #%02x%02x%02x", r, g, b, r, g, b).toUpperCase();
		} else if (value != null) {
			s = value.toString();
		}
		
		return s;
	}
	
	public static Double getMaxValue(final ContinuousMapping<?, ?> mapping) {
		Double max = null;
		
		if (mapping != null) {
			final List<?> points = mapping.getAllPoints();
			
			if (!points.isEmpty()) {
				max = Double.NEGATIVE_INFINITY;
				final Iterator<?> iterator = points.iterator();
				
				while (iterator.hasNext()) {
					final ContinuousMappingPoint<?, ?> p = (ContinuousMappingPoint<?, ?>) iterator.next();
					final Object value = p.getValue();
					
					 if (value instanceof Number) {
						 final double v = ((Number)value).doubleValue();
						 max = Math.max(v, max);
					 }
				}
			}
		}
		
		return max;
	}
	
	public static Double getMinValue(final ContinuousMapping<?, ?> mapping) {
		Double min = null;
		
		if (mapping != null) {
			final List<?> points = mapping.getAllPoints();
			
			if (!points.isEmpty()) {
				min = Double.POSITIVE_INFINITY;
				final Iterator<?> iterator = points.iterator();
				
				while (iterator.hasNext()) {
					final ContinuousMappingPoint<?, ?> p = (ContinuousMappingPoint<?, ?>) iterator.next();
					final Object value = p.getValue();
					
					if (value instanceof Number) {
						final double v = ((Number)value).doubleValue();
						min = Math.min(v, min);
					}
				}
			}
		}
		
		return min;
	}
	
	public static ImageIcon resizeIcon(final ImageIcon icon, int width, int height) {
		final Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
		final BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		final Graphics2D g = bi.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(img, 0, 0, width, height, null);
		g.dispose();
		
		return new ImageIcon(bi);
	}
	
	public static Color getContrastingColor(final Color color) {
		int d;
		// Counting the perceptive luminance - human eye favors green color...
		final double a = 1 - (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;

		if (a < 0.5)
			d = 0; // bright colors - black font
		else
			d = 255; // dark colors - white font

		return new Color(d, d, d);
	}
	
	private VisualPropertyUtil() {
		// restrict instantiation
	}
}
