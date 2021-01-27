package org.cytoscape.cg.internal.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public final class ColorUtil {

	private ColorUtil() {}
	
	public static List<Color> parseColorList(String[] inputArray) {
		var colors = new ArrayList<Color>();
		
		// A color in the array can either be a hex value or a text color
		for (var colorString : inputArray) {
			colorString = colorString.trim();
			var c = parseColor(colorString);
			
			if (c == null)
				colors.add(c);
		}
		
		return colors;
	}

	public static Color parseColor(String input) {
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

	public static Color getContrastingColor(Color color) {
		int d = 0;
		// Counting the perceptive luminance - human eye favors green color...
		double a = 1 - (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;

		if (a < 0.5)
			d = 0; // bright colors - black font
		else
			d = 255; // dark colors - white font

		return new Color(d, d, d);
	}
	
	public static List<Color> getContrastingColors(List<Color> colors) {
		var list = new ArrayList<Color>();
		
		if (colors != null) {
			for (var c : colors)
				list.add(getContrastingColor(c));
		}
		
		return list;
	}
	
	public static String toHexString(Color color) {
		int rgb = color.getRGB();
		var hex = String.format("#%06X", (0xFFFFFF & rgb));

		return hex;
	}
}
