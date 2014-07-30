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
	RED_BLACK_YELLOW("Red-Black-Yellow", Color.RED, Color.BLACK, Color.YELLOW),
	
	// 3-color ColorBrewer schemes (color-blind safe and print friendly):
	// Sequential - Multi-hue
	BU_GN("ColorBrewer: BuGn", new Color(44,162,95), new Color(153,216,201), new Color(229,245,249)),
	BU_PU("ColorBrewer: BuPu", new Color(136,86,167), new Color(158,188,218), new Color(224,236,244)),
	GN_BU("ColorBrewer: GnBu", new Color(67,162,202), new Color(168,221,181), new Color(224,243,219)),
	OR_RD("ColorBrewer: OrRd", new Color(227,74,51), new Color(253,187,132), new Color(254,232,200)),
	PU_BU("ColorBrewer: PuBu", new Color(43,140,190), new Color(166,189,219), new Color(236,231,242)),
	PU_BU_GN("ColorBrewer: PuBuGn", new Color(28,144,153), new Color(166,189,219), new Color(236,226,240)),
	PU_RD("ColorBrewer: PuRd", new Color(221,28,119), new Color(201,148,199), new Color(231,225,239)),
	RD_PU("ColorBrewer: RdPu", new Color(197,27,138), new Color(250,159,181), new Color(253,224,221)),
	YL_GN("ColorBrewer: YlGn", new Color(49,163,84), new Color(173,221,142), new Color(247,252,185)),
	YL_GN_B("ColorBrewer: YlGnB", new Color(44,127,184), new Color(127,205,187), new Color(237,248,177)),
	YL_OR_BR("ColorBrewer: YlOrBr", new Color(217,95,14), new Color(254,196,79), new Color(255,247,188)),
	YL_OR_RD("ColorBrewer: YlOrRd", new Color(240,59,32), new Color(254,178,76), new Color(255,237,160)),
	// Sequential - Single-Hue
	BLUES("ColorBrewer: Blues", new Color(49,130,189), new Color(158,202,225), new Color(222,235,247)),
	GREENS("ColorBrewer: Greens", new Color(49,163,84), new Color(161,217,155), new Color(229,245,224)),
	GREYS("ColorBrewer: Greys", new Color(99,99,99), new Color(189,189,189), new Color(240,240,240)),
	ORANGES("ColorBrewer: Oranges", new Color(230,85,13), new Color(253,174,107), new Color(254,230,206)),
	PURPLES("ColorBrewer: Purples", new Color(117,107,177), new Color(188,189,220), new Color(239,237,245)),
	REDS("ColorBrewer: Reds", new Color(222,45,38), new Color(252,146,114), new Color(254,224,210)),
	// Diverging
	BR_BG("ColorBrewer: BrBG", new Color(90,180,172), null/*new Color(245,245,245)*/, new Color(216,179,101)),
	PI_YG("ColorBrewer: PiYG", new Color(161,215,106), null/*new Color(247,247,247)*/, new Color(233,163,201)),
	PR_GN("ColorBrewer: PRGn", new Color(127,191,123), null/*new Color(247,247,247)*/, new Color(175,141,195)),
	PU_OR("ColorBrewer: PuOr", new Color(153,142,195), null/*new Color(247,247,247)*/, new Color(241,163,64)),
	RD_BU("ColorBrewer: RdBu", new Color(103,169,207), null/*new Color(247,247,247)*/, new Color(239,138,98)),
	RD_YL_BU("ColorBrewer: RdYlBu", new Color(145,191,219), null/*new Color(255,255,191)*/, new Color(252,141,89))
	;

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
	
	public static ColorGradient getGradient(final String name) {
		return cMap.get(normalize(name));
	}
	
	public static List<Color> getColors(String name) {
		name = normalize(name);
		
		if (name != null && cMap.containsKey(name))
			return cMap.get(name).getColors();
		
		return Collections.emptyList();
	}
	
	private void addGradient(final ColorGradient cg) {
		if (cMap == null) cMap = new HashMap<String, ColorGradient>();
		cMap.put(normalize(cg.name()), cg);
	}
	
	private static String normalize(final String name) {
		return name != null ? name.toUpperCase().replaceAll("[-_]", "") : null;
	}
}
