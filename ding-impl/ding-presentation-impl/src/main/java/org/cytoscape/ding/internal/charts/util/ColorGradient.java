package org.cytoscape.ding.internal.charts.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ColorGradient {
	
	CYAN_YELLOW("Cyan-Yellow", Color.CYAN, null, Color.YELLOW),
	CYAN_WHITE_YELLOW("Cyan-White-Yellow", Color.CYAN, Color.WHITE, Color.YELLOW),
	CYAN_BLACK_YELLOW("Cyan-Black-Yellow", Color.CYAN, Color.BLACK, Color.YELLOW),
	BLUE_RED("Blue-Red", Color.RED, null, Color.BLUE),
	BLUE_BLACK_RED("Blue-Black-Red", Color.RED, Color.BLACK, Color.BLUE),
	BLUE_YELLOW("Blue-Yellow", Color.BLUE, null, Color.YELLOW),
	BLUE_GREEN_YELLOW("Blue-Green-Yellow", Color.BLUE, Color.GREEN, Color.YELLOW),
	GREEN_PURPLE("Green-Purple", Color.GREEN, null, Color.MAGENTA),
	GREEN_BLACK_PURPLE("Green-Black_Purple", Color.GREEN, Color.BLACK, Color.MAGENTA),
	GREEN_RED("Green-Red", Color.GREEN, null, Color.RED),
	GREEN_BLACK_RED("Green-Black-Red", Color.GREEN, Color.BLACK, Color.RED),
	ORANGE_PURPLE("Orange-Purple", Color.ORANGE, null, Color.MAGENTA),
	PURPLE_YELLOW("Purple-Yellow", Color.MAGENTA, null, Color.YELLOW),
	PURPLE_BLACK_YELLOW("Purple-Black-Yellow", Color.MAGENTA, Color.BLACK, Color.YELLOW),
	RED_YELLOW("Red-Yellow", Color.RED, null, Color.YELLOW),
	RED_BLACK_YELLOW("Red-Black-Yellow", Color.RED, Color.BLACK, Color.YELLOW);

	private String label;
	private final Color up, zero, down;
	
	private static Map<String, ColorGradient>cMap;

	ColorGradient(final String label, final Color down, final Color zero, final Color up) {
		this.label = label;
		this.up = up;
		this.down = down;
		this.zero = zero;
		addGradient(this);
	}

	public String getLabel() {
		return label;
	}
	
	public List<Color> getColors() {
		final List<Color> retColors = new ArrayList<Color>();
		retColors.add(up);
		if (zero != null) retColors.add(zero);
		retColors.add(down);

		return retColors;
	}

	public static boolean contains(final String name) {
		return name != null && cMap.containsKey(normalize(name));
	}
	
	public static List<Color> getGradient(String name) {
		name = normalize(name);
		
		if (name != null && cMap.containsKey(name))
			return cMap.get(name).getColors();
		
		return Collections.emptyList();
	}
	
	private void addGradient(final ColorGradient col) {
		if (cMap == null) cMap = new HashMap<String,ColorGradient>();
		cMap.put(col.name().replaceAll("_", ""), col);
	}
	
	private static String normalize(final String name) {
		return name != null ? name.toUpperCase().replaceAll("[-_]", "") : null;
	}
}
