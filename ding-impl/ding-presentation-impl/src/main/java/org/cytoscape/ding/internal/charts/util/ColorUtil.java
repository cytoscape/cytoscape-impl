package org.cytoscape.ding.internal.charts.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public final class ColorUtil {

	public static final String CONTRASTING = "contrasting";
	public static final String MODULATED = "modulated";
	public static final String RAINBOW = "rainbow";
	public static final String RANDOM = "random";
	public static final String UP_DOWN = "updown";
	public static final String CUSTOM = "custom";
	
	/** ColorBrewer - PuOr (2 color-blind safe divergent colors) */
	public static final Color[] UP_DOWN_PUOR = new Color[] { new Color(241, 163, 64), new Color(153, 142, 195) };
	
	public static final String ZERO = "zero:";

	private ColorUtil() {}
	
	public static List<Color> getColors(final String scheme, final int nColors) {
		final List<Color> colors;

		if (scheme != null) 
			colors = parseColorKeyword(scheme.trim(), nColors);
		else
			colors = generateContrastingColors(nColors); // Default
		
		return colors;
	}
	
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

	public static List<Color> generateRandomColors(int nColors) {
		Calendar cal = Calendar.getInstance();
		int seed = cal.get(Calendar.SECOND);
		Random rand = new Random(seed);
		List<Color> result = new ArrayList<Color>(nColors);
		
		for (int index = 0; index < nColors; index++) {
			int r = rand.nextInt(255);
			int g = rand.nextInt(255);
			int b = rand.nextInt(255);
			result.add(index, new Color(r, g, b, 255));
		}
		
		return result;
	}

	// Rainbow colors just divide the Hue wheel into n pieces and return them
	public static List<Color> generateRainbowColors(int nColors) {
		List<Color> values = new ArrayList<Color>();
		
		for (float i = 0.0f; i < (float) nColors; i += 1.0f) {
			values.add(new Color(Color.HSBtoRGB(i / (float) nColors, 1.0f, 1.0f)));
		}
		
		return values;
	}

	// Rainbow colors just divide the Hue wheel into n pieces and return them,
	// but in this case, we're going to change the saturation and intensity
	public static List<Color> generateModulatedRainbowColors(int nColors) {
		List<Color> values = new ArrayList<Color>();
		
		for (float i = 0.0f; i < (float) nColors; i += 1.0f) {
			float sat = (Math.abs(((Number) Math.cos((8 * i) / (2 * Math.PI))).floatValue()) * 0.7f) + 0.3f;
			float br = (Math.abs(((Number) Math.sin(((i) / (2 * Math.PI)) + (Math.PI / 2))).floatValue()) * 0.7f) + 0.3f;
	
			values.add(new Color(Color.HSBtoRGB(i / (float) nColors, sat, br)));
		}
		
		return values;
	}

	// This is like rainbow, but we alternate sides of the color wheel
	public static List<Color> generateContrastingColors(int nColors) {
		List<Color> values = new ArrayList<Color>();
		
		// We need to special-case the situation where we only have two colors
		if (nColors == 2) {
			values.add(new Color(Color.HSBtoRGB(0.0f, 1.0f, 1.0f)));
			values.add(new Color(Color.HSBtoRGB(0.5f, 1.0f, 1.0f)));
			return values;
		}
	
		float divs = (float) nColors;
		for (float i = 0.0f; i < divs; i += 1.0f) {
			Color rgbColor = new Color(Color.HSBtoRGB(i / divs, 1.0f, 1.0f));
			values.add(rgbColor);
			i += 1.0f;
			if (i >= divs)
				break;
			float hue = (i / divs) + 0.5f; // This moves to the opposite side of the color wheel
			
			if (hue >= 1.0f)
				hue = hue - 1.0f;
			
			rgbColor = new Color(Color.HSBtoRGB(hue, 1.0f, 1.0f));
			values.add(rgbColor);
		}
		
		return values;
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

	public static List<Color> parseColorKeyword(final String input, final int nColors) {
		List<Color> colors = null;
		
		if (nColors > 0) {
			if (input.equals(RANDOM) || input.equals(CUSTOM))
				colors = generateRandomColors(nColors);
			if (input.equals(RAINBOW))
				colors = generateRainbowColors(nColors);
			if (input.equals(MODULATED))
				colors = generateModulatedRainbowColors(nColors);
			if (input.equals(CONTRASTING))
				colors = generateContrastingColors(nColors);
		} 

		if (colors == null || colors.isEmpty()) {
			// Perhaps it's one of the predefined color gradients for up-zero-down
			colors = ColorGradients.getGradient(input);
		}
		
		if (colors == null || colors.isEmpty()) {
			if (input.equals(UP_DOWN)) {
				colors = Arrays.asList(UP_DOWN_PUOR);
			} else {
				String[] colorArray = new String[1];
				colorArray[0] = input;
				colors = parseColorList(colorArray);
			}
		}
		
		return colors;
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
		String hex = Integer.toHexString(color.getRGB());
		hex = hex.substring(2, hex.length()); // remove alpha bits

		return "#" + hex;
	}
}
