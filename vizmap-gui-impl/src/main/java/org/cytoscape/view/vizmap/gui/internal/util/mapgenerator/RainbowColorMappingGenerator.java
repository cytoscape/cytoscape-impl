package org.cytoscape.view.vizmap.gui.internal.util.mapgenerator;

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
		if(attributeSet == null || attributeSet.size() == 0)
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
