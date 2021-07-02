package org.cytoscape.util.internal.color;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.	If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProvider;
import org.cytoscape.util.color.PaletteType;

public class AbstractDivergingPalette extends AbstractPalette {
	protected Color[] colors;

	AbstractDivergingPalette(PaletteProvider provider, String name, Color[] colors, boolean cbs) {
		super(provider, name, colors.length, (PaletteType)BrewerType.DIVERGING, cbs);
		this.colors = colors;
	}

	public Color[] getColors() {
		if (reversed) return reverseColors(colors);
		return colors;
	}

	public Color[] getColors(int nColors) {
		if (nColors == colors.length) getColors();
		if (reversed)
			return reverseColors(interpolatedColors(nColors));
		return interpolatedColors(nColors);
	}

	public String toString() { 
		return super.getName(); 
	}

	public boolean isReversable() { 
		return true; 
	}
	
	private Color[] interpolatedColors(int colorCount) {
		Color[] newColors = new Color[colorCount];
		int maxIndex = colors.length-1;
		float scale = maxIndex/(float)(colorCount-1);
	//System.out.println("scale: " + scale);

		for (int i = 0; i < colorCount; i++) {
			float value = scale * i;
			int index = (int)Math.floor(value);

			Color c1 = colors[index];
			float remainder = 0.0f;
			Color c2 = null;
			if (index+1 < colors.length) {
				c2 = colors[index+1];
				remainder = value - index;
			} else {
				c2 = colors[index];
			}
			// System.out.println("value: " + value + " index: " + index + " remainder: " + remainder);
			int red	 = Math.round((1 - remainder) * c1.getRed()		+ (remainder) * c2.getRed());
			int green = Math.round((1 - remainder) * c1.getGreen()	+ (remainder) * c2.getGreen());
			int blue	= Math.round((1 - remainder) * c1.getBlue()	 + (remainder) * c2.getBlue());

			newColors[i] = new Color(red, green, blue);
		}
		return newColors;
	}

}
