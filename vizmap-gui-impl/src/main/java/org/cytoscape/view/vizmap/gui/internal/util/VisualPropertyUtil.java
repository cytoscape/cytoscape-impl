package org.cytoscape.view.vizmap.gui.internal.util;

import java.awt.Color;
import java.awt.Font;
import java.util.Iterator;
import java.util.List;

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
	
	private VisualPropertyUtil() {
		// restrict instantiation
	}
}
