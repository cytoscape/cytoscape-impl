package org.cytoscape.view.vizmap.gui.internal.util.mapgenerator;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProvider;
import org.cytoscape.util.color.PaletteProviderManager;

/**
 *
 */
public class RainbowOscColorMappingGenerator extends
		AbstractDiscreteMappingGenerator<Color> {

	PaletteProviderManager paletteProviderMgr;

	public RainbowOscColorMappingGenerator(final PaletteProviderManager paletteProviderManager, 
	                                       final Class<Color> type) {
		super(type);
		paletteProviderMgr = paletteProviderManager;
	}

	/**
	 * Generate discrete mapping from T to Color (Rainbow w/ oscillation
	 * algorithm)
	 * 
	 * @param <T>
	 *            Attribute type
	 * @param attributeSet
	 *            Set of attribute type T
	 * 
	 * @return Discrete mapping from T to Color
	 */
	public <T> Map<T, Color> generateMap(Set<T> attributeSet) {
		final Map<T, Color> valueMap = new HashMap<T, Color>();

		int nColors = attributeSet.size();

		Color[] colors = paletteProviderMgr.getPaletteProvider("RainbowOSC").getPalette("rainbowosc", nColors).getColors();

		int i = 0;
		for (T key : attributeSet)
			valueMap.put(key, colors[i++]);

		return valueMap;
	}
}
