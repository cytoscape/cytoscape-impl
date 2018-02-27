package org.cytoscape.internal.prefs.lib;
import java.awt.Color;


public class Colors
{
	public static String toString(Color inColor)
	{
		return ("" + inColor.getRed() + inColor.getGreen() + inColor.getBlue());
	}
	public static Color colorFromString(final String sv)		// reads 0xff1100 or #ff1100 or ff1100
	{
		if (sv == null) return Color.RED;
			try {
				int offset = 0;
				String cstr = sv.trim();
				if (cstr.charAt(0) == '#')					offset = 1;
				else if (cstr.startsWith("0x"))				offset = 2;
				cstr = cstr.substring(offset, Math.min(offset+8, cstr.length()));
				return new Color( Integer.parseInt(cstr, 16), cstr.length() == 8);
				
			} catch (Exception ex2) { }

		return Color.RED;
	}
	public static Color colorFromIndex(int i)	{		return colorFromString(colors[i]);	}
	static private String colors[] = { // from Macintosh color table
		"#FFFFFE", "#FFFFFF", "#E6E6E6", "#CCCCCC", "#B3B3B3", "#999999", "#7F7F7F", "#4C4C4C", "#333333",
		"#000000", "#FFE4E4", "#FFADAD", "#FF8686", "#FF4848", "#FF0000", "#D60000", "#AC0000", "#820000",
		"#580000", "#2E0000", "#FFF2E4", "#FFD6AD", "#FFC386", "#FFA448", "#FF7F00", "#D66B00", "#AC5600",
		"#824100", "#582C00", "#2E1700", "#FFFFE4", "#FFFFAD", "#FFFF86", "#FFFF48", "#FFFF00", "#D6D600",
		"#ACAC00", "#828200", "#585800", "#2E2E00", "#F2FFE4", "#D6FFAD", "#C3FF86", "#A4FF48", "#7FFF00",
		"#6BD600", "#56AC00", "#418200", "#2C5800", "#172E00", "#E4FFE4", "#ADFFAD", "#86FF86", "#48FF48",
		"#00FF00", "#00D600", "#00AC00", "#008200", "#005800", "#002E00", "#E4FFF2", "#ADFFD6", "#86FFC3",
		"#48FFA4", "#00FF7F", "#00D66B", "#00AC56", "#008241", "#00582C", "#002E17", "#E4FFFF", "#ADFFFF",
		"#86FFFF", "#48FFFF", "#00FFFF", "#00D6D6", "#00ACAC", "#008282", "#005858", "#002E2E", "#E4F2FF",
		"#ADD6FF", "#86C3FF", "#48A4FF", "#007FFF", "#006BD6", "#0056AC", "#004182", "#002C58", "#00172E",
		"#E4E4FF", "#ADADFF", "#8686FF", "#4848FF", "#0000FF", "#0000D6", "#0000AC", "#000082", "#000058",
		"#00002E", "#F2E4FF", "#D6ADFF", "#C386FF", "#A448FF", "#7F00FF", "#6B00D6", "#5600AC", "#410082",
		"#2C0058", "#17002E", "#FFE4FF", "#FFADFF", "#FF86FF", "#FF48FF", "#FF00FF", "#D600D6", "#AC00AC",
		"#820082", "#580058", "#2E002E", "#FFE4F2", "#FFADD6", "#FF86C3", "#FF48A4", "#FF007F", "#D6006B",
		"#AC0056", "#820041", "#58002C", "#2E0017" };
	public static Color kUnfilled = new Color(0x00ffffff, true);
}
