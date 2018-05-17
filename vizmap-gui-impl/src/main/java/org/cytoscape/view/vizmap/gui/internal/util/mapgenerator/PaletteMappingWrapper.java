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
import java.util.Properties;
import java.util.Set;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProvider;
import org.cytoscape.util.color.PaletteProviderManager;

import org.cytoscape.view.vizmap.gui.internal.util.ServicePropertiesUtil;
import org.cytoscape.view.vizmap.gui.util.DiscreteMappingGenerator;

public class PaletteMappingWrapper extends AbstractDiscreteMappingGenerator<Color> {
	String name;
	Palette palette;
	public PaletteMappingWrapper(String name, Palette palette) {
		super(Color.class);
		this.name = name;
		this.palette = palette;
	}

	public <T> Map<T, Color> generateMap(Set<T> attributeSet) {
		// Error check
		if(attributeSet == null || attributeSet.size() == 0)
			return null;

		int nColors = attributeSet.size();
		Color[] colors = palette.getColors(nColors);
		final Map<T, Color> valueMap = new HashMap<T, Color>();

		int i = 0;
		for (T key : attributeSet) {
			valueMap.put(key, colors[i++]);
		}

		return valueMap;
	}
}
