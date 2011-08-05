package org.cytoscape.view.vizmap.gui.internal.theme;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class ColorManager {

	private Map<String, Color> colorMap;

	public ColorManager() {
		colorMap = new HashMap<String, Color>();
		loadColor();
	}

	private void loadColor() {
		colorMap.put("UNUSED_COLOR", new Color(100, 100, 100, 50));
	}

	public Color getColor(String name) {
		final Color color = colorMap.get(name);
		if (color == null) {
			return Color.white;
		} else
			return color;
	}

}
