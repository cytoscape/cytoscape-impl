package org.cytoscape.internal.prefs;

public enum PPanels {

//	LAYOUTS("Layouts", "\uf133;", "layouts"),
	GROUPS("Groups", "\uf0c0", "groups"),
	TABLES("Tables", "\uf0ce", "tables"),
	STYLES("Styles", "\uf02e", "styles"),
	BEHAVIOR("Behavior", "\uf14a", ""),
	EFFICIENCY("Efficiency", "\uf085", "efficiency"),
	SECURITY("Security", "\uf023", "security"),
//	APPS("Apps", "\uf009", "apps"),
//	MENUS("Menus", "\uf039", "menus"),
//	LEGENDS("Legends", "\uf1c3", "legends"),
	COLORS("Colors", "\uf1fb", "colors"),
	TEXT("Text", "\uf032", "text"),
	LINKS("Links", "\uf08e", "links"),
//	IMAGES("Images", "\uf03e", "images"),
	ADVANCED("Advanced", "\uf1b2", "advanced");
	

	public static int[] rowLengths = new int[] { 3, 3, 3 };
	public static String[] rowNames = new String[]{ "Networks", "Cytoscape", "Output"};

	
	String displayName;
	String icon;		// define the FontAwesome icons
	String tooltip;
 
	PPanels(String displayName, String icon, String tooltip)
	{
		this.displayName = displayName;
		this.icon = icon;
		this.tooltip = tooltip;
	}
	
	public String getDisplayName() { return displayName; };
	public String getIcon() { return icon; };
	public String getTooltip() { return tooltip; };

}
