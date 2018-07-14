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

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProvider;
import org.cytoscape.util.color.PaletteProviderManager;
import org.cytoscape.util.color.PaletteType;

public class PaletteProviderManagerImpl implements PaletteProviderManager {
	Map<String,PaletteProvider> providerMap;
	Map<Object,Palette> savedPaletteMap;

	public PaletteProviderManagerImpl() {
		providerMap = new HashMap<>();
		savedPaletteMap = new HashMap<>();

		// Create our built-in palette providers
		addPaletteProvider(new RainbowPaletteProvider());
		addPaletteProvider(new RainbowOSCPaletteProvider());
		addPaletteProvider(new RandomPaletteProvider());
		addPaletteProvider(new ColorBrewerPaletteProvider());
		addPaletteProvider(new ViridisPaletteProvider());
		addPaletteProvider(new BuiltinDivergentPaletteProvider());
	}

	public List<PaletteProvider> getPaletteProviders() {
		return new ArrayList<PaletteProvider>(providerMap.values());
	}

	public List<PaletteProvider> getPaletteProviders(PaletteType type, boolean colorSafe) {
		List<PaletteProvider> providers = new ArrayList<>();
		for (PaletteProvider provider: providerMap.values()) {
			List<String> palettes = provider.listPaletteNames(type, colorSafe);
			if (palettes != null && palettes.size() > 0)
				providers.add(provider);
		}
		return providers;
	}

	public PaletteProvider getPaletteProvider(String provider) {
		if (providerMap.containsKey(provider)) {
			return providerMap.get(provider);
		}
		return null;
	}

	public void addPaletteProvider(PaletteProvider provider) {
		providerMap.put(provider.getProviderName(), provider);
	}

	public void removePaletteProvider(PaletteProvider provider) {
		if (providerMap.containsKey(provider.getProviderName()))
			providerMap.remove(provider.getProviderName());
	}

	public void savePalette(Object key, Palette palette) {
		savedPaletteMap.put(key, palette);
	}

	public Palette retrievePalette(Object key) {
		if (!savedPaletteMap.containsKey(key))
			return null;
		return savedPaletteMap.get(key);
	}
}
