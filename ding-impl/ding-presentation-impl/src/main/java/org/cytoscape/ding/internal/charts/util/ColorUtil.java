package org.cytoscape.ding.internal.charts.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public final class ColorUtil {

	public static final String CONTRASTING = "contrasting";
	public static final String MODULATED = "modulated";
	public static final String RAINBOW = "rainbow";
	public static final String RANDOM = "random";
	public static final String UP = "up:";
	public static final String DOWN = "down:";
	public static final String ZERO = "zero:";
	
	private static final double EPSILON = 1E-8f;

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
			result.add(index, new Color(r, g, b, 200));
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

	public static List<Color> parseUpDownColor(String[] colorArray) {
		if (colorArray.length < 2)
			return null;

		String[] colors = new String[3];
		colors[2] = "black";
		
		for (int index = 0; index < colorArray.length; index++) {
			if (colorArray[index].toLowerCase().startsWith(UP)) {
				colors[0] = colorArray[index].substring(UP.length());
			} else if (colorArray[index].toLowerCase().startsWith(DOWN)) {
				colors[1] = colorArray[index].substring(DOWN.length());
			} else if (colorArray[index].toLowerCase().startsWith(ZERO)) {
				colors[2] = colorArray[index].substring(ZERO.length());
			}
		}
		
		return ColorUtil.parseColorList(colors);
	}
	
//	private List<Color> parseUpDownColor(String[] colorArray, List<Double> values, final boolean normalize) {
//	List<Color> upDownColors = parseUpDownColor(colorArray);
//	Color up = upDownColors.get(0);
//	Color down = upDownColors.get(1);
//	Color zero = upDownColors.get(2);
//
//	List<Color> results = new ArrayList<Color>(values.size());
//	
//	for (Double v : values) {
//		if (v == null)
//			return null;
//		
//		double vn = v;
//		
//		if (normalize)
//			vn = normalize(v, rangeMin, rangeMax);
//		
//		if (vn < (-EPSILON))
//			results.add(scaleColor(-vn, zero, down));
//		else if (vn > EPSILON)
//			results.add(scaleColor(vn, zero, up));
//		else
//			results.add(zero);
//	}
//	
//	return results;
//}
//
//private Color scaleColor(double v, Color zero, Color c) {
//	if (rangeMin == 0.0 && rangeMax == 0.0)
//		return c;
//
//	// We want to scale our color to be between "zero" and "c"
//	int b = (int) (Math.abs(c.getBlue() - zero.getBlue()) * v);
//	int r = (int) (Math.abs(c.getRed() - zero.getRed()) * v);
//	int g = (int) (Math.abs(c.getGreen() - zero.getGreen()) * v);
//	
//	return new Color(r, g, b);
//}
	
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

	public static  List<Color> parseColorKeyword(String input, int nColors) {
		if (input.equals(RANDOM))
			return generateRandomColors(nColors);
		else if (input.equals(RAINBOW))
			return generateRainbowColors(nColors);
		else if (input.equals(MODULATED))
			return generateModulatedRainbowColors(nColors);
		else if (input.equals(CONTRASTING))
			return generateContrastingColors(nColors);
		else {
			String[] colorArray = new String[1];
			colorArray[0] = input;
			List<Color> colors = parseColorList(colorArray);
			return colors;
		}
	}
}
