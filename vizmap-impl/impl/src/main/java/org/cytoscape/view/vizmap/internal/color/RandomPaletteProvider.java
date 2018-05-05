package org.cytoscape.view.vizmap.internal.color;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProvider;

/**
 * This is a built-in palette provider that provides the palettes originally
 * implemented by Cytoscape's VizMapper.
 */
public class RandomPaletteProvider implements PaletteProvider {
	public String getProviderName() { return "Random"; }

	public List<Palette.PaletteType> getPaletteTypes() { return Collections.singletonList(Palette.PaletteType.QUALITATIVE); }

	public List<String> listPaletteNames(Palette.PaletteType type, boolean colorBlindSafe) {
		return Arrays.asList(" ");
	}

	@SuppressWarnings("unchecked")
	public List<Object> listPaletteIdentifiers(Palette.PaletteType type, boolean colorBlindSafe) {
		return (List)listPaletteNames(type, colorBlindSafe);
	}

	public Palette getPalette(String paletteName) {
		return getPalette(paletteName, 8);
	}

	public Palette getPalette(String paletteName, int size) {
		if (paletteName.equalsIgnoreCase(" ")) {
			return new RandomPalette(size);
		}
		return null;
	}

	public Palette getPalette(Object paletteIdentifier) {
		return getPalette((String)paletteIdentifier);
	}

	public Palette getPalette(Object paletteIdentifier, int size) {
		return getPalette((String)paletteIdentifier, size);
	}

	private final int MAX_COLOR = 256 * 256 * 256;
	private final long seed = System.currentTimeMillis();
	private final Random rand = new Random(seed);

	class RandomPalette extends AbstractPalette {
		Color[] colors;
		RandomPalette(int size) {
			super(" ", size, PaletteType.QUALITATIVE, false);
			getColors(size);
		}

		public Color[] getColors() {
			return colors;
		}

		public Color[] getColors(int nColors) {
			colors = new Color[size];
			for (int i = 0; i < size; i++) {
				colors[i] = new Color((int)(rand.nextFloat() * MAX_COLOR));
			}
			return colors;
		}
	}
}
