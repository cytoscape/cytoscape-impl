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

import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProvider;
import org.cytoscape.util.color.PaletteType;

/**
 * This is a built-in palette provider that provides the palettes originally
 * implemented by Cytoscape's VizMapper.
 */
public class ViridisPaletteProvider implements PaletteProvider {
	Map<String, Viridis> paletteMap;
	public ViridisPaletteProvider() {
		paletteMap = new HashMap<>();
		paletteMap.put(Viridis.Viridis.getName(), Viridis.Viridis);
		paletteMap.put(Viridis.Magma.getName(), Viridis.Magma);
		paletteMap.put(Viridis.Inferno.getName(), Viridis.Inferno);
		paletteMap.put(Viridis.Plasma.getName(), Viridis.Plasma);
	}

	public String getProviderName() { return "Viridis"; }

	public List<PaletteType> getPaletteTypes() { 
			return Arrays.asList(BrewerType.SEQUENTIAL); 
	}

	public List<String> listPaletteNames(PaletteType type, boolean colorBlindSafe) {
		if (type != BrewerType.SEQUENTIAL)
			return null;

		return new ArrayList<>(paletteMap.keySet());
	}

	@SuppressWarnings("unchecked")
	public List<Object> listPaletteIdentifiers(PaletteType type, boolean colorBlindSafe) {
		if (type != BrewerType.SEQUENTIAL)
			return null;

		return new ArrayList<>(paletteMap.values());
	}

	public Palette getPalette(String paletteName) {
		return getPalette(paletteName, -1);
	}

	public Palette getPalette(String paletteName, int size) {
		for (String name: paletteMap.keySet()) {
			if (name.equalsIgnoreCase(paletteName))
				return new PaletteWrapper(paletteMap.get(name), size);
		}
		return null;
	}

	public Palette getPalette(Object paletteIdentifier) {
		return getPalette(paletteIdentifier, -1);
	}

	public Palette getPalette(Object paletteIdentifier, int size) {
		for (Viridis cb: paletteMap.values()) {
			if (paletteIdentifier.equals(cb))
				return new PaletteWrapper(cb, size);
		}
		return null;
	}

	/**
	 * Wrapper for ColorBrewer palettes
	 */
	class PaletteWrapper implements Palette {
		Viridis palette;
		PaletteType type;
		int size;
		public PaletteWrapper(Viridis cbPalette, int size) {
			this.palette = cbPalette;
			if (size < 0) size = cbPalette.getColors().length;
			this.size = size;
			this.type = BrewerType.SEQUENTIAL;
		}

		public String getName() { return palette.getDescription(); }

		public Object getIdentifier() { return palette; }

		public boolean isColorBlindSafe() {
			return true;
		}

		public PaletteType getType() { return type; }

		public int size() { return size; }

		public Color[] getColors(int nColors) {
			return palette.getColorPalette(nColors);
		}

		public Color[] getColors() {
			return getColors(size);
		}

		public String toString() { return "Viridis "+getName(); }
	}
}
