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
import java.util.Set;

public class RainbowColorMappingGenerator extends
		AbstractDiscreteMappingGenerator<Color> {

	public RainbowColorMappingGenerator(final Class<Color> type) {
		super(type);
	}

	public <T> Map<T, Color> generateMap(Set<T> attributeSet) {
		// Error check
		if(attributeSet == null || attributeSet.isEmpty())
			return null;

		final float increment = 1f / ((Number) attributeSet.size())
				.floatValue();

		float hue = 0;

		final Map<T, Color> valueMap = new HashMap<T, Color>();

		for (T key : attributeSet) {
			hue = hue + increment;
			valueMap.put(key, new Color(Color.HSBtoRGB(hue, 1f, 1f)));
		}

		return valueMap;
	}

}
