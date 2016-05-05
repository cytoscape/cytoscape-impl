package org.cytoscape.view.vizmap.internal.mappings;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

// TODO: DELETE?
public class ColorUtil {

	private static final int COLOR_RANGE_MAX = 255;

	private static final Map<String, String> COLOR_MAP = new HashMap<>();
	private static final String COLOR_CODE_RESOURCE = "cross_browser_color_code.txt";

	static {
		buildColorCodeTable(ColorUtil.class.getClassLoader().getResource(COLOR_CODE_RESOURCE));
	}

	private static void buildColorCodeTable(final URL resourceURL) {
		BufferedReader bufRd = null;
		String line;

		try {
			bufRd = new BufferedReader(new InputStreamReader(resourceURL.openStream(), Charset.forName("UTF-8").newDecoder()));
			while ((line = bufRd.readLine()) != null) {
				String[] parts = line.split("\\t");
				COLOR_MAP.put(parts[0].trim().toUpperCase(), parts[1].trim());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufRd != null) {
				try {
					bufRd.close();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					bufRd = null;
				}
			}
		}
	}

	/**
	 * Convert text representation of color into Color object.
	 * 
	 * <p>
	 * This parser cupports the following test representation of color:
	 * 
	 * <ul>
	 * <li>Hex representation of color (e.g. #6677FF)</li>
	 * <li>RGB numbers (float or integer, e.g. (255, 10, 100))</li>
	 * <li>Java standard colors text representations (all lower case. e.g.
	 * "black")</li>
	 * </ul>
	 * 
	 * @param colorAsText
	 * @return Color object
	 */
	public static Color parseColorText(final String colorAsText) {
		if (colorAsText == null)
			return null;

		final String trimed = colorAsText.trim();
		final String[] parts = trimed.split(",");

		// Start by seeing if this is a hex representation
		if (parts.length == 1) {
			try {
				// Chech this is a cross-browser standard color name
				final String upper = trimed.toUpperCase();
				if (COLOR_MAP.containsKey(upper))
					return Color.decode(COLOR_MAP.get(upper));

				// Otherwise, treat as a hex notation.
				return Color.decode(trimed);
			} catch (Exception e) {
				return null;
			}
		}

		if (parts.length != 3)
			return null;

		final String red = parts[0].trim();
		final String green = parts[1].trim();
		final String blue = parts[2].trim();

		try {
			if (red.contains(".") || green.contains(".") || blue.contains(".")) {
				float r = Float.parseFloat(red);
				float g = Float.parseFloat(green);
				float b = Float.parseFloat(blue);
				return new Color(r, g, b);
			} else {
				int r = Integer.parseInt(red);
				int g = Integer.parseInt(green);
				int b = Integer.parseInt(blue);
				return new Color(r, g, b);
			}

		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Convert Color object into RGB String (e.g., (200,100,120))
	 * 
	 * @param color
	 * @return String representation
	 */
	public static String getColorAsText(Color color) {
		return color.getRed() + "," + color.getGreen() + "," + color.getBlue();
	}

	public static Color getComplementaryColor(final Color color1) {
		final Color complement = new Color(COLOR_RANGE_MAX - color1.getRed(), COLOR_RANGE_MAX - color1.getGreen(),
				COLOR_RANGE_MAX - color1.getBlue());
		final float[] hsb = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
		final float[] cHSB = Color.RGBtoHSB(complement.getRed(), complement.getGreen(), complement.getBlue(), null);

		if (hsb[2] > 0.7f)
			return Color.getHSBColor(cHSB[0], 1.0f - hsb[1], 0f);
		else
			return Color.getHSBColor(cHSB[0], 1.0f - hsb[1], 1.0f);
	}

}
