package org.cytoscape.util.internal.color;

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

import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProvider;
import org.cytoscape.util.color.PaletteType;

/**
 * This is a built-in palette provider that provides the palettes originally
 * implemented by Cytoscape's VizMapper.
 */
public class ColorBrewerPaletteProvider implements PaletteProvider {
	Map<PaletteType,Map<String, ColorBrewer>> paletteMap;
	public ColorBrewerPaletteProvider() {
		paletteMap = new HashMap<>();
		paletteMap.put(BrewerType.SEQUENTIAL,
		               getBrewerPalettes(ColorBrewer.getSequentialColorPalettes(false)));
		paletteMap.put(BrewerType.QUALITATIVE,
		               getBrewerPalettes(ColorBrewer.getQualitativeColorPalettes(false)));
		paletteMap.put(BrewerType.DIVERGING,
		               getBrewerPalettes(ColorBrewer.getDivergingColorPalettes(false)));
	}

	public String getProviderName() { return "ColorBrewer"; }

	public List<PaletteType> getPaletteTypes() { 
			return Arrays.asList(BrewerType.SEQUENTIAL, 
			                     BrewerType.QUALITATIVE, 
			                     BrewerType.DIVERGING); 
	}

	public List<String> listPaletteNames(PaletteType type, boolean colorBlindSafe) {
		Map<String, ColorBrewer> palettes = paletteMap.get(type);
		if (colorBlindSafe) {
			List<String> names = new ArrayList<>();
			for (String key: palettes.keySet()) {
				if (palettes.get(key).isColorBlindSave())
					names.add(key);
			}
			return names;
		}
		return new ArrayList<>(palettes.keySet());
	}

	@SuppressWarnings("unchecked")
	public List<Object> listPaletteIdentifiers(PaletteType type, boolean colorBlindSafe) {
		Map<String, ColorBrewer> palettes = paletteMap.get(type);
		if (colorBlindSafe) {
			List<Object> safePalettes = new ArrayList<>();
			for (String key: palettes.keySet()) {
				if (palettes.get(key).isColorBlindSave())
					safePalettes.add(palettes.get(key));
			}
			return safePalettes;
		}
		return new ArrayList<>(palettes.values());
	}

	public Palette getPalette(String paletteName) {
		return getPalette(paletteName, 8);
	}

	public Palette getPalette(String paletteName, int size) {
		for (PaletteType type: paletteMap.keySet()) {
			Map<String, ColorBrewer> palettes = paletteMap.get(type);
			for (String name: palettes.keySet()) {
				if (name.equalsIgnoreCase(paletteName))
					return new BrewerPalette(palettes.get(name), size, type);
			}
		}
		return null;
	}

	public Palette getPalette(Object paletteIdentifier) {
		return getPalette(paletteIdentifier, 8);
	}

	public Palette getPalette(Object paletteIdentifier, int size) {
		for (PaletteType type: paletteMap.keySet()) {
			Map<String, ColorBrewer> palettes = paletteMap.get(type);
			for (ColorBrewer cb: palettes.values()) {
				if (paletteIdentifier.equals(cb))
					return new BrewerPalette(cb, size, type);
			}
		}
		return null;
	}

	private Map<String, ColorBrewer> getBrewerPalettes(ColorBrewer[] palettes) {
		Map<String, ColorBrewer> paletteMap = new HashMap<>();
		for (ColorBrewer cb: palettes) {
			paletteMap.put(cb.getPaletteDescription(), cb);
		}
		return paletteMap;
	}

	/**
	 * Wrapper for ColorBrewer palettes
	 */
	class BrewerPalette implements Palette {
		ColorBrewer palette;
		PaletteType type;
		int size;
		public BrewerPalette(ColorBrewer cbPalette, int size, PaletteType type) {
			this.palette = cbPalette;
			this.size = size;
			this.type = type;
		}

		public String getName() { return palette.getPaletteDescription(); }

		public Object getIdentifier() { return palette; }

		public boolean isColorBlindSafe() {
			return palette.isColorBlindSave();
		}

		public PaletteType getType() { return type; }

		public int size() { return size; }

		public Color[] getColors(int nColors) {
			return palette.getColorPalette(nColors);
		}

		public Color[] getColors() {
			return getColors(size);
		}

		public String toString() { return "ColorBrewer "+getName(); }
	}
}
