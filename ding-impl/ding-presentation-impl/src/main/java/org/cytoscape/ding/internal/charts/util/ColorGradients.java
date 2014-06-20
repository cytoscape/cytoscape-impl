package org.cytoscape.ding.internal.charts.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ColorGradients {
	
	REDGREEN("redgreen", Color.GREEN, null, Color.RED),
	REDBLUE("redblue", Color.RED, null, Color.BLUE),
	YELLOWWHITECYAN("yellowwhitecyan", Color.CYAN, Color.WHITE, Color.YELLOW),
	YELLOWCYAN("yellowcyan", Color.CYAN, null, Color.YELLOW),
	YELLOWBLACKCYAN("yellowblackcyan", Color.CYAN, Color.BLACK, Color.YELLOW),
	YELLOWBLUE("yellowblue", Color.BLUE, null, Color.YELLOW),
	ORANGEPURPLE("orangepurple", Color.ORANGE, null, Color.MAGENTA),
	BLUEGREENYELLOW("bluegreenyellow", Color.BLUE, Color.GREEN, Color.YELLOW),
	PURPLEYELLOW("purpleyellow", Color.MAGENTA, null, Color.YELLOW),
	GREENPURPLE("greenpurple", Color.GREEN, null, Color.MAGENTA),
	REDYELLOW("redyellow", Color.RED, null, Color.YELLOW);

	private final Color up, zero, down;
	private final String name;
	private static Map<String, ColorGradients>cMap;

	ColorGradients(final String name, final Color down, final Color zero, final Color up) {
		this.name = name;
		this.up = up;
		this.down = down;
		this.zero = zero;
		addGradient(this);
	}

	public String getLabel() {
		return name;
	}

	private void addGradient(final ColorGradients col) {
		if (cMap == null) cMap = new HashMap<String,ColorGradients>();
		cMap.put(col.getLabel(), col);
	}

	public List<Color> getColors() {
		final List<Color> retColors = new ArrayList<Color>();
		retColors.add(up);
		if (zero != null) retColors.add(zero);
		retColors.add(down);

		return retColors;
	}

	public static List<Color> getGradient(final String name) {
		if (name != null && cMap.containsKey(name.toLowerCase()))
			return cMap.get(name).getColors();
		
		return Collections.emptyList();
	}
}
