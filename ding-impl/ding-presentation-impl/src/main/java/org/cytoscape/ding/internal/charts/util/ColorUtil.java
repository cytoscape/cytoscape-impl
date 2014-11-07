package org.cytoscape.ding.internal.charts.util;

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
		Color color = null;
		
		if (input.matches("^#([A-Fa-f0-9]{8}|[A-Fa-f0-9]{6})$")) {
			// We have a hex value with either 6 (rgb) or 8 (rgba) digits
			int r = Integer.parseInt(input.substring(1, 3), 16);
			int g = Integer.parseInt(input.substring(3, 5), 16);
			int b = Integer.parseInt(input.substring(5, 7), 16);
			
			if (input.length() > 7) {
				int a = Integer.parseInt(input.substring(7, 9), 16);
				color = new Color(r, g, b, a);
			} else {
				color = new Color(r, g, b);
			}
		} else {
			// Check for color string
			color = ColorKeyword.getColor(input);
		}
		
		return color;
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
