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

import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProvider;

/**
 * This is a built-in palette provider that provides the palettes originally
 * implemented by Cytoscape's VizMapper.
 */
public class BuiltinDivergentPaletteProvider implements PaletteProvider {
	
	enum DivPalette {
		YlBlRd ("Yellow-Black-Red", true, new Color[] { Color.RED, Color.BLACK, Color.YELLOW}),
		YlWhRd ("Yellow-White-Red", true, new Color[] { Color.RED, Color.WHITE, Color.YELLOW}),
		CyBlYl ("Cyan-Black-Yellow", true, new Color[] { Color.YELLOW, Color.BLACK, Color.CYAN}),
		CyWhYl ("Cyan-White-Yellow", true, new Color[] { Color.YELLOW, Color.WHITE, Color.CYAN}),
		BlBlYl ("Blue-Black-Yellow", true, new Color[] { Color.YELLOW, Color.BLACK, Color.BLUE}),
		BlWhYl ("Blue-White-Yellow", true, new Color[] { Color.YELLOW, Color.WHITE, Color.BLUE}),
		BlBlRd ("Blue-Black-Red", true, new Color[] { Color.RED, Color.BLACK, Color.BLUE}),
		BlGnYl ("Blue-Green-Yellow", true, new Color[] { Color.YELLOW, Color.GREEN, Color.BLUE}),
		YlBlPl ("Purple-Black-Yellow", true, new Color[] { Color.YELLOW, Color.BLACK, Color.MAGENTA}),
		GnBlPl ("Green-Black-Purple", true, new Color[] { Color.MAGENTA, Color.BLACK, Color.GREEN}),
		GnWhPl ("Green-White-Purple", true, new Color[] { Color.MAGENTA, Color.WHITE, Color.GREEN}),
		YlBlBl ("Blue-Black-Yellow", true, new Color[] { Color.YELLOW, Color.BLACK, Color.BLUE}),
		OrWhBl ("Blue-White-Orange", true, new Color[] { Color.ORANGE, Color.WHITE, Color.BLUE}),
		OrBlBl ("Blue-Black-Orange", true, new Color[] { Color.ORANGE, Color.BLACK, Color.BLUE});

		String paletteDescription;
		boolean colorBlindSafe;
		Color[] colors;
	 	DivPalette(String paletteDescription, boolean colorBlindSafe, Color[] colors) {
			this.paletteDescription = paletteDescription;
			this.colorBlindSafe = colorBlindSafe;
			this.colors = colors;
		}

		public String getDescription() { return paletteDescription; }
		public Color[] getColors() { return colors; }
		public boolean isColorBlindSafe() { return colorBlindSafe; }
	}

	Map<String, Palette> palettesMap;
	public BuiltinDivergentPaletteProvider() {
		palettesMap = new HashMap<>();
		for (DivPalette dp: DivPalette.values()) {
			palettesMap.put(dp.getDescription(), new DivergentPalette(dp));
		}
	}

	public String getProviderName() { return "Built-in Divergent Palettes"; }

	public List<Palette.PaletteType> getPaletteTypes() { return Collections.singletonList(Palette.PaletteType.DIVERGING); }

	public List<String> listPaletteNames(Palette.PaletteType type, boolean colorBlindSafe) {
		List<String> paletteNames = new ArrayList<>();
		if (type.equals(Palette.PaletteType.DIVERGING)) {
			return new ArrayList<String>(palettesMap.keySet());
		}
		return paletteNames;
	}

	@SuppressWarnings("unchecked")
	public List<Object> listPaletteIdentifiers(Palette.PaletteType type, boolean colorBlindSafe) {
		return (List)listPaletteNames(type, colorBlindSafe);
	}

	public Palette getPalette(String paletteName) {
		return getPalette(paletteName, 3);
	}

	public Palette getPalette(String paletteName, int size) {
		if (palettesMap.containsKey(paletteName))
			return palettesMap.get(paletteName);
		return null;
	}

	public Palette getPalette(Object paletteIdentifier) {
		return getPalette((String)paletteIdentifier);
	}

	public Palette getPalette(Object paletteIdentifier, int size) {
		return getPalette((String)paletteIdentifier, size);
	}

	class DivergentPalette extends AbstractDivergingPalette {
		DivergentPalette(DivPalette dp) {
			super(dp.getDescription(), dp.getColors(), dp.isColorBlindSafe());
		}
	}

}
