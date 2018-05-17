package org.cytoscape.view.vizmap.gui.internal.util.mapgenerator;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProvider;
import org.cytoscape.util.color.PaletteProviderManager;

/**
 * Mapping generator from any attributes to random color
 */
public class RandomColorMappingGenerator extends
		AbstractDiscreteMappingGenerator<Color> {

	PaletteProviderManager paletteProviderMgr;

	public RandomColorMappingGenerator(final PaletteProviderManager paletteProviderManager, 
	                                   final Class<Color> type) {
		super(type);
		paletteProviderMgr = paletteProviderManager;
	}

	/**
	 * From a given set of attributes, create a discrete mapping from the
	 * attribute to random color.
	 * 
	 * @param <T>
	 *            Attribute type
	 * @param attributeSet
	 *            Set of attribute values
	 * 
	 * @return map from T to Color
	 */
	public <T> Map<T, Color> generateMap(Set<T> attributeSet) {
		final Map<T, Color> valueMap = new HashMap<T, Color>();

		int nColors = attributeSet.size();

		Color[] colors = paletteProviderMgr.getPaletteProvider("Random").getPalette("random", nColors).getColors();

		int i = 0;
		for (T key : attributeSet)
			valueMap.put(key, colors[i++]);

		return valueMap;
	}
}
