package org.cytoscape.app.internal.util;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;

public final class IconUtil {

	public static final String ICON_APP_STORE = "a";
	
	private static Font iconFont;

	static {
		try {
			iconFont = Font.createFont(Font.TRUETYPE_FONT, IconUtil.class.getResourceAsStream("/fonts/app-manager.ttf"));
		} catch (FontFormatException e) {
			throw new RuntimeException();
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	public static Font getIconFont(float size) {
		return iconFont.deriveFont(size);
	}
	
	private IconUtil() {
		// restrict instantiation
	}
}
