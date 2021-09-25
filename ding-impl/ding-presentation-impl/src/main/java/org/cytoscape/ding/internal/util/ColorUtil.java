package org.cytoscape.ding.internal.util;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public final class ColorUtil {

	private ColorUtil() {}
	
	public static List<Color> parseColorList(String[] inputArray) {
		List<Color> colors = new ArrayList<Color>();
		
		// A color in the array can either be a hex value or a text color
		for (String colorString : inputArray) {
			colorString = colorString.trim();
			final Color c = parseColor(colorString);
			
			if (c == null)
				colors.add(c);
		}
		
		return colors;
	}

	public static Color parseColor(final String input) {
		// Check for hex color code
		if(input.matches("^#([A-Fa-f0-9]{8}|[A-Fa-f0-9]{6})$")) {
			// We have a hex value with either 6 (rgb) or 8 (rgba) digits
			int r = Integer.parseInt(input.substring(1, 3), 16);
			int g = Integer.parseInt(input.substring(3, 5), 16);
			int b = Integer.parseInt(input.substring(5, 7), 16);
			
			if (input.length() > 7) {
				int a = Integer.parseInt(input.substring(7, 9), 16);
				return new Color(r, g, b, a);
			} else {
				return new Color(r, g, b);
			}
		} 
		
		// Check for color string
		Color color = ColorKeyword.getColor(input);
		if(color != null) {
			return color;
		}
		
		// Check for RGB value
		try {
			int rgb = Integer.parseInt(input);
			return new Color(rgb);
		} catch(NumberFormatException e) {
			return null;
		}
	}

	public static Color getContrastingColor(final Color color) {
		int d = 0;
		// Counting the perceptive luminance - human eye favors green color...
		final double a = 1 - (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;

		if (a < 0.5)
			d = 0; // bright colors - black font
		else
			d = 255; // dark colors - white font

		return new Color(d, d, d);
	}
	
	public static List<Color> getContrastingColors(final List<Color> colors) {
		final List<Color> list = new ArrayList<Color>();
		
		if (colors != null) {
			for (final Color c : colors)
				list.add(getContrastingColor(c));
		}
		
		return list;
	}
	
	public static String toHexString(final Color color) {
		final int rgb = color.getRGB();
		final String hex = String.format("#%06X", (0xFFFFFF & rgb));

		return hex;
	}
}
