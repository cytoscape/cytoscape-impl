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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProvider;
import org.cytoscape.util.color.PaletteType;

/**
 * This is a built-in palette provider that provides the palettes originally
 * implemented by Cytoscape's VizMapper.
 */
public class RainbowPaletteProvider implements PaletteProvider {
	public String getProviderName() { return "Rainbow"; }

	public List<PaletteType> getPaletteTypes() { return Collections.singletonList(BrewerType.QUALITATIVE); }

	public List<String> listPaletteNames(PaletteType type, boolean colorBlindSafe) {
		if (type.equals(BrewerType.QUALITATIVE) || type.equals(BrewerType.ANY))
			return Arrays.asList("Rainbow");
		else return new ArrayList<String>();
	}

	@SuppressWarnings("unchecked")
	public List<Object> listPaletteIdentifiers(PaletteType type, boolean colorBlindSafe) {
		return (List)listPaletteNames(type, colorBlindSafe);
	}

	public Palette getPalette(String paletteName) {
		return getPalette(paletteName, 8);
	}

	public Palette getPalette(String paletteName, int size) {
		if (paletteName.equalsIgnoreCase("Rainbow") || paletteName.equalsIgnoreCase(" ")) {
			return new RainbowPalette(this, size);
		}
		return null;
	}

	public Palette getPalette(Object paletteIdentifier) {
		return getPalette((String)paletteIdentifier);
	}

	public Palette getPalette(Object paletteIdentifier, int size) {
		return getPalette((String)paletteIdentifier, size);
	}

	class RainbowPalette extends AbstractPalette {
		RainbowPalette(PaletteProvider provider, int size) {
			super(provider, "Rainbow", size, BrewerType.QUALITATIVE, false);
		}

		public Color[] getColors() {
			return getColors(size);
		}

		public Color[] getColors(int nColors) {
			Color[] colors = new Color[nColors];
			final float increment = 1f / (float)nColors;

			float hue = 0;

			for (int i = 0; i < nColors; i++) {
				hue = hue + increment;
				colors[i] = new Color(Color.HSBtoRGB(hue, 1f, 1f));
			}
			return colors;
		}

		public String toString() { return "Rainbow"; }
	}

}
